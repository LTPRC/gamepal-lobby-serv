package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.Region;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface WorldService {

    Map<String, GameWorld> getWorldMap();

    ResponseEntity<String> addWorld(String worldCode);

    ResponseEntity<String> removeWorld(String worldCode);

    Map<Integer, Region> getRegionMap();

    void loadScenes();
}
