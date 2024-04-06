package com.github.ltprc.gamepal.service;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.PlayerInfo;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PlayerService {

    ResponseEntity<String> setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute);

    ResponseEntity<String> updatePlayerInfo(String userCode, PlayerInfo playerInfo);

    ResponseEntity<String> updatePlayerInfoCharacter(String userCode, JSONObject req);

    ResponseEntity<String> updatePlayerMovement(String userCode, JSONObject req);

    ResponseEntity<String> generateNotificationMessage(String userCode, String content);

    Map<String, Integer> getRelationMapByUserCode(String userCode);

    ResponseEntity<String> useItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> getItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> getPreservedItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> damageHp(String userCode, String fromUserCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeHp(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeVp(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeHunger(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeThirst(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> interactBlocks(String userCode, int interactionCode, String id);

    ResponseEntity<String> updateBuff(String userCode);

    ResponseEntity<String> useSkill(String userCode, int skillNo, boolean isDown);

    ResponseEntity<String> setMember(String userCode, String nextUserCode);

    ResponseEntity<String> useTools(String userCode, String itemNo);

    ResponseEntity<String> useOutfits(String userCode, String itemNo);

    ResponseEntity<String> addDrop(String userCode, String itemNo, int amount);
}
