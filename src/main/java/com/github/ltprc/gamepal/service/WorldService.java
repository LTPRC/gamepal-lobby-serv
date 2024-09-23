package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.item.Item;
import com.github.ltprc.gamepal.model.item.Recipe;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface WorldService {

    Map<String, GameWorld> getWorldMap();

    ResponseEntity<String> addWorld(String worldId);

    ResponseEntity<String> removeWorld(String worldId);

    Map<String, Item> getItemMap();

    Map<String, Recipe> getRecipeMap();

    void loadItems();

    void loadRecipes();

    ResponseEntity<String> addEvent(String userCode, WorldBlock event);

    void updateEvents(GameWorld world);

    void expandByCoordinate(GameWorld world, WorldCoordinate fromWorldCoordinate, WorldCoordinate toWorldCoordinate,
                            int depth);

    void expandRegion(GameWorld world, int regionNo);

    void expandScene(GameWorld world, WorldCoordinate worldCoordinate, int depth);

    void updateWorldTime(GameWorld world, int increment);

    ResponseEntity<String> putBlock(GameWorld world, WorldBlock worldBlock);
}
