package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.item.Item;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

public interface WorldService {

    Map<String, GameWorld> getWorldMap();

    ResponseEntity<String> addWorld(String worldCode);

    ResponseEntity<String> removeWorld(String worldCode);

    Map<String, Item> getItemMap();

    void loadItems();

    void initiateGame(GameWorld world);

    ResponseEntity<String> addEvent(String userCode, WorldBlock event);

    void updateEvents(GameWorld world);

    void expandScene(GameWorld world, WorldCoordinate worldCoordinate);

    void updateWorldTime(GameWorld world);
}
