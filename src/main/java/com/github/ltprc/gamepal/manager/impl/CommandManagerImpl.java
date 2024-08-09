package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.factory.CreatureFactory;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.CommandManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;


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

    @Override
    public ResponseEntity<String> useCommand(String userCode, String commandContent) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        switch (commandContent) {
            case "nwcagents":
                playerService.generateNotificationMessage(userCode, "特工们，准备战斗。");
                for (int i = 0; i < 3; i++) {
                    PlayerInfo agentInfo = npcManager.putSpecificCreatureByRole(world, userCode, playerInfo,
                            CreatureConstants.NPC_ROLE_MINION);
                    CreatureFactory.randomlyPersonalizePlayerInfo(agentInfo, CreatureConstants.GENDER_MALE);
                    playerService.getItem(agentInfo.getId(), "a005", 1);
                    playerService.useItem(agentInfo.getId(), "a005", 1);
                    playerService.getItem(agentInfo.getId(), "t002", 1);
                    playerService.useItem(agentInfo.getId(), "t002", 1);
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
                break;
            case "nwcneo":
                playerService.generateNotificationMessage(userCode, "你要战胜的是你自己。");
                playerInfo.setExp(playerInfo.getExpMax());
                break;
            case "nwctrinity":
                playerService.generateNotificationMessage(userCode, "枪在手，跟我走。");
                PlayerInfo trinityInfo = npcManager.putSpecificCreatureByRole(world, userCode, playerInfo,
                        CreatureConstants.NPC_ROLE_PEER);
                CreatureFactory.randomlyPersonalizePlayerInfo(trinityInfo, CreatureConstants.GENDER_FEMALE);
                playerService.getItem(trinityInfo.getId(), "a004", 1);
                playerService.useItem(trinityInfo.getId(), "a004", 1);
                playerService.getItem(trinityInfo.getId(), "t000", 1);
                playerService.useItem(trinityInfo.getId(), "t000", 1);
                break;
            case "nwcnebuchadnezzar":
                playerService.generateNotificationMessage(userCode, "跑得快。");
                playerService.changeVp(userCode, playerInfo.getVpMax(), true);
                break;
            case "nwcmorpheus":
                playerService.generateNotificationMessage(userCode, "自由的灯塔在照耀我前进。");
                buffManager.resetBuff(playerInfo);
                break;
            case "nwcoracle":
            case "nwcwhatisthematrix":
            case "nwcignoranceisbliss":
                playerService.generateNotificationMessage(userCode, "（暂无效果）");
                break;
            case "nwctheconstruct":
                playerService.generateNotificationMessage(userCode, "财源滚滚。");
                playerInfo.setMoney(playerInfo.getMoney() + 100);
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
                if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] == 0) {
                    playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] = -1;
                } else {
                    playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] = 0;
                }
                break;
            case "nwczion":
                playerService.generateNotificationMessage(userCode, "欢迎回家。");
                movementManager.settleCoordinate(world, playerInfo, GamePalConstants.DEFAULT_BIRTHPLACE, true);
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
