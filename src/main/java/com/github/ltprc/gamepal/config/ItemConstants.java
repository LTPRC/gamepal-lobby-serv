package com.github.ltprc.gamepal.config;

import java.util.HashMap;
import java.util.Map;

public class ItemConstants {

    public static final char ITEM_CHARACTER_TOOL = 't';
    public static final char ITEM_CHARACTER_OUTFIT = 'o';
    public static final char ITEM_CHARACTER_CONSUMABLE = 'c';
    public static final char ITEM_CHARACTER_MATERIAL = 'm';
    public static final char ITEM_CHARACTER_JUNK = 'j';
    public static final char ITEM_CHARACTER_AMMO = 'a';
    public static final char ITEM_CHARACTER_NOTE = 'n';
    public static final char ITEM_CHARACTER_RECORDING = 'r';
    public static final char RECIPE_CHARACTER_WORKSHOP = 'w';
    public static final char RECIPE_CHARACTER_COOKER = 'c';
    public static final char RECIPE_CHARACTER_SINK = 's';

    // Backend constants

    public static final int TOOL_INDEX_DEFAULT = 0;
    public static final int TOOL_INDEX_PRIMARY = 1;
    public static final int TOOL_INDEX_SECONDARY = 2;

    public static final int OUTFIT_INDEX_DEFAULT = 0;
    public static final int OUTFIT_INDEX_CLOTHES = 1;

    public static Map<Integer, String> ITEM_PACK_MAP = new HashMap<>();
    public static Map<String, Integer> ITEM_BUILD_MAP = new HashMap<>();

    static {
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_BED, "t308");
        ITEM_BUILD_MAP.put("t308", BlockConstants.BLOCK_TYPE_BED);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_TOILET, "t309");
        ITEM_BUILD_MAP.put("t309", BlockConstants.BLOCK_TYPE_TOILET);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_DRESSER, "t310");
        ITEM_BUILD_MAP.put("t310", BlockConstants.BLOCK_TYPE_DRESSER);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_STORAGE, "t311");
        ITEM_BUILD_MAP.put("t311", BlockConstants.BLOCK_TYPE_STORAGE);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_COOKER, "t312");
        ITEM_BUILD_MAP.put("t312", BlockConstants.BLOCK_TYPE_COOKER);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_SINK, "t313");
        ITEM_BUILD_MAP.put("t313", BlockConstants.BLOCK_TYPE_SINK);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_CONTAINER, "t314");
        ITEM_BUILD_MAP.put("t314", BlockConstants.BLOCK_TYPE_CONTAINER);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_RADIO, "t315");
        ITEM_BUILD_MAP.put("t315", BlockConstants.BLOCK_TYPE_RADIO);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_BUILDING, "t351");
        ITEM_BUILD_MAP.put("t351", BlockConstants.BLOCK_TYPE_BUILDING);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_WORKSHOP, "t301");
        ITEM_BUILD_MAP.put("t301", BlockConstants.BLOCK_TYPE_WORKSHOP);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL, "t302");
        ITEM_BUILD_MAP.put("t302", BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO, "t303");
        ITEM_BUILD_MAP.put("t303", BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT, "t304");
        ITEM_BUILD_MAP.put("t304", BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM, "t305");
        ITEM_BUILD_MAP.put("t305", BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE, "t306");
        ITEM_BUILD_MAP.put("t306", BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE);
    }
}
