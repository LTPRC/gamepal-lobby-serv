package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FrameEventTask {

    @Autowired
    private WorldService worldService;

    @Scheduled(fixedRate = 40)
    public void execute() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            worldService.updateEvents(entry.getValue());
        }
    }
}
