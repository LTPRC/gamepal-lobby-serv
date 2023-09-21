package com.github.ltprc.gamepal.service;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.terminal.Terminal;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

public interface PlayerService {

    ResponseEntity setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute);

    ResponseEntity updateplayerinfoCharacter(String userCode, JSONObject req);

    ResponseEntity updateMovingBlock(String userCode, JSONObject req);

    ResponseEntity getPlayerInfo(HttpServletRequest request);

    Map<String, PlayerInfo> getPlayerInfoMap();

    Map<String, Integer> getRelationMapByUserCode(String userCode);

    Set<String> getFlagSet();

    Map<String, Terminal> getTerminalMap();

    ResponseEntity useItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity getItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity getPreservedItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity changeHp(String userCode, int value, boolean isAbsolute);

    ResponseEntity changeVp(String userCode, int value, boolean isAbsolute);

    ResponseEntity changeHunger(String userCode, int value, boolean isAbsolute);

    ResponseEntity changeThirst(String userCode, int value, boolean isAbsolute);

    ResponseEntity interactBlocks(String userCode, int interactionCode, String id);
}
