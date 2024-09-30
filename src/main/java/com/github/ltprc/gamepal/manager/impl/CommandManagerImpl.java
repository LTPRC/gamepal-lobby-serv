package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.factory.CreatureFactory;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.CommandManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
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
    private WorldService worldService;

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
                    Block agent = npcManager.putSpecificCreatureByRole(world,
                            userCode, player.getWorldCoordinate(), CreatureConstants.NPC_ROLE_MINION);
                    CreatureFactory.randomlyPersonalizePlayerInfo(agent.getPlayerInfo(), CreatureConstants.GENDER_MALE);
                    playerService.getItem(agent.getBlockInfo().getId(), "o005", 1);
                    playerService.useItem(agent.getBlockInfo().getId(), "o005", 1);
                    playerService.getItem(agent.getBlockInfo().getId(), "t002", 1);
                    playerService.useItem(agent.getBlockInfo().getId(), "t002", 1);
                    playerService.getItem(agent.getBlockInfo().getId(), "a002", 7);
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
                player.getPlayerInfo().setExp(player.getPlayerInfo().getExpMax());
                break;
            case "nwctrinity":
                playerService.generateNotificationMessage(userCode, "枪在手，跟我走。");
                Block trinity = npcManager.putSpecificCreatureByRole(world, userCode, player.getWorldCoordinate(),
                        CreatureConstants.NPC_ROLE_PEER);
                CreatureFactory.randomlyPersonalizePlayerInfo(trinity.getPlayerInfo(), CreatureConstants.GENDER_FEMALE);
                playerService.getItem(trinity.getBlockInfo().getId(), "o004", 1);
                playerService.useItem(trinity.getBlockInfo().getId(), "o004", 1);
                playerService.getItem(trinity.getBlockInfo().getId(), "t000", 1);
                playerService.useItem(trinity.getBlockInfo().getId(), "t000", 1);
                playerService.getItem(trinity.getBlockInfo().getId(), "a001", 20);
                break;
            case "nwcnebuchadnezzar":
                playerService.generateNotificationMessage(userCode, "跑得快。");
                playerService.changeVp(userCode, player.getPlayerInfo().getVpMax(), true);
                break;
            case "nwcmorpheus":
                playerService.generateNotificationMessage(userCode, "自由的灯塔在照耀我前进。");
                buffManager.resetBuff(player.getPlayerInfo());
                break;
            case "nwcwhatisthematrix":
                playerService.generateNotificationMessage(userCode, "认清现实吧。");
                if (player.getPlayerInfo().getBuff()[GamePalConstants.BUFF_CODE_REALISTIC] == 0) {
                    player.getPlayerInfo().getBuff()[GamePalConstants.BUFF_CODE_REALISTIC] = -1;
                } else {
                    player.getPlayerInfo().getBuff()[GamePalConstants.BUFF_CODE_REALISTIC] = 0;
                }
            case "nwcoracle":
                playerService.generateNotificationMessage(userCode, "先知带你看世界。");
                String animalUserCode = UUID.randomUUID().toString();
                Block animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                animal.getPlayerInfo().setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                animal.getPlayerInfo().setSkinColor(CreatureConstants.SKIN_COLOR_TIGER);
                WorldCoordinate worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(player.getWorldCoordinate().getCoordinate().getX().add(BigDecimal.valueOf(3)),
                                player.getWorldCoordinate().getCoordinate().getY()));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                animalUserCode = UUID.randomUUID().toString();
                animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                animal.getPlayerInfo().setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                animal.getPlayerInfo().setSkinColor(CreatureConstants.SKIN_COLOR_FOX);
                worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(player.getWorldCoordinate().getCoordinate().getX(),
                                player.getWorldCoordinate().getCoordinate().getY().add(BigDecimal.valueOf(3))));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                animalUserCode = UUID.randomUUID().toString();
                animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                animal.getPlayerInfo().setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                animal.getPlayerInfo().setSkinColor(CreatureConstants.SKIN_COLOR_RACOON);
                worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(player.getWorldCoordinate().getCoordinate().getX().subtract(BigDecimal.valueOf(3)),
                                player.getWorldCoordinate().getCoordinate().getY()));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                animalUserCode = UUID.randomUUID().toString();
                animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                animal.getPlayerInfo().setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                animal.getPlayerInfo().setSkinColor(CreatureConstants.SKIN_COLOR_SHEEP);
                worldCoordinate = new WorldCoordinate(player.getWorldCoordinate().getRegionNo(),
                        player.getWorldCoordinate().getSceneCoordinate(),
                        new Coordinate(player.getWorldCoordinate().getCoordinate().getX(),
                                player.getWorldCoordinate().getCoordinate().getY().subtract(BigDecimal.valueOf(3))));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                break;
            case "nwcignoranceisbliss":
                playerService.generateNotificationMessage(userCode, "（暂无效果）");
                break;
            case "nwctheconstruct":
                playerService.generateNotificationMessage(userCode, "财源滚滚。");
                player.getPlayerInfo().setMoney(player.getPlayerInfo().getMoney() + 100);
                break;
            case "nwcbluepill":
                playerService.generateNotificationMessage(userCode, "真香，嗝。");
                playerService.killPlayer(userCode);
                break;
            case "nwcredpill":
                playerService.generateNotificationMessage(userCode, "我复活辣。");
                playerService.revivePlayer(userCode);
                break;
            case "nwcthereisnospoon":
                playerService.generateNotificationMessage(userCode, "我无敌辣。");
                if (player.getPlayerInfo().getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] == 0) {
                    player.getPlayerInfo().getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] = -1;
                } else {
                    player.getPlayerInfo().getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] = 0;
                }
                break;
            case "nwczion":
                playerService.generateNotificationMessage(userCode, "欢迎回家。");
                worldService.expandByCoordinate(world, player.getWorldCoordinate(), GamePalConstants.DEFAULT_BIRTHPLACE, 1);
                movementManager.settleCoordinate(world, player, GamePalConstants.DEFAULT_BIRTHPLACE);
                world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_MOVEMENT] = true;
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
