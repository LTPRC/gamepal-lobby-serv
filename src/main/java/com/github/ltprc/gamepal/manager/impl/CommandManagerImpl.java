package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.CommandManager;
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
                playerService.generateNotificationMessage(userCode, "");
                break;
            case "nwclotsofguns":
                break;
            case "nwcneo":
                playerService.generateNotificationMessage(userCode, "见得多啦。");
                playerInfo.setExp(playerInfo.getExpMax());
                break;
            case "nwctrinity":
                break;
            case "nwcnebuchadnezzar":
                break;
            case "nwcmorpheus":
                break;
            case "nwcoracle":
                break;
            case "nwcwhatisthematrix":
                break;
            case "nwcignoranceisbliss":
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
                BlockUtil.copyWorldCoordinate(GamePalConstants.DEFAULT_BIRTHPLACE, playerInfo);
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
