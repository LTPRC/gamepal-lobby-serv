package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
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

    @Scheduled(fixedRate = 40)
    public void executeByFrame() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();

            // Update events
            worldService.updateEvents(world);

            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, PlayerInfo> playerInfoMap = playerService.getPlayerInfoMap();

            // Update buff
            // buff minus one
            onlineMap.entrySet().stream()
                    .filter(entry2 -> playerInfoMap.containsKey(entry2.getKey())
                            && playerInfoMap.get(entry2.getKey()).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(entry2 -> {
                        for (int i = 0; i < GamePalConstants.BUFF_CODE_LENGTH; i++) {
                            if (playerInfoMap.get(entry2.getKey()).getBuff()[i] > 0)
                            playerInfoMap.get(entry2.getKey()).getBuff()[i] = playerInfoMap.get(entry2.getKey()).getBuff()[i] - 1;
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
                        // TODO please cancel this poison
                        playerService.changeHp(entry2.getKey(), -1, false);

                        // Change vp
                        int newVp = 1;
                        Coordinate speed = playerInfoMap.get(entry2.getKey()).getSpeed();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            newVp--;
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

                        // Update buff
                        playerService.updateBuff(entry2.getKey());
                    });
        }
    }

    @Scheduled(cron = "* */2 * * * ?")
    public void executeBy120s() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            Map<String, Long> onlineMap = world.getOnlineMap();

            // Check idle user which is not under anyone's control.
            if (!onlineMap.isEmpty() && Instant.now().getEpochSecond() - onlineMap.entrySet().iterator().next().getValue()
                    > GamePalConstants.ONLINE_TIMEOUT_SECOND) {
                String userCode = onlineMap.entrySet().iterator().next().getKey();
                userService.logoff(userCode, null, false);
            }
        }
    }
}
