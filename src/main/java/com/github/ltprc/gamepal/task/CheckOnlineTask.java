package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class CheckOnlineTask {

    @Autowired
    private WorldService worldService;

    @Autowired
    private UserService userService;

    /**
     * This method is used for checking idle user which is not under anyone's control.
     * All worlds are to be checked. 23/08/28
     */
//    @Scheduled(cron = "* */2 * * * ?")
    public void execute() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            Map<String, Long> onlineMap = world.getOnlineMap();
            if (!onlineMap.isEmpty() && Instant.now().getEpochSecond() - onlineMap.entrySet().iterator().next().getValue()
                    > GamePalConstants.ONLINE_TIMEOUT_SECOND) {
                String userCode = onlineMap.entrySet().iterator().next().getKey();
                userService.logoff(userCode, "", false);
            }
        }
    }
}
