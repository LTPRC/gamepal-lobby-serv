package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.manager.CommandManager;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.ContentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class CommandManagerImpl implements CommandManager {

    @Autowired
    private PlayerService playerService;

    @Override
    public ResponseEntity<String> useCommand(String userCode, String commandContent) {
        JSONObject rst = ContentUtil.generateRst();
        switch (commandContent) {
            case "nwcagents":
                playerService.generateNotificationMessage(userCode, "");
                break;
            case "nwclotsofguns":
                break;
            case "nwcneo":
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
                break;
            case "nwcbluepill":
                break;
            case "nwcredpill":
                break;
            case "nwcthereisnospoon":
                break;
            case "nwczion":
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
