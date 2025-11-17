package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.item.Item;
import com.github.ltprc.gamepal.model.item.Recipe;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface WorldService {

    Map<String, GameWorld> getWorldMap();

    ResponseEntity<String> addWorld(String worldId);

    ResponseEntity<String> removeWorld(String worldId);

    Map<String, Item> getItemMap();

    Map<String, Recipe> getRecipeMap();

    Map<Integer, Structure> getStructureMap();

    void loadItems();

    void loadRecipes();

    void loadStructures();

    void expandByCoordinate(GameWorld world, WorldCoordinate fromWorldCoordinate, WorldCoordinate toWorldCoordinate,
                            int depth);

    void expandRegion(GameWorld world, int regionNo);

    void expandScene(GameWorld world, WorldCoordinate worldCoordinate, int depth);

    void updateWorldTime(GameWorld world, int increment);

    void registerOnline(GameWorld world, String id);

    void registerOffline(GameWorld world, String id);
}
