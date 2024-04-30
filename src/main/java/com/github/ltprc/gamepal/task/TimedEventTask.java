package com.github.ltprc.gamepal.task;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldEvent;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

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

    @Scheduled(fixedRate = 40)
    public void executeByFrame() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();

            // Update events
            worldService.updateEvents(world);

            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();

            // Count buff remaining time
            onlineMap.entrySet().stream()
                    .filter(entry2 -> playerInfoMap.containsKey(entry2.getKey())
                            && playerInfoMap.get(entry2.getKey()).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(entry2 -> {
                        String userCode = entry2.getKey();
                        PlayerInfo playerInfo = playerInfoMap.get(userCode);
                        for (int i = 0; i < GamePalConstants.BUFF_CODE_LENGTH; i++) {
                            if (playerInfo.getBuff()[i] <= 0) {
                                continue;
                            }
                            playerInfo.getBuff()[i] = playerInfo.getBuff()[i] - 1;
                            if (i == GamePalConstants.BUFF_CODE_DEAD) {
                                if (playerInfo.getBuff()[i] == 0) {
                                    playerService.changeHp(userCode, playerInfo.getHpMax(), true);
                                    playerService.changeVp(userCode, playerInfo.getVpMax(), true);
                                    playerService.changeHunger(userCode, playerInfo.getHungerMax(), true);
                                    playerService.changeThirst(userCode, playerInfo.getThirstMax(), true);
                                    playerService.generateNotificationMessage(userCode, "复活成功。");
                                    WorldEvent worldEvent = BlockUtil.createWorldEvent(playerInfo.getId(),
                                            GamePalConstants.EVENT_CODE_SACRIFICE, playerInfo);
                                    userService.getWorldByUserCode(playerInfo.getId()).getEventQueue().add(worldEvent);
                                    world.getEventQueue().add(new WorldEvent());
                                } else if (playerInfo.getBuff()[i] % GamePalConstants.FRAME_PER_SECOND == 0) {
                                    playerService.generateNotificationMessage(userCode, "距离复活还有"
                                            + playerInfo.getBuff()[i] / GamePalConstants.FRAME_PER_SECOND + "秒。");
                                }
                            }
                        }
                    });

            onlineMap.entrySet().stream()
                    .filter(entry2 -> playerInfoMap.containsKey(entry2.getKey())
                            && SkillUtil.validateDamage(playerInfoMap.get(entry2.getKey())))
                    .forEach(entry2 -> {
                        // TODO Activate buff if necessary

                        double randomNumber;

                        // Change hp
//                        playerService.changeHp(entry2.getKey(), -10, false);

                        // Change vp
                        int newVp = 1;
                        Coordinate speed = playerInfoMap.get(entry2.getKey()).getSpeed();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            newVp = -1;
                        }
                        playerService.changeVp(entry2.getKey(), newVp, false);

                        // Change hunger
                        randomNumber = Math.random();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            randomNumber *= 10;
                        }
                        if (randomNumber < 1000D / (7 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                            playerService.changeHunger(entry2.getKey(), -1, false);
                        }

                        // Change thirst
                        randomNumber = Math.random();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            randomNumber *= 10;
                        }
                        if (randomNumber < 1000D / (3 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                            playerService.changeThirst(entry2.getKey(), -1, false);
                        }

                        // Change skill remaining time
                        for (int i = 0; i < playerInfoMap.get(entry2.getKey()).getSkill().length; i++) {
                            if (playerInfoMap.get(entry2.getKey()).getSkill()[i].getFrame() > 0) {
                                playerInfoMap.get(entry2.getKey()).getSkill()[i].setFrame(playerInfoMap.get(entry2.getKey()).getSkill()[i].getFrame() - 1);
                            }
                        }

                        // Change view radius
                        BlockUtil.updatePerceptionInfo(playerInfoMap.get(entry2.getKey()).getPerceptionInfo(),
                                world.getWorldTime());

                        // Update buff
                        playerService.updateBuff(entry2.getKey());
                    });

            // Other movements
            npcManager.updateNpcBrains(world);
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
                        String npcUserCode = npcManager.createNpc(world);
                        npcManager.putNpc(userCode, npcUserCode);
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
