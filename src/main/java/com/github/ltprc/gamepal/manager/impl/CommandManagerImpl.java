package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.factory.CreatureFactory;
import com.github.ltprc.gamepal.manager.*;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;


@Component
public class CommandManagerImpl implements CommandManager {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserService userService;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private BuffManager buffManager;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private ItemManager itemManager;

    @Override
    public ResponseEntity<String> useCommand(String userCode, String commandContent) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        switch (commandContent) {
            case "nwcagents":
                playerService.generateNotificationMessage(userCode, "特工们，准备战斗。");
                for (int i = 0; i < 3; i++) {
                    Block agent = npcManager.putSpecificCreatureByRole(world, userCode,
                            new WorldCoordinate(player.getWorldCoordinate()), CreatureConstants.NPC_ROLE_MINION);
                    PlayerInfo playerInfo = world.getPlayerInfoMap().get(agent.getBlockInfo().getId());
                    CreatureFactory.randomlyPersonalizePlayerInfo(playerInfo, CreatureConstants.GENDER_MALE);
                    itemManager.getItem(world, agent.getBlockInfo().getId(), "o005", 1);
                    itemManager.useItem(world, agent.getBlockInfo().getId(), "o005", 1);
                    itemManager.getItem(world, agent.getBlockInfo().getId(), "t002", 1);
                    itemManager.useItem(world, agent.getBlockInfo().getId(), "t002", 1);
                    itemManager.getItem(world, agent.getBlockInfo().getId(), "a002", 7);
                }
                break;
            case "nwclotsofguns":
                playerService.generateNotificationMessage(userCode, "全副武装。");
                playerService.addDrop(userCode, "t000", 1);
                playerService.addDrop(userCode, "t105", 1);
                playerService.addDrop(userCode, "t200", 1);
                playerService.addDrop(userCode, "t201", 1);
                playerService.addDrop(userCode, "t206", 1);
                playerService.addDrop(userCode, "t208", 1);
                playerService.addDrop(userCode, "t209", 1);
                playerService.addDrop(userCode, "t210", 1);
                playerService.addDrop(userCode, "t214", 1);
                playerService.addDrop(userCode, "t220", 1);
                for (int i = 1; i <= 22; i++) {
                    playerService.addDrop(userCode, "a" + String.format("%03d", i), 100);
                }
                break;
            case "nwcneo":
                playerService.generateNotificationMessage(userCode, "你要战胜的是你自己。");
                PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
                playerInfo.setExp(playerInfo.getExpMax());
                break;
            case "nwctrinity":
                playerService.generateNotificationMessage(userCode, "枪在手，跟我走。");
                Block trinity = npcManager.putSpecificCreatureByRole(world, userCode,
                        new WorldCoordinate(player.getWorldCoordinate()), CreatureConstants.NPC_ROLE_PEER);
                playerInfo = world.getPlayerInfoMap().get(trinity.getBlockInfo().getId());
                CreatureFactory.randomlyPersonalizePlayerInfo(playerInfo, CreatureConstants.GENDER_FEMALE);
                itemManager.getItem(world, trinity.getBlockInfo().getId(), "o004", 1);
                itemManager.useItem(world, trinity.getBlockInfo().getId(), "o004", 1);
                itemManager.getItem(world, trinity.getBlockInfo().getId(), "t000", 1);
                itemManager.useItem(world, trinity.getBlockInfo().getId(), "t000", 1);
                itemManager.getItem(world, trinity.getBlockInfo().getId(), "a001", 20);
                break;
            case "nwcnebuchadnezzar":
                playerService.generateNotificationMessage(userCode, "跑得快。");
                playerInfo = world.getPlayerInfoMap().get(userCode);
                playerService.changeVp(userCode, playerInfo.getVpMax(), true);
                break;
            case "nwcmorpheus":
                playerService.generateNotificationMessage(userCode, "自由的灯塔在照耀我前进。");
                playerInfo = world.getPlayerInfoMap().get(userCode);
                buffManager.resetBuff(playerInfo);
                break;
            case "nwcwhatisthematrix":
                playerService.generateNotificationMessage(userCode, "认清现实吧。");
                playerInfo = world.getPlayerInfoMap().get(userCode);
                if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_REALISTIC] == 0) {
                    playerInfo.getBuff()[BuffConstants.BUFF_CODE_REALISTIC] = -1;
                } else {
                    playerInfo.getBuff()[BuffConstants.BUFF_CODE_REALISTIC] = 0;
                }
            case "nwcoracle":
                playerService.generateNotificationMessage(userCode, "先知带你看世界。");
                String animalUserCode = UUID.randomUUID().toString();
                Block animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                playerInfo = world.getPlayerInfoMap().get(animal.getBlockInfo().getId());
                playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                playerInfo.setSkinColor(CreatureConstants.SKIN_COLOR_TIGER);
                WorldCoordinate worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(
                                player.getWorldCoordinate().getCoordinate().getX().add(BigDecimal.valueOf(3)),
                                player.getWorldCoordinate().getCoordinate().getY(),
                                player.getWorldCoordinate().getCoordinate().getZ()));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                animalUserCode = UUID.randomUUID().toString();
                animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                playerInfo = world.getPlayerInfoMap().get(animal.getBlockInfo().getId());
                playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                playerInfo.setSkinColor(CreatureConstants.SKIN_COLOR_FOX);
                worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(
                                player.getWorldCoordinate().getCoordinate().getX(),
                                player.getWorldCoordinate().getCoordinate().getY().add(BigDecimal.valueOf(3)),
                                player.getWorldCoordinate().getCoordinate().getZ()));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                animalUserCode = UUID.randomUUID().toString();
                animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                playerInfo = world.getPlayerInfoMap().get(animal.getBlockInfo().getId());
                playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                playerInfo.setSkinColor(CreatureConstants.SKIN_COLOR_RACOON);
                worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(
                                player.getWorldCoordinate().getCoordinate().getX().subtract(BigDecimal.valueOf(3)),
                                player.getWorldCoordinate().getCoordinate().getY(),
                                player.getWorldCoordinate().getCoordinate().getZ()));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                animalUserCode = UUID.randomUUID().toString();
                animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                playerInfo = world.getPlayerInfoMap().get(animal.getBlockInfo().getId());
                playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                playerInfo.setSkinColor(CreatureConstants.SKIN_COLOR_SHEEP);
                worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(
                                player.getWorldCoordinate().getCoordinate().getX(),
                                player.getWorldCoordinate().getCoordinate().getY().subtract(BigDecimal.valueOf(3)),
                                player.getWorldCoordinate().getCoordinate().getZ()));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                break;
            case "nwcignoranceisbliss":
                playerService.generateNotificationMessage(userCode, "（暂无效果）");
                break;
            case "nwctheconstruct":
                playerService.generateNotificationMessage(userCode, "财源滚滚。");
                playerInfo = world.getPlayerInfoMap().get(userCode);
                playerInfo.setMoney(playerInfo.getMoney() + 100);
                break;
            case "nwcbluepill":
                playerService.generateNotificationMessage(userCode, "真香，嗝。");
                eventManager.changeHp(world, player, 0, true);
                break;
            case "nwcredpill":
                playerService.generateNotificationMessage(userCode, "我复活辣。");
                playerService.revivePlayer(userCode);
                break;
            case "nwcthereisnospoon":
                playerService.generateNotificationMessage(userCode, "我无敌辣。");
                playerInfo = world.getPlayerInfoMap().get(userCode);
                if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_INVINCIBLE] == 0) {
                    playerInfo.getBuff()[BuffConstants.BUFF_CODE_INVINCIBLE] = -1;
                } else {
                    playerInfo.getBuff()[BuffConstants.BUFF_CODE_INVINCIBLE] = 0;
                }
                break;
            case "nwczion":
                playerService.generateNotificationMessage(userCode, "欢迎回家。");
                movementManager.settleCoordinate(world, player,
                        world.getPlayerInfoMap().get(userCode).getRespawnPoint(), true);
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
