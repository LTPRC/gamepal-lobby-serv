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

                        // Change vp
                        int newVp = 10;
                        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SICK] != 0) {
                            newVp = 5;
                        }
                        Coordinate speed = playerInfoMap.get(userCode).getSpeed();
                        BigDecimal maxSpeed = playerInfoMap.get(userCode).getMaxSpeed();
                        newVp -= playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] != 0 ? 100 : 20 * Math.ceil(Math.sqrt(Math.pow(speed.getX().doubleValue(), 2)
                                + Math.pow(speed.getX().doubleValue(), 2)) / maxSpeed.doubleValue());
                        playerService.changeVp(userCode, newVp, false);

                        // Change hunger
                        randomNumber = Math.random();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            randomNumber *= 10;
                        }
                        if (randomNumber < 1000D / (7 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                            playerService.changeHunger(userCode, -1, false);
                        }

                        // Change thirst
                        randomNumber = Math.random();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            randomNumber *= 10;
                        }
                        if (randomNumber < 1000D / (3 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                            playerService.changeThirst(userCode, -1, false);
                        }

                        // Change view radius
                        BlockUtil.updatePerceptionInfo(playerInfoMap.get(userCode).getPerceptionInfo(),
                                world.getWorldTime());
                        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLIND] != 0) {
                            playerInfoMap.get(userCode).getPerceptionInfo().setDistinctVisionRadius(
                                    playerInfoMap.get(userCode).getPerceptionInfo().getDistinctVisionAngle()
                                            .divide(BigDecimal.TEN, RoundingMode.HALF_UP));
                            playerInfoMap.get(userCode).getPerceptionInfo().setIndistinctVisionRadius(
                                    playerInfoMap.get(userCode).getPerceptionInfo().getIndistinctVisionAngle()
                                            .divide(BigDecimal.TEN, RoundingMode.HALF_UP));
                        }

                        // Change skill remaining time
                        for (int i = 0; i < playerInfoMap.get(userCode).getSkill().length; i++) {
                            if (playerInfoMap.get(userCode).getSkill()[i].getFrame() > 0) {
                                playerInfoMap.get(userCode).getSkill()[i].setFrame(playerInfoMap.get(userCode).getSkill()[i].getFrame() - 1);
                            }
                        }
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
    public void executeBy1s() {
        // Update worldTime
        worldService.getWorldMap().forEach((key, value) -> worldService.updateWorldTime(value,
                GamePalConstants.UPDATED_WORLD_TIME_PER_SECOND / 10));
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

    @Scheduled(cron = "*/5 * * * * ?")
    public void executeBy5s() {
        for (Map.Entry<String, GameWorld> entry1 : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry1.getValue();
            if (!world.getNpcBrainMap().isEmpty()) {
                continue;
            }
            Map<String, Long> onlineMap = world.getOnlineMap();
            onlineMap.entrySet().stream()
                    .filter(entry2 -> world.getPlayerInfoMap().get(entry2.getKey()).getPlayerType()
                            == CreatureConstants.PLAYER_TYPE_HUMAN)
                    .filter(entry2 -> world.getPlayerInfoMap().get(entry2.getKey()).getPlayerStatus()
                            == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(entry2 -> {
                        String userCode = entry2.getKey();
                        String npcUserCode = UUID.randomUUID().toString();
                        PlayerInfo playerInfo =
                                npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC, npcUserCode);
                        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                        WorldCoordinate worldCoordinate = new WorldCoordinate();
                        BlockUtil.copyWorldCoordinate(world.getPlayerInfoMap().get(entry2.getKey()), worldCoordinate);
                        worldCoordinate.getCoordinate().setX(worldCoordinate.getCoordinate().getX()
                                .add(BigDecimal.valueOf(1 * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                        worldCoordinate.getCoordinate().setY(worldCoordinate.getCoordinate().getY()
                                .subtract(BigDecimal.valueOf(1 * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                        npcManager.putCreature(world, npcUserCode, worldCoordinate);
                        JSONObject behaviorRequest = new JSONObject();
                        behaviorRequest.put("userCode", npcUserCode);
                        behaviorRequest.put("npcBehaviorType", CreatureConstants.NPC_BEHAVIOR_FOLLOW);
                        behaviorRequest.put("targetUserCode", userCode);
                        npcManager.changeNpcBehavior(behaviorRequest);
                        world.getPlayerInfoMap().get(npcUserCode).setBossId(userCode);
                    });
        }
    }
}
