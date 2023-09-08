package com.github.ltprc.gamepal.task.effect;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VpRecoveryTask {

    @Autowired
    private WorldService worldService;

    @Autowired
    private PlayerService playerService;

    @Scheduled(fixedRate = 50)
    public void execute() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, PlayerInfo> playerInfoMap = playerService.getPlayerInfoMap();
            onlineMap.entrySet().stream()
                    .filter(entry2 -> playerInfoMap.containsKey(entry2.getKey())
                            && playerInfoMap.get(entry2.getKey()).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(entry2 -> {
                        int newVp = 0;
                        Coordinate speed = playerInfoMap.get(entry2.getKey()).getSpeed();
                        if (Math.abs(speed.getX().doubleValue()) > 0 || Math.abs(speed.getY().doubleValue()) > 0) {
                            newVp--;
                        }
                        if (Double.valueOf(playerInfoMap.get(entry2.getKey()).getHp()) / Double.valueOf(playerInfoMap.get(entry2.getKey()).getHpMax()) >= 0.5) {
                            newVp++;
                        } else if (Double.valueOf(playerInfoMap.get(entry2.getKey()).getHp()) / Double.valueOf(playerInfoMap.get(entry2.getKey()).getHpMax()) < 0.1) {
                            newVp--;
                        }
                        playerService.changeHp(entry2.getKey(), newVp, false);
                    });
        }
    }
}
