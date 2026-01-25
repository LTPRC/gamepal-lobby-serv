package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.config.InteractionConstants;
import com.github.ltprc.gamepal.manager.FarmManager;
import com.github.ltprc.gamepal.manager.InteractionManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.FarmInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.InteractionInfo;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class InteractionManagerImpl implements InteractionManager {

    private static final Log logger = LogFactory.getLog(InteractionManagerImpl.class);
    private static final Random random = new Random();

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private FarmManager farmManager;

    @Autowired
    private WorldService worldService;

    @Override
    public void focusOnBlock(GameWorld world, String userCode, Block block) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        if (null == block) {
            world.getInteractionInfoMap().remove(userCode);
            return;
        }
        Block player = creatureMap.get(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_STUNNED] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
            world.getInteractionInfoMap().remove(userCode);
            return;
        }
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        if (checkFocusOnBlock(region, player, block)) {
            world.getInteractionInfoMap().put(userCode, generateInteractionInfo(world, block.getBlockInfo()));
        }
    }

    private boolean checkFocusOnBlock(Region region, Block player, Block block) {
        BigDecimal distance = BlockUtil.calculateDistance(region, player.getWorldCoordinate(),
                block.getWorldCoordinate());
        BigDecimal angle = BlockUtil.calculateAngle(region, player.getWorldCoordinate(), block.getWorldCoordinate());
        return BlockUtil.checkBlockTypeInteractive(block.getBlockInfo().getType())
                && !StringUtils.equals(block.getBlockInfo().getId(), player.getBlockInfo().getId())
                && null != distance
                && distance.doubleValue() < InteractionConstants.MAX_INTERACTION_DISTANCE.doubleValue()
                && null != angle
                && BlockUtil.compareAnglesInDegrees(angle.doubleValue(),
                player.getMovementInfo().getFaceDirection().doubleValue())
                < InteractionConstants.MAX_INTERACTION_ANGLE.doubleValue();
    }

    private InteractionInfo generateInteractionInfo(GameWorld world, BlockInfo blockInfo) {
        InteractionInfo interactionInfo = new InteractionInfo();
        interactionInfo.setType(blockInfo.getType());
        interactionInfo.setId(blockInfo.getId());
        interactionInfo.setCode(blockInfo.getCode());
        // Keep syncing with Frontend
        List<Integer> list = new ArrayList<>();
        switch (blockInfo.getType()) {
            case BlockConstants.BLOCK_TYPE_PLAYER:
                Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
                if (!playerInfoMap.containsKey(blockInfo.getId())) {
                    logger.error(ErrorUtil.ERROR_1007);
                    break;
                }
                PlayerInfo playerInfo = playerInfoMap.get(blockInfo.getId());
                if (playerService.validateActiveness(world, blockInfo.getId())) {
                    if (CreatureConstants.CREATURE_TYPE_HUMAN == playerInfo.getCreatureType()) {
                        list.add(InteractionConstants.INTERACTION_TALK);
                        list.add(InteractionConstants.INTERACTION_SUCCUMB);
                        list.add(InteractionConstants.INTERACTION_EXPEL);
                    }
                    if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
                        list.add(InteractionConstants.INTERACTION_PULL);
                    }
                }
                break;
            case BlockConstants.BLOCK_TYPE_BED:
                list.add(InteractionConstants.INTERACTION_SLEEP);
                list.add(InteractionConstants.INTERACTION_PACK);
                break;
            case BlockConstants.BLOCK_TYPE_TOILET:
                list.add(InteractionConstants.INTERACTION_DRINK);
                list.add(InteractionConstants.INTERACTION_PACK);
                break;
            case BlockConstants.BLOCK_TYPE_DRESSER:
                list.add(InteractionConstants.INTERACTION_SET);
                list.add(InteractionConstants.INTERACTION_PACK);
                break;
            case BlockConstants.BLOCK_TYPE_GAME:
                list.add(InteractionConstants.INTERACTION_USE);
                break;
            case BlockConstants.BLOCK_TYPE_STORAGE:
            case BlockConstants.BLOCK_TYPE_CONTAINER:
            case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
            case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                list.add(InteractionConstants.INTERACTION_EXCHANGE);
                list.add(InteractionConstants.INTERACTION_PACK);
                break;
            case BlockConstants.BLOCK_TYPE_COOKER:
            case BlockConstants.BLOCK_TYPE_WORKSHOP:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                list.add(InteractionConstants.INTERACTION_USE);
                list.add(InteractionConstants.INTERACTION_PACK);
                break;
            case BlockConstants.BLOCK_TYPE_SINK:
                list.add(InteractionConstants.INTERACTION_USE);
                list.add(InteractionConstants.INTERACTION_DRINK);
                list.add(InteractionConstants.INTERACTION_PACK);
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                if (!world.getFarmMap().containsKey(blockInfo.getId())) {
                    logger.error(ErrorUtil.ERROR_1014);
                    break;
                }
                FarmInfo farmInfo = world.getFarmMap().get(blockInfo.getId());
                if (farmInfo.getCropStatus() == BlockConstants.CROP_STATUS_NONE
                        || farmInfo.getCropStatus() == BlockConstants.CROP_STATUS_GATHERED) {
                    list.add(InteractionConstants.INTERACTION_PLANT);
                } else if (farmInfo.getCropStatus() == BlockConstants.CROP_STATUS_MATURE) {
                    list.add(InteractionConstants.INTERACTION_GATHER);
                }
                break;
        }
        interactionInfo.setList(list);
        return interactionInfo;
    }

    /**
     * Not all cases need to be implemented
     * @param userCode
     * @param interactionCode
     * @return
     */
    @Override
    public void interactBlocks(GameWorld world, String userCode, int interactionCode) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
            playerService.generateNotificationMessage(userCode, "濒死状态无法进行交互！");
            logger.error(ErrorUtil.ERROR_1043);
            return;
        }
        InteractionInfo interactionInfo = world.getInteractionInfoMap().get(userCode);
        if (null == interactionInfo) {
            logger.error(ErrorUtil.ERROR_1034);
            return;
        }
        String id = interactionInfo.getId();
        Block block;
        switch (interactionCode) {
            case InteractionConstants.INTERACTION_TALK:
            case InteractionConstants.INTERACTION_ATTACK:
            case InteractionConstants.INTERACTION_FLIRT:
            case InteractionConstants.INTERACTION_SUCCUMB:
            case InteractionConstants.INTERACTION_EXPEL:
            case InteractionConstants.INTERACTION_PULL:
                block = creatureMap.get(id);
                break;
            default:
                block = world.getBlockMap().get(id);
                break;
        }
        if (null == block) {
            logger.error(ErrorUtil.ERROR_1012);
            return;
        }
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        switch (interactionCode) {
            case InteractionConstants.INTERACTION_USE:
                switch (block.getBlockInfo().getType()) {
                    case BlockConstants.BLOCK_TYPE_TOILET:
                        playerService.generateNotificationMessage(userCode, "你正在使用马桶。");
                        break;
                    case BlockConstants.BLOCK_TYPE_GAME:
//                        playerService.generateNotificationMessage(userCode, "你开启了桌游。");
//                        if (!world.getTerminalMap().containsKey(id)) {
//                            GameTerminal gameTerminal = new GameTerminal(world);
//                            gameTerminal.setId(id);
//                            gameTerminal.setUserCode(userCode);
//                            gameTerminal.setStatus(GameConstants.GAME_PLAYER_STATUS_START);
//                            gameTerminal.setOutputs(new ArrayList<>());
//                            world.getTerminalMap().put(id, gameTerminal);
//                        }
//                        stateMachineService.gameTerminalInput((GameTerminal) world.getTerminalMap().get(id), "1");
                        break;
                    case BlockConstants.BLOCK_TYPE_COOKER:
                        playerService.generateNotificationMessage(userCode, "你正在使用灶台。");
                        break;
                    case BlockConstants.BLOCK_TYPE_SINK:
                        playerService.generateNotificationMessage(userCode, "你正在使用饮水台。");
                        break;
                    case BlockConstants.BLOCK_TYPE_WORKSHOP:
                        playerService.generateNotificationMessage(userCode, "你正在使用工作台。");
                        break;
                    case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
                        playerService.generateNotificationMessage(userCode, "你正在使用工具工坊。");
                        break;
                    case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
                        playerService.generateNotificationMessage(userCode, "你正在使用弹药工坊。");
                        break;
                    case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
                        playerService.generateNotificationMessage(userCode, "你正在使用服装工坊。");
                        break;
                    case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
                        playerService.generateNotificationMessage(userCode, "你正在使用化学工坊。");
                        break;
                    case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                        playerService.generateNotificationMessage(userCode, "你正在使用回收站。");
                        break;
                    default:
                        break;
                }
                break;
            case InteractionConstants.INTERACTION_EXCHANGE:
                switch (block.getBlockInfo().getType()) {
                    case BlockConstants.BLOCK_TYPE_STORAGE:
                        playerService.generateNotificationMessage(userCode, "你正在整理个人物品。");
                        break;
                    case BlockConstants.BLOCK_TYPE_CONTAINER:
                        playerService.generateNotificationMessage(userCode, "你正在整理容器。");
                        break;
                    case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
                        playerService.generateNotificationMessage(userCode, "你正在整理人类躯体。");
                        break;
                    case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                        playerService.generateNotificationMessage(userCode, "你正在整理动物躯体。");
                        break;
                    default:
                        break;
                }
                break;
            case InteractionConstants.INTERACTION_SLEEP:
                playerInfo.setVp(playerInfo.getVpMax());
                playerInfo.setRespawnPoint(block.getWorldCoordinate());
                playerService.generateNotificationMessage(userCode, "你打了一个盹。");
                break;
            case InteractionConstants.INTERACTION_DRINK:
                playerInfo.setThirst(playerInfo.getThirstMax());
                playerService.generateNotificationMessage(userCode, "你痛饮了起来。");
                break;
            case InteractionConstants.INTERACTION_DECOMPOSE:
                break;
            case InteractionConstants.INTERACTION_SET:
                playerService.generateNotificationMessage(userCode, "你捯饬了起来。");
                break;
            case InteractionConstants.INTERACTION_PACK:
                Block packedDrop = sceneManager.addDropBlock(world, block.getWorldCoordinate(),
                        new AbstractMap.SimpleEntry<>(BlockUtil.convertBlockInfo2ItemNo(block.getBlockInfo()), 1));
                movementManager.speedUpBlock(world, packedDrop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                        new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                        BlockConstants.DROP_THROW_RADIUS));
                sceneManager.removeBlock(world, block, false);
                break;
            case InteractionConstants.INTERACTION_PLANT:
                farmManager.plant(world, userCode, id, "c064");
                break;
            case InteractionConstants.INTERACTION_GATHER:
                farmManager.gather(world, userCode, id);
                break;
            case InteractionConstants.INTERACTION_PULL:
                playerService.pullPlayer(userCode, id);
                break;
            default:
                break;
        }
    }
}
