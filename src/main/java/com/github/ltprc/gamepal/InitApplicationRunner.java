package com.github.ltprc.gamepal;

import com.github.ltprc.gamepal.service.WorldService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitApplicationRunner implements ApplicationRunner {

    private static final Log logger = LogFactory.getLog(InitApplicationRunner.class);

    @Autowired
    private WorldService worldService;

    @Override
    public void run(ApplicationArguments args) {
        String defaultWorldName = "DEFAULT";
        worldService.loadItems();
        logger.info("Item config is loaded.");
        worldService.addWorld(defaultWorldName);
        logger.info("World " + defaultWorldName + " is added.");
    }
}
