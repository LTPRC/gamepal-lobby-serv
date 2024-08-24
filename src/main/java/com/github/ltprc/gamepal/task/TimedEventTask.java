package com.github.ltprc.gamepal.task;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.world.WorldEvent;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class TimedEventTask {

    @Autowired
    private WorldService worldService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private BuffManager buffManager;

    @Scheduled(fixedRate = 40)
    public void executeByFrame() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();

            // Update events
            worldService.updateEvents(world);

            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();

            onlineMap.keySet().stream()
                    .filter(playerInfoMap::containsKey)
                    .filter(userCode -> playerInfoMap.get(userCode).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(userCode -> {
                        buffManager.updateBuffTime(world, userCode);
                        buffManager.changeBuff(world, userCode);
                    });

            onlineMap.keySet().stream()
                    .filter(userCode -> SkillUtil.validateActiveness(playerInfoMap.get(userCode)))
                    .forEach(userCode -> {
                        PlayerInfo playerInfo = playerInfoMap.get(userCode);
                        double randomNumber;

                        // Change hp
                        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLEEDING] != 0) {
                            playerService.changeHp(userCode, -1, false);
                        }
                        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_RECOVERING] != 0) {
                            playerService.changeHp(userCode, 1, false);
                        }

                        // Change vp
                        int newVp = 10;
                        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SICK] != 0) {
                            newVp -= 5;
                        }
                        Coordinate speed = playerInfo.getSpeed();
                        BigDecimal maxSpeed = playerInfo.getMaxSpeed();
                        int vpFracturedFactor = playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] != 0
                                ? 150 : 15;
                        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] != 0) {
                            newVp -= speed.getX().equals(BigDecimal.ZERO) && speed.getY().equals(BigDecimal.ZERO)
                                    ? 0 : vpFracturedFactor;
                        } else {
                            newVp -= Math.floor(vpFracturedFactor * Math.sqrt(Math.pow(speed.getX().doubleValue(), 2)
                                    + Math.pow(speed.getY().doubleValue(), 2)) / maxSpeed.doubleValue());
                        }
                        if (newVp < 0 && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] != 0) {
                            newVp *= 2;
                        } else if (newVp > 0 && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] != 0) {
                            newVp /= 2;
                        }
                        playerService.changeVp(userCode, newVp, false);

                        // Change hunger
                        randomNumber = Math.random();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            randomNumber *= 10;
                        }
                        if (randomNumber < 1000D / (7 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                            playerService.changeHunger(userCode, -1, false);
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] != 0) {
                                playerService.changeHp(userCode, -1, false);
                            }
                        }

                        // Change thirst
                        randomNumber = Math.random();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            randomNumber *= 10;
                        }
                        if (randomNumber < 1000D / (3 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                            playerService.changeThirst(userCode, -1, false);
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] != 0) {
                                playerService.changeHp(userCode, -1, false);
                            }
                        }

                        // Change precision
                        playerService.changePrecision(userCode, 50 - 100
                                * (int) (Math.sqrt(Math.pow(playerInfo.getSpeed().getX().doubleValue(), 2)
                                + Math.pow(playerInfo.getSpeed().getY().doubleValue(), 2))
                                / playerInfo.getMaxSpeed().doubleValue()), false);

                        // Change view radius
                        BlockUtil.updatePerceptionInfo(playerInfo.getPerceptionInfo(), world.getWorldTime());
                        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLIND] != 0) {
                            playerInfo.getPerceptionInfo().setDistinctVisionRadius(
                                    playerInfo.getPerceptionInfo().getDistinctVisionRadius()
                                            .divide(BigDecimal.TEN, RoundingMode.HALF_UP));
                            playerInfo.getPerceptionInfo().setIndistinctVisionRadius(
                                    playerInfo.getPerceptionInfo().getIndistinctVisionRadius()
                                            .divide(BigDecimal.TEN, RoundingMode.HALF_UP));
                        }

                        // Change skill remaining time
                        for (int i = 0; i < playerInfo.getSkill().length; i++) {
                            if (null != playerInfo.getSkill()[i] && playerInfo.getSkill()[i].getFrame() > 0) {
                                playerInfo.getSkill()[i].setFrame(playerInfo.getSkill()[i].getFrame() - 1);
                            }
                        }

                        // Check level-up
                        playerService.checkLevelUp(userCode);
                    });

            // NPC movements
            npcManager.updateNpcBrains(world);

            // Buff changing
            onlineMap.keySet().stream()
                    .filter(playerInfoMap::containsKey)
                    .filter(userCode -> playerInfoMap.get(userCode).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(userCode -> buffManager.changeBuff(world, userCode));
        }
    }

    @Scheduled(fixedRate = 100)
    public void executeBy100ms() {
        // Update worldTime
        worldService.getWorldMap().forEach((key, value) -> worldService.updateWorldTime(value,
                GamePalConstants.UPDATED_WORLD_TIME_PER_SECOND / 10));
    }

    @Scheduled(cron = "* * * * * ?")
    public void executeBy1s() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
            onlineMap.keySet().stream()
                    .filter(userCode -> SkillUtil.validateActiveness(playerInfoMap.get(userCode)))
                    .forEach(userCode -> {
                        PlayerInfo playerInfo = playerInfoMap.get(userCode);

                        // Add footstep
                        if (Math.pow(playerInfo.getSpeed().getX().doubleValue(), 2)
                                + Math.pow(playerInfo.getSpeed().getY().doubleValue(), 2)
                                > Math.pow(playerInfo.getMaxSpeed().doubleValue(), 2)) {
                            WorldEvent worldEvent = BlockUtil.createWorldEvent(userCode,
                                    GamePalConstants.EVENT_CODE_FOOTSTEP, playerInfo);
                            world.getEventQueue().add(worldEvent);
                        }
                    });
        }
    }

    /**
     * This method is used for checking idle user which is not under anyone's control.
     * All worlds are to be checked. 23/08/28
     */
    @Scheduled(cron = "* */2 * * * ?")
    public void executeBy120s() {
        long timestamp = Instant.now().getEpochSecond();
        for (Map.Entry<String, GameWorld> entry1 : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry1.getValue();
            world.getOnlineMap().forEach((key, value) -> {
                PlayerInfo playerInfo = world.getPlayerInfoMap().get(key);
                if (playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN
                        && timestamp - value > GamePalConstants.ONLINE_TIMEOUT_SECOND) {
                    userService.logoff(key, "", false);
                }
            });
        }
    }
}
