package com.github.ltprc.gamepal.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Deprecated
public class CheckOnlineTask {

    //@Scheduled(cron = "0 */5 * * * ?")
    public void execute() {
        // ...
    }
}
