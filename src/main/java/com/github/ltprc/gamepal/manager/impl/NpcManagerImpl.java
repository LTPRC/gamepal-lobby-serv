package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.factory.PlayerInfoFactory;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.npc.NpcBrain;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

public class NpcManagerImpl implements NpcManager {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerInfoFactory playerInfoFactory;

    @Override
    public String createNpc(GameWorld world) {
        String userCode = UUID.randomUUID().toString();
        PlayerInfo npcPlayerInfo = playerInfoFactory.createPlayerInfoInstance();
        npcPlayerInfo.setId(userCode);
        npcPlayerInfo.setPlayerType(GamePalConstants.PLAYER_TYPE_AI);
        npcPlayerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        world.getPlayerInfoMap().put(userCode, npcPlayerInfo);
        userService.addUserIntoWorldMap(world, userCode);
        NpcBrain npcBrain = generateNpcBrain();
        world.getNpcBrainMap().put(userCode, npcBrain);
        return userCode;
    }

    @Override
    public void putNpc(String userCode, String npcUserCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        PlayerInfo npcPlayerInfo = playerInfoMap.get(npcUserCode);
        npcPlayerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
        PlayerUtil.copyWorldCoordinate(playerInfoMap.get(userCode), playerInfoMap.get(npcUserCode));
        npcPlayerInfo.setFaceDirection(BigDecimal.valueOf(Math.random() * 360D));
        npcPlayerInfo.getCoordinate().setX(playerInfo.getCoordinate().getX()
                .add(BigDecimal.valueOf(1 * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
        npcPlayerInfo.getCoordinate().setY(playerInfo.getCoordinate().getY()
                .subtract(BigDecimal.valueOf(1 * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
    }

    private NpcBrain generateNpcBrain() {
        NpcBrain npcBrain = new NpcBrain();
        npcBrain.setObserveTaskQueue(new PriorityQueue<>());
        npcBrain.setMoveTaskQueue(new PriorityQueue<>());
        npcBrain.setAttackTaskQueue(new PriorityQueue<>());
        return npcBrain;
    }
}
