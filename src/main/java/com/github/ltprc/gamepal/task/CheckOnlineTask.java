package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class CheckOnlineTask {

    private static final long TIMEOUT_SECOND = 3;

    @Autowired
    private UserService userService;

    /**
     * This method is used for checking idle user which is not under anyone's control.
     */
    @Scheduled(cron = "*/2 * * * * ?")
    public void execute() {
        Map<String, Long> onlineMap = userService.getOnlineMap();
        if (!onlineMap.isEmpty() && Instant.now().getEpochSecond() - onlineMap.entrySet().iterator().next().getValue()
                > TIMEOUT_SECOND) {
            String userCode = onlineMap.entrySet().iterator().next().getKey();
            userService.logoff(userCode, userService.getTokenMap().get(userCode));
        }
    }
}
