package com.github.ltprc.gamepal.model.npc;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class NpcMoveTask implements INpcTask{

    @Autowired
    private UserService userService;

    @Override
    public int getNpcTaskType() {
        return GamePalConstants.NPC_TASK_TYPE_MOVE;
    }

    @Override
    public void runNpcTask(String npcUserCode, WorldCoordinate wc) {
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
        double distance = PlayerUtil.calculateDistance(
                world.getRegionMap().get(playerInfo.getRegionNo()), playerInfo, wc).doubleValue();
        double stopDistance = GamePalConstants.PLAYER_RADIUS.doubleValue() * 2;
        if (playerInfo.getRegionNo() != wc.getRegionNo() || distance <= stopDistance) {
            playerInfo.setFaceDirection(BigDecimal.ZERO);
            playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            return;
        }
        double newSpeed = Math.sqrt(Math.pow(playerInfo.getSpeed().getX().doubleValue(), 2)
                + Math.pow(playerInfo.getSpeed().getY().doubleValue(), 2)) + playerInfo.getAcceleration().doubleValue();
        double maxSpeed = playerInfo.getVp() > 0 ? playerInfo.getMaxSpeed().doubleValue()
                : playerInfo.getMaxSpeed().doubleValue() * 0.5D;
        newSpeed = Math.min(newSpeed, maxSpeed);
        newSpeed = Math.min(newSpeed, distance - stopDistance);
        playerInfo.setFaceDirection(PlayerUtil.calculateAngle(world.getRegionMap().get(playerInfo.getRegionNo()),
                playerInfo, wc));
        playerInfo.setSpeed(new Coordinate(BigDecimal.valueOf(
                newSpeed * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI)),
                BigDecimal.valueOf(-newSpeed * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
    }
}
