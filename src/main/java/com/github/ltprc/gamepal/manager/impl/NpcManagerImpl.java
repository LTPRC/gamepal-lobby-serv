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
        PlayerInfo playerInfo = playerInfoFactory.createPlayerInfoInstance();
        playerInfo.setId(userCode);
        playerInfo.setPlayerType(GamePalConstants.PLAYER_TYPE_AI);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
        world.getPlayerInfoMap().put(userCode, playerInfo);
        userService.addUserIntoWorldMap(world, userCode);
        NpcBrain npcBrain = generateNpcBrain();
        world.getNpcBrainMap().put(userCode, npcBrain);
        return userCode;
    }

    @Override
    public void putNpc(String userCode, String npcUserCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerUtil.copyWorldCoordinate(playerInfoMap.get(userCode), playerInfoMap.get(npcUserCode));
        playerInfoMap.get(npcUserCode).setFaceDirection(BigDecimal.valueOf(Math.random() * 360D));
        playerInfoMap.get(npcUserCode).getCoordinate().setX(playerInfoMap.get(npcUserCode).getCoordinate().getX()
                .add(BigDecimal.valueOf(1 * Math.cos(playerInfoMap.get(userCode).getFaceDirection().doubleValue()
                        / 180 * Math.PI))));
        playerInfoMap.get(npcUserCode).getCoordinate().setY(playerInfoMap.get(npcUserCode).getCoordinate().getY()
                .subtract(BigDecimal.valueOf(1 * Math.sin(playerInfoMap.get(userCode).getFaceDirection().doubleValue()
                        / 180 * Math.PI))));
    }

    private NpcBrain generateNpcBrain() {
        NpcBrain npcBrain = new NpcBrain();
        npcBrain.setObserveTaskQueue(new PriorityQueue<>());
        npcBrain.setMoveTaskQueue(new PriorityQueue<>());
        npcBrain.setAttackTaskQueue(new PriorityQueue<>());
        return npcBrain;
    }
}
