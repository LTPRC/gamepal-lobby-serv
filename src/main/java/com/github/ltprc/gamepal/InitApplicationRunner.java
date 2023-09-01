package com.github.ltprc.gamepal;

import com.github.ltprc.gamepal.model.world.GameWorld;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.service.impl.WorldServiceImpl;
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
    public void run(ApplicationArguments args) throws Exception {
        String defaultWorldName = "DEFAULT";
        worldService.addWorld(defaultWorldName);
        logger.info("World " + defaultWorldName + " is added.");
        worldService.loadScenes();
        logger.info("World map is loaded.");
    }
}
