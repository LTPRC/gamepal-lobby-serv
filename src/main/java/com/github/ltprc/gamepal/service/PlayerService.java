package com.github.ltprc.gamepal.service;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.map.InteractionInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PlayerService {

    ResponseEntity<String> updatePlayerInfoCharacter(String userCode, JSONObject req);

    ResponseEntity<String> updatePlayerMovement(String userCode, WorldCoordinate worldCoordinate,
                                                MovementInfo movementInfo);

    ResponseEntity<String> generateNotificationMessage(String userCode, String content);

    ResponseEntity<String> setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute);

    Map<String, Integer> getRelationMapByUserCode(String userCode);

    ResponseEntity<String> useItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> getItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> getPreservedItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> getInteractedItem(String userCode, String itemNo, int itemAmount);

    ResponseEntity<String> useRecipe(String userCode, String recipeNo, int recipeAmount);

    ResponseEntity<String> changeVp(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeHunger(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changeThirst(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> changePrecision(String userCode, int value, boolean isAbsolute);

    ResponseEntity<String> interactBlocks(String userCode, int interactionCode);

    ResponseEntity<String> useSkill(String userCode, int skillNo, boolean isDown);

    ResponseEntity<String> setMember(String userCode, String userCode1, String userCode2);

    String findTopBossId(final String userCode);

    ResponseEntity<String> useTools(String userCode, String itemNo);

    ResponseEntity<String> useOutfits(String userCode, String itemNo);

    ResponseEntity<String> addDrop(String userCode, String itemNo, int amount);

    ResponseEntity<String> useDrop(String userCode, String dropId);

    ResponseEntity<String> updateInteractionInfo(String userCode, InteractionInfo interactionInfo);

    ResponseEntity<String> killPlayer(String userCode);

    ResponseEntity<String> revivePlayer(String userCode);

    ResponseEntity<String> addPlayerTrophy(String userCode, boolean hasTrophy);

    ResponseEntity<String> destroyPlayer(String userCode);

    ResponseEntity<String> checkLevelUp(String userCode);

    ResponseEntity<String> updateSkillsByTool(String userCode);

    boolean validateActiveness(final GameWorld world, final String id);
}
