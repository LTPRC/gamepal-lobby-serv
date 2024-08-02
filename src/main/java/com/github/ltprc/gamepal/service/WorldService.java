package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.item.Item;
import com.github.ltprc.gamepal.model.item.Recipe;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

public interface WorldService {

    Map<String, GameWorld> getWorldMap();

    ResponseEntity<String> addWorld(String worldId);

    ResponseEntity<String> removeWorld(String worldId);

    Map<String, Item> getItemMap();

    Map<String, Recipe> getRecipeMap();

    void loadItems();

    void loadRecipes();

    void initiateGame(GameWorld world);

    ResponseEntity<String> addEvent(String userCode, WorldBlock event);

    void updateEvents(GameWorld world);

    void expandScene(GameWorld world, WorldCoordinate worldCoordinate);

    void updateWorldTime(GameWorld world, int increment);
}
