package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.ItemManager;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.item.*;
import com.github.ltprc.gamepal.model.map.InteractionInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class ItemManagerImpl implements ItemManager {

    private static final Log logger = LogFactory.getLog(ItemManagerImpl.class);

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private EventManager eventManager;

    @Override
    public boolean useItem(GameWorld world, String userCode, String itemNo, int itemAmount) {
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
            logger.warn(ErrorUtil.ERROR_1043);
            return false;
        }
        if (StringUtils.isBlank(itemNo) || !bagInfo.getItems().containsKey(itemNo)) {
            logger.error(ErrorUtil.ERROR_1020);
            return false;
        }
        if (bagInfo.getItems().getOrDefault(itemNo, 0) == 0 || itemAmount <= 0) {
            logger.error(ErrorUtil.ERROR_1024);
            return false;
        }
        switch (itemNo.charAt(0)) {
            case ItemConstants.ITEM_CHARACTER_TOOL:
                useTools(world, userCode, itemNo);
                playerService.updateTimestamp(playerInfo);
                break;
            case ItemConstants.ITEM_CHARACTER_OUTFIT:
                useOutfits(world, userCode, itemNo);
                playerService.updateTimestamp(playerInfo);
                break;
            case ItemConstants.ITEM_CHARACTER_CONSUMABLE:
                useConsumable(world, userCode, itemNo, itemAmount);
                break;
            case ItemConstants.ITEM_CHARACTER_MATERIAL:
            case ItemConstants.ITEM_CHARACTER_JUNK:
            case ItemConstants.ITEM_CHARACTER_AMMO:
            case ItemConstants.ITEM_CHARACTER_NOTE:
            case ItemConstants.ITEM_CHARACTER_RECORDING:
            default:
                break;
        }
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        return true;
    }

    @Override
    public boolean getItem(GameWorld world, String userCode, String itemNo, int itemAmount) {
        if (itemAmount == 0) {
            logger.warn(ErrorUtil.ERROR_1035);
            return true;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        if (!bagInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        BagInfo bagInfo = bagInfoMap.get(userCode);
        int oldItemAmount = bagInfo.getItems().getOrDefault(itemNo, 0);
        if (oldItemAmount + itemAmount < 0) {
            logger.error(ErrorUtil.ERROR_1035);
            return false;
        }
        bagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (bagInfo.getItems().getOrDefault(itemNo, 0) == 0) {
            switch (itemNo.charAt(0)) {
                case ItemConstants.ITEM_CHARACTER_TOOL:
                    if (playerInfoMap.containsKey(userCode)) {
                        playerInfoMap.get(userCode).getTools().remove(itemNo);
                    }
                    playerService.updateSkillsByTool(userCode);
                    break;
                case ItemConstants.ITEM_CHARACTER_OUTFIT:
                    if (playerInfoMap.containsKey(userCode)) {
                        playerInfoMap.get(userCode).getOutfits().remove(itemNo);
                    }
                    playerService.updateSkillsByTool(userCode);
                    break;
                default:
                    break;
            }
        }
        BigDecimal capacity = bagInfo.getCapacity();
        if (worldService.getItemMap().containsKey(itemNo)) {
            Item item = worldService.getItemMap().get(itemNo);
            bagInfo.setCapacity(capacity.add(item.getWeight().multiply(BigDecimal.valueOf(itemAmount))));
            if (itemAmount < 0) {
                playerService.generateNotificationMessage(userCode,
                        "失去 " + item.getName() + "(" + (-1) * itemAmount + ")");
            } else {
                playerService.generateNotificationMessage(userCode,
                        "获得 " + item.getName() + "(" + itemAmount + ")");
            }
        }
        if (world.getFlagMap().containsKey(userCode)) {
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        }
        return true;
    }

    @Override
    public boolean getPreservedItem(GameWorld world, String userCode, String itemNo, int itemAmount) {
        if (itemAmount == 0) {
            logger.warn(ErrorUtil.ERROR_1035);
            return true;
        }
        Map<String, BagInfo> preservedBagInfoMap = world.getPreservedBagInfoMap();
        BagInfo preservedBagInfo = preservedBagInfoMap.get(userCode);
        int oldItemAmount = preservedBagInfo.getItems().getOrDefault(itemNo, 0);
        if (oldItemAmount + itemAmount < 0) {
            logger.error(ErrorUtil.ERROR_1035);
            return false;
        }
        preservedBagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (itemAmount < 0) {
            playerService.generateNotificationMessage(userCode,
                    "失去 " + worldService.getItemMap().get(itemNo).getName() + "(" + (-1) * itemAmount + ")");
        } else {
            playerService.generateNotificationMessage(userCode,
                    "储存 " + worldService.getItemMap().get(itemNo).getName() + "(" + itemAmount + ")");
        }
        if (world.getFlagMap().containsKey(userCode)) {
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        }
        return true;
    }

    @Override
    public void getInteractedItem(GameWorld world, String userCode, String itemNo, int itemAmount) {
        if (itemAmount == 0) {
            logger.warn(ErrorUtil.ERROR_1035);
            return;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        int oldItemAmount = bagInfo.getItems().getOrDefault(itemNo, 0);
        if (oldItemAmount + itemAmount < 0) {
            logger.error(ErrorUtil.ERROR_1035);
            return;
        }
        InteractionInfo interactionInfo = world.getInteractionInfoMap().get(userCode);
        if (null == interactionInfo) {
            logger.error(ErrorUtil.ERROR_1034);
            return;
        }
        String id = interactionInfo.getId();
        Block block = world.getBlockMap().get(id);
        if (null == block) {
            logger.error(ErrorUtil.ERROR_1012);
            return;
        }
        BagInfo interactedBagInfo;
        int oldInteractedItemAmount;
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_STORAGE:
                Map<String, BagInfo> preservedBagInfoMap = world.getPreservedBagInfoMap();
                interactedBagInfo = preservedBagInfoMap.get(userCode);
                oldInteractedItemAmount = interactedBagInfo.getItems().getOrDefault(itemNo, 0);
                break;
            case BlockConstants.BLOCK_TYPE_CONTAINER:
                interactedBagInfo = bagInfoMap.get(id);
                oldInteractedItemAmount = interactedBagInfo.getItems().getOrDefault(itemNo, 0);
                break;
            default:
                logger.error(ErrorUtil.ERROR_1013);
                return;
        }
        if (oldInteractedItemAmount - itemAmount < 0) {
            logger.error(ErrorUtil.ERROR_1035);
            return;
        }
        interactedBagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldInteractedItemAmount - itemAmount, Integer.MAX_VALUE)));
        bagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (bagInfo.getItems().getOrDefault(itemNo, 0) == 0) {
            switch (itemNo.charAt(0)) {
                case ItemConstants.ITEM_CHARACTER_TOOL:
                    if (playerInfoMap.containsKey(userCode)) {
                        playerInfoMap.get(userCode).getTools().remove(itemNo);
                    }
                    break;
                case ItemConstants.ITEM_CHARACTER_OUTFIT:
                    if (playerInfoMap.containsKey(userCode)) {
                        playerInfoMap.get(userCode).getOutfits().remove(itemNo);
                    }
                    break;
                default:
                    break;
            }
        }
        BigDecimal capacity = bagInfo.getCapacity();
        bagInfo.setCapacity(capacity.add(worldService.getItemMap().get(itemNo).getWeight()
                .multiply(BigDecimal.valueOf(itemAmount))));
        if (itemAmount < 0) {
            playerService.generateNotificationMessage(userCode,
                    "存入 " + worldService.getItemMap().get(itemNo).getName() + "(" + (-1) * itemAmount + ")");
        } else {
            playerService.generateNotificationMessage(userCode,
                    "取出 " + worldService.getItemMap().get(itemNo).getName() + "(" + itemAmount + ")");
        }
        if (world.getFlagMap().containsKey(userCode)) {
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        }
    }

    @Override
    public void recycleItem(GameWorld world, String userCode, String itemNo, int itemAmount) {
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        if (StringUtils.isBlank(itemNo) || !bagInfo.getItems().containsKey(itemNo)) {
            logger.error(ErrorUtil.ERROR_1020);
            return;
        }
        if (bagInfo.getItems().getOrDefault(itemNo, 0) == 0 || itemAmount <= 0) {
            logger.error(ErrorUtil.ERROR_1024);
            return;
        }
        switch (itemNo.charAt(0)) {
            case ItemConstants.ITEM_CHARACTER_JUNK:
                getItem(world, userCode, itemNo, -1);
                ((Junk) worldService.getItemMap().get(itemNo)).getMaterials()
                        .forEach((key, value) -> getItem(world, userCode, key, value));
                break;
            case ItemConstants.ITEM_CHARACTER_TOOL:
            case ItemConstants.ITEM_CHARACTER_OUTFIT:
            case ItemConstants.ITEM_CHARACTER_CONSUMABLE:
            case ItemConstants.ITEM_CHARACTER_MATERIAL:
            case ItemConstants.ITEM_CHARACTER_AMMO:
            case ItemConstants.ITEM_CHARACTER_NOTE:
            case ItemConstants.ITEM_CHARACTER_RECORDING:
            default:
                break;
        }
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
    }

    @Override
    public void useRecipe(GameWorld world, String userCode, String recipeNo, int recipeAmount) {
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        Map<String, Recipe> recipeMap = worldService.getRecipeMap();
        if (StringUtils.isBlank(recipeNo) || !recipeMap.containsKey(recipeNo)) {
            logger.error(ErrorUtil.ERROR_1023);
            return;
        }
        Recipe recipe = recipeMap.get(recipeNo);
        if (recipe.getCost().entrySet().stream()
                .anyMatch(entry -> bagInfo.getItems().getOrDefault(entry.getKey(), 0) < entry.getValue() * recipeAmount)) {
            logger.error(ErrorUtil.ERROR_1024);
            return;
        }
        recipe.getCost().forEach((key1, value1) -> getItem(world, userCode, key1, -value1 * recipeAmount));
        recipe.getValue().forEach((key, value) -> getItem(world, userCode, key, value * recipeAmount));
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_RECIPES] = true;
    }

    @Override
    public boolean useTools(GameWorld world, String userCode, String itemNo) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int toolIndex = ((Tool) worldService.getItemMap().get(itemNo)).getItemIndex();
        Set<String> newTools = new ConcurrentSkipListSet<>();
        if (playerInfo.getTools().contains(itemNo)) {
//            playerInfo.getTools().stream()
//                    .filter(toolNo -> !itemNo.equals(toolNo))
//                    .forEach(newTools::add);
//            playerInfo.setTools(newTools);
            playerInfo.getTools().remove(itemNo);
        } else if (toolIndex != ItemConstants.TOOL_INDEX_DEFAULT) {
            playerInfo.getTools().stream()
                    .filter(toolNo -> toolIndex != ((Tool) worldService.getItemMap().get(toolNo)).getItemIndex())
                    .forEach(newTools::add);
            playerInfo.setTools(newTools);
            playerInfo.getTools().add(itemNo);
        } else {
            playerInfo.getTools().add(itemNo);
        }
        playerService.updateSkillsByTool(userCode);
        return true;
    }

    @Override
    public boolean useOutfits(GameWorld world, String userCode, String itemNo) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int outfitIndex = ((Outfit) worldService.getItemMap().get(itemNo)).getItemIndex();
        Set<String> newOutfits = new ConcurrentSkipListSet<>();
        if (playerInfo.getOutfits().contains(itemNo)) {
//            playerInfo.getOutfits().stream()
//                    .filter(outfitNo -> !itemNo.equals(outfitNo))
//                    .forEach(newOutfits::add);
//            playerInfo.setOutfits(newOutfits);
            playerInfo.getOutfits().remove(itemNo);
        } else if (outfitIndex != ItemConstants.OUTFIT_INDEX_DEFAULT) {
            playerInfo.getOutfits().stream()
                    .filter(outfitNo -> outfitIndex != ((Outfit) worldService.getItemMap().get(outfitNo)).getItemIndex())
                    .forEach(newOutfits::add);
            playerInfo.setOutfits(newOutfits);
            playerInfo.getOutfits().add(itemNo);
        } else {
            playerInfo.getOutfits().add(itemNo);
        }
        playerService.updateSkillsByTool(userCode);
        return true;
    }

    @Override
    public boolean useConsumable(GameWorld world, String userCode, String itemNo, int itemAmount) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return false;
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        for (int i = 0; i < itemAmount; i++) {
            ((Consumable) worldService.getItemMap().get(itemNo)).getEffects().entrySet()
                    .forEach((Map.Entry<String, Integer> entry) -> {
                        switch (entry.getKey()) {
                            case "hp":
                                eventManager.changeHp(world, player, entry.getValue(), false);
                                break;
                            case "vp":
                                playerService.changeVp(userCode, entry.getValue(), false);
                                break;
                            case "hunger":
                                playerService.changeHunger(userCode, entry.getValue(), false);
                                break;
                            case "thirst":
                                playerService.changeThirst(userCode, entry.getValue(), false);
                                break;
                            default:
                                break;
                        }
                    });
        }
        switch (worldService.getItemMap().get(itemNo).getItemNo()) {
            case "c005":
                eventManager.changeHp(world, player, 0, true);
                break;
            case "c006":
                playerService.revivePlayer(userCode);
                break;
            case "c007":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_STUNNED] = -1;
                break;
            case "c008":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_STUNNED] = 0;
                break;
            case "c009":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLEEDING] = -1;
                break;
            case "c010":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLEEDING] = 0;
                break;
            case "c011":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_SICK] = -1;
                break;
            case "c012":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_SICK] = 0;
                break;
            case "c013":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_FRACTURED] = -1;
                break;
            case "c014":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_FRACTURED] = 0;
                break;
            case "c015":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLIND] = -1;
                break;
            case "c016":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLIND] = 0;
                break;
            case "c063":
                playerInfo.getBuff()[BuffConstants.BUFF_CODE_RECOVERING]
                        = 15 * GamePalConstants.FRAME_PER_SECOND;
                break;
            default:
                break;
        }
        return getItem(world, userCode, itemNo, -1 * itemAmount);
    }
}
