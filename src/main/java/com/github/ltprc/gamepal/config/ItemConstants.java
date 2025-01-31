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

    public static final String ITEM_NO_OUTFIT_UNDERWEAR = "o001";
    public static final String ITEM_NO_OUTFIT_ZGC_1 = "o002";
    public static final String ITEM_NO_OUTFIT_ZGC_2 = "o003";
    public static final String ITEM_NO_OUTFIT_SOLDIER = "o004";
    public static final String ITEM_NO_OUTFIT_SUIT_1 = "o005";
    public static final String ITEM_NO_OUTFIT_SUIT_2 = "o006";

    // Backend constants

    public static final int TOOL_INDEX_DEFAULT = 0;
    public static final int TOOL_INDEX_PRIMARY = 1;
    public static final int TOOL_INDEX_SECONDARY = 2;

    public static final int OUTFIT_INDEX_DEFAULT = 0;
    public static final int OUTFIT_INDEX_CLOTHES = 1;

    public static Map<Integer, String> ITEM_PACK_MAP = new HashMap<>(); // blockCode, itemNo
    public static Map<String, Integer> ITEM_BUILD_MAP = new HashMap<>(); // itemNo, blockCode
    public static Map<String, String> ITEM_PLANT_MAP = new HashMap<>();

    static {
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_SINGLE_BED, "t308");
        ITEM_BUILD_MAP.put("t308", BlockConstants.BLOCK_CODE_SINGLE_BED);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_TOILET, "t309");
        ITEM_BUILD_MAP.put("t309", BlockConstants.BLOCK_CODE_TOILET);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_DRESSER_1, "t310");
        ITEM_BUILD_MAP.put("t310", BlockConstants.BLOCK_CODE_DRESSER_1);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_CHEST_OPEN, "t311");
        ITEM_BUILD_MAP.put("t311", BlockConstants.BLOCK_CODE_CHEST_OPEN);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_COOKER, "t312");
        ITEM_BUILD_MAP.put("t312", BlockConstants.BLOCK_CODE_COOKER);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_SINK, "t313");
        ITEM_BUILD_MAP.put("t313", BlockConstants.BLOCK_CODE_SINK);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_CHEST_CLOSE, "t314");
        ITEM_BUILD_MAP.put("t314", BlockConstants.BLOCK_CODE_CHEST_CLOSE);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_SPEAKER, "t315");
        ITEM_BUILD_MAP.put("t315", BlockConstants.BLOCK_CODE_SPEAKER);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_ASH_PILE, "t351");
        ITEM_BUILD_MAP.put("t351", BlockConstants.BLOCK_CODE_ASH_PILE);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_FARM, "t234");
        ITEM_BUILD_MAP.put("t234", BlockConstants.BLOCK_CODE_FARM);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_WORKSHOP_CONSTRUCTION, "t301");
        ITEM_BUILD_MAP.put("t301", BlockConstants.BLOCK_CODE_WORKSHOP_CONSTRUCTION);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_WORKSHOP_TOOL, "t302");
        ITEM_BUILD_MAP.put("t302", BlockConstants.BLOCK_CODE_WORKSHOP_TOOL);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_WORKSHOP_AMMO, "t303");
        ITEM_BUILD_MAP.put("t303", BlockConstants.BLOCK_CODE_WORKSHOP_AMMO);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_WORKSHOP_OUTFIT, "t304");
        ITEM_BUILD_MAP.put("t304", BlockConstants.BLOCK_CODE_WORKSHOP_OUTFIT);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_WORKSHOP_CHEM, "t305");
        ITEM_BUILD_MAP.put("t305", BlockConstants.BLOCK_CODE_WORKSHOP_CHEM);
        ITEM_PACK_MAP.put(BlockConstants.BLOCK_CODE_WORKSHOP_RECYCLE, "t306");
        ITEM_BUILD_MAP.put("t306", BlockConstants.BLOCK_CODE_WORKSHOP_RECYCLE);

        ITEM_PLANT_MAP.put("c064", "c024");
    }
}
