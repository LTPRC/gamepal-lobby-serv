package com.github.ltprc.gamepal.service;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.InteractionInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PlayerService {

    ResponseEntity<String> updatePlayerInfoCharacter(String userCode, JSONObject req);

    ResponseEntity<String> generateNotificationMessage(String userCode, String content);

    ResponseEntity<String> setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute);

    Map<String, Integer> getRelationMapByUserCode(String userCode);

    ResponseEntity<String> changeVp(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeHunger(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeThirst(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changePrecision(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> interactBlocks(String userCode, int interactionCode);

    ResponseEntity<String> useSkill(String userCode, int skillNo, boolean isDown);

    ResponseEntity<String> setMember(String userCode, String userCode1, String userCode2);

    String findTopBossId(final String userCode);

    ResponseEntity<String> addDrop(String userCode, String itemNo, int amount);

    ResponseEntity<String> useDrop(String userCode, String dropId);

    ResponseEntity<String> updateInteractionInfo(String userCode, InteractionInfo interactionInfo);

    ResponseEntity<String> knockPlayer(String userCode);

    ResponseEntity<String> killPlayer(String userCode);

    ResponseEntity<String> pullPlayer(String fromUserCode, String toUserCode);

    ResponseEntity<String> revivePlayer(String userCode);

    ResponseEntity<String> addPlayerTrophy(String userCode, boolean hasTrophy);

    ResponseEntity<String> destroyPlayer(String userCode);

    ResponseEntity<String> addExp(String userCode, int expVal);

    ResponseEntity<String> updateSkillsByTool(String userCode);

    boolean validateActiveness(final GameWorld world, final String id);

    void updateTimestamp(PlayerInfo playerInfo);
}
