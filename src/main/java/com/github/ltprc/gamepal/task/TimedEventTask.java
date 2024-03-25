package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

import static com.github.ltprc.gamepal.config.GamePalConstants.FRAME_PER_SECOND;

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
                        for (int i = 0; i < GamePalConstants.BUFF_CODE_LENGTH; i++) {
                            if (playerInfoMap.get(userCode).getBuff()[i] <= 0) {
                                continue;
                            }
                            playerInfoMap.get(userCode).getBuff()[i] = playerInfoMap.get(userCode).getBuff()[i] - 1;
                            if (i == GamePalConstants.BUFF_CODE_DEAD) {
                                if (playerInfoMap.get(userCode).getBuff()[i] == 0) {
                                    playerService.changeHp(userCode, playerInfoMap.get(userCode).getHpMax(), true);
                                    playerService.changeVp(userCode, playerInfoMap.get(userCode).getVpMax(), true);
                                    playerService.changeHunger(userCode, playerInfoMap.get(userCode).getHungerMax(), true);
                                    playerService.changeThirst(userCode, playerInfoMap.get(userCode).getThirstMax(), true);
                                    playerService.generateNotificationMessage(userCode, "复活成功。");
                                    WorldBlock rebirthEventBlock = playerService.generateEventByUserCode(userCode);
                                    rebirthEventBlock.setType(GamePalConstants.EVENT_CODE_SACRIFICE);
                                    worldService.addEvent(userCode, rebirthEventBlock);
                                } else if (playerInfoMap.get(userCode).getBuff()[i] % FRAME_PER_SECOND == 0) {
                                    playerService.generateNotificationMessage(userCode, "距离复活还有"
                                            + playerInfoMap.get(userCode).getBuff()[i] / FRAME_PER_SECOND + "秒。");
                                }
                            }
                        }
                    });

            onlineMap.entrySet().stream()
                    .filter(entry2 -> playerInfoMap.containsKey(entry2.getKey())
                            && playerInfoMap.get(entry2.getKey()).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING
                            && (playerInfoMap.get(entry2.getKey()).getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0))
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
                        if (randomNumber < 40D / 70000D) {
                            playerService.changeHunger(entry2.getKey(), -1, false);
                        }

                        // Change thirst
                        randomNumber = Math.random();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            randomNumber *= 10;
                        }
                        if (randomNumber < 40D / 30000D) {
                            playerService.changeThirst(entry2.getKey(), -1, false);
                        }

                        // Change skill remaining time
                        for (int i = 0; i < playerInfoMap.get(entry2.getKey()).getSkill().length; i++) {
                            if (playerInfoMap.get(entry2.getKey()).getSkill()[i][2] > 0) {
                                playerInfoMap.get(entry2.getKey()).getSkill()[i][2] = playerInfoMap.get(entry2.getKey()).getSkill()[i][2] - 1;
                            }
                        }

                        // Update buff
                        playerService.updateBuff(entry2.getKey());
                    });

            // Other movements
            worldService.updateNpcMovement();
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
            world.getOnlineMap().entrySet().stream()
                    .forEach(entry2 -> {
                        PlayerInfo playerInfo = world.getPlayerInfoMap().get(entry2.getKey());
                        if (playerInfo.getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN
                                && timestamp - entry2.getValue() > GamePalConstants.ONLINE_TIMEOUT_SECOND) {
                            userService.logoff(entry2.getKey(), null, false);
                        }
                    });
        }
    }

    @Scheduled(cron = "*/5 * * * * ?")
    public void executeBy5s() {
        for (Map.Entry<String, GameWorld> entry1 : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry1.getValue();
            Map<String, Long> onlineMap = world.getOnlineMap();
            onlineMap.entrySet().stream()
                    .filter(entry2 -> world.getPlayerInfoMap().get(entry2.getKey()).getPlayerType()
                            == GamePalConstants.PLAYER_TYPE_HUMAN)
                    .filter(entry2 -> world.getPlayerInfoMap().get(entry2.getKey()).getPlayerStatus()
                            == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(entry2 -> {
                        String userCode = entry2.getKey();
                        String npcUserCode = npcManager.createNpc(world);
                        npcManager.putNpc(userCode, npcUserCode);
                    });
        }
    }
}
