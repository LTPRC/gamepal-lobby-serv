package com.github.ltprc.gamepal.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PlayerService {

    ResponseEntity<String> setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute);

    ResponseEntity<String> updatePlayerInfo(String userCode, JSONObject req);

    ResponseEntity<String> updatePlayerInfoCharacter(String userCode, JSONObject req);

    ResponseEntity<String> updatePlayerMovement(String userCode, JSONObject req);

    ResponseEntity<String> generateNotificationMessage(String userCode, String content);

    Map<String, Integer> getRelationMapByUserCode(String userCode);

    ResponseEntity<String> useItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> getItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> getPreservedItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> useRecipe(String userCode, String recipeNo, int recipeAmount);

    ResponseEntity<String> damageHp(String userCode, String fromUserCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeHp(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeVp(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeHunger(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeThirst(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> interactBlocks(String userCode, int interactionCode, String id);

    ResponseEntity<String> updateBuff(String userCode);

    ResponseEntity<String> useSkill(String userCode, int skillNo, boolean isDown);

    ResponseEntity<String> setMember(String userCode, String userCode1, String userCode2);

    String findTopBossId(final String userCode);

    ResponseEntity<String> useTools(String userCode, String itemNo);

    ResponseEntity<String> useOutfits(String userCode, String itemNo);

    ResponseEntity<String> addDrop(String userCode, String itemNo, int amount);
}
