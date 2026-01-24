package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.item.Junk;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

import java.util.Optional;

public interface ItemManager {
    boolean useItem(GameWorld world, String userCode, String itemNo, int itemAmount);
    boolean getItem(GameWorld world, String userCode, String itemNo, int itemAmount, boolean isNotified);
    boolean getPreservedItem(GameWorld world, String userCode, String itemNo, int itemAmount);
    void getInteractedItem(GameWorld world, String userCode, String itemNo, int itemAmount);
    void recycleItem(GameWorld world, String userCode, String itemNo, int itemAmount);
    void useRecipe(GameWorld world, String userCode, String recipeNo, int recipeAmount);
    boolean useTools(GameWorld world, String userCode, String itemNo);
    boolean useOutfits(GameWorld world, String userCode, String itemNo);
    boolean useConsumable(GameWorld world, String userCode, String itemNo, int itemAmount);
    Optional<Junk> peekRandomJunk(GameWorld world, String userCode);
}
