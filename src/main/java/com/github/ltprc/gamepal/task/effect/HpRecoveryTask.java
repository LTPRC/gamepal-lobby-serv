package com.github.ltprc.gamepal.task.effect;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HpRecoveryTask {

    @Autowired
    private WorldService worldService;

    @Autowired
    private PlayerService playerService;

    @Scheduled(fixedRate = 1000)
    public void execute() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, PlayerInfo> playerInfoMap = playerService.getPlayerInfoMap();
            onlineMap.entrySet().stream()
                    .filter(entry2 -> playerInfoMap.containsKey(entry2.getKey())
                            && playerInfoMap.get(entry2.getKey()).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(entry2 -> {
                        playerService.changeHp(entry2.getKey(), 1, false);
                    });
        }
    }
}
