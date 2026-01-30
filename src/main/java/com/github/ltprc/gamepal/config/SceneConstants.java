package com.github.ltprc.gamepal.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 场景生成相关的常量配置
 */
public class SceneConstants {
    
    // 物体生成权重映射
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_DEFAULT;
    
    // 泥土地形上的物体权重
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_DIRT;
    
    // 沙地地形上的物体权重
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_SAND;
    
    // 草地地形上的物体权重
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_GRASS;
    
    // 沼泽地形上的物体权重
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_SWAMP;
    
    // 粗糙地形上的物体权重
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_ROUGH;
    
    // 地下地形上的物体权重
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_SUBTERRANEAN;
    
    // 岩浆地形上的物体权重
    public static final Map<Integer, Integer> OBJECT_WEIGHT_MAP_LAVA;
    
    // 动物生成权重映射
    public static final Map<Integer, Integer> ANIMAL_WEIGHT_MAP_DEFAULT;
    
    // 泥土地形上的动物权重
    public static final Map<Integer, Integer> ANIMAL_WEIGHT_MAP_DIRT;
    
    // 草地地形上的动物权重
    public static final Map<Integer, Integer> ANIMAL_WEIGHT_MAP_GRASS;
    
    // 雪地地形上的动物权重
    public static final Map<Integer, Integer> ANIMAL_WEIGHT_MAP_SNOW;
    
    // 沼泽地形上的动物权重
    public static final Map<Integer, Integer> ANIMAL_WEIGHT_MAP_SWAMP;
    
    // 粗糙地形上的动物权重
    public static final Map<Integer, Integer> ANIMAL_WEIGHT_MAP_ROUGH;
    
    // 地下地形上的动物权重
    public static final Map<Integer, Integer> ANIMAL_WEIGHT_MAP_SUBTERRANEAN;
    
    static {
        // 初始化默认权重映射
        Map<Integer, Integer> tmpObjDefault = new HashMap<>();
        tmpObjDefault.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        OBJECT_WEIGHT_MAP_DEFAULT = Collections.unmodifiableMap(tmpObjDefault);
        
        // 初始化泥土地形权重
        Map<Integer, Integer> tmpObjDirt = new HashMap<>();
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BIG_PINE, 10);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BIG_OAK, 10);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE, 1);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_PINE, 20);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_OAK, 20);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_WITHERED_TREE, 2);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_PALM, 0);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_RAFFLESIA, 1);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_STUMP, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_MOSSY_STUMP, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_HOLLOW_TRUNK, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_FLOWER_BUSH, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BUSH, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_1, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_2, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_3, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_1, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_2, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_3, 5);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_MUSHROOM_1, 2);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_MUSHROOM_2, 2);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_GRASS_1, 10);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_GRASS_2, 10);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_GRASS_3, 10);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_GRASS_4, 10);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_CACTUS_1, 0);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_CACTUS_2, 0);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_CACTUS_3, 0);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_ROCK_1, 1);
        tmpObjDirt.put(BlockConstants.BLOCK_CODE_ROCK_2, 1);
        OBJECT_WEIGHT_MAP_DIRT = Collections.unmodifiableMap(tmpObjDirt);
        
        // 初始化沙地地形权重
        Map<Integer, Integer> tmpObjSand = new HashMap<>();
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BIG_PINE, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BIG_OAK, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_PINE, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_OAK, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_WITHERED_TREE, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_PALM, 100);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_RAFFLESIA, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_STUMP, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_MOSSY_STUMP, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_HOLLOW_TRUNK, 5);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_FLOWER_BUSH, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BUSH, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_1, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_2, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_3, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_1, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_2, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_3, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_MUSHROOM_1, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_MUSHROOM_2, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_GRASS_1, 10);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_GRASS_2, 10);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_GRASS_3, 10);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_GRASS_4, 10);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_CACTUS_1, 50);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_CACTUS_2, 50);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_CACTUS_3, 50);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_ROCK_1, 0);
        tmpObjSand.put(BlockConstants.BLOCK_CODE_ROCK_2, 0);
        OBJECT_WEIGHT_MAP_SAND = Collections.unmodifiableMap(tmpObjSand);
        
        // 初始化草地地形权重
        Map<Integer, Integer> tmpObjGrass = new HashMap<>();
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BIG_PINE, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BIG_OAK, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE, 2);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_PINE, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_OAK, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_WITHERED_TREE, 2);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_PALM, 0);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_RAFFLESIA, 2);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_STUMP, 5);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_MOSSY_STUMP, 5);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_HOLLOW_TRUNK, 5);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_FLOWER_BUSH, 20);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BUSH, 20);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_1, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_2, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_3, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_1, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_2, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_3, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_MUSHROOM_1, 20);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_MUSHROOM_2, 20);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_GRASS_1, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_GRASS_2, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_GRASS_3, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_GRASS_4, 100);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_CACTUS_1, 0);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_CACTUS_2, 0);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_CACTUS_3, 0);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_ROCK_1, 10);
        tmpObjGrass.put(BlockConstants.BLOCK_CODE_ROCK_2, 10);
        OBJECT_WEIGHT_MAP_GRASS = Collections.unmodifiableMap(tmpObjGrass);
        
        // 初始化沼泽地形权重
        Map<Integer, Integer> tmpObjSwamp = new HashMap<>();
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BIG_PINE, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BIG_OAK, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_PINE, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_OAK, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_WITHERED_TREE, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_PALM, 0);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_RAFFLESIA, 20);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_STUMP, 0);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_MOSSY_STUMP, 1);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_HOLLOW_TRUNK, 1);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_FLOWER_BUSH, 20);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BUSH, 20);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_1, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_2, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_3, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_1, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_2, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_3, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_MUSHROOM_1, 50);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_MUSHROOM_2, 50);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_GRASS_1, 100);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_GRASS_2, 100);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_GRASS_3, 100);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_GRASS_4, 100);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_CACTUS_1, 0);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_CACTUS_2, 0);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_CACTUS_3, 0);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_ROCK_1, 10);
        tmpObjSwamp.put(BlockConstants.BLOCK_CODE_ROCK_2, 10);
        OBJECT_WEIGHT_MAP_SWAMP = Collections.unmodifiableMap(tmpObjSwamp);
        
        // 初始化粗糙地形权重
        Map<Integer, Integer> tmpObjRough = new HashMap<>();
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BIG_PINE, 20);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BIG_OAK, 0);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE, 1);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_PINE, 20);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_OAK, 0);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_WITHERED_TREE, 1);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_PALM, 0);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_RAFFLESIA, 0);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_STUMP, 1);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_MOSSY_STUMP, 1);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_HOLLOW_TRUNK, 1);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_FLOWER_BUSH, 20);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BUSH, 20);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_1, 2);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_2, 2);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_3, 2);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_1, 2);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_2, 2);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_3, 2);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_MUSHROOM_1, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_MUSHROOM_2, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_GRASS_1, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_GRASS_2, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_GRASS_3, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_GRASS_4, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_CACTUS_1, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_CACTUS_2, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_CACTUS_3, 5);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_ROCK_1, 50);
        tmpObjRough.put(BlockConstants.BLOCK_CODE_ROCK_2, 50);
        OBJECT_WEIGHT_MAP_ROUGH = Collections.unmodifiableMap(tmpObjRough);
        
        // 初始化地下地形权重
        Map<Integer, Integer> tmpObjSubterranean = new HashMap<>();
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BIG_PINE, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BIG_OAK, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_PINE, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_OAK, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_WITHERED_TREE, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_PALM, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_RAFFLESIA, 5);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_STUMP, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_MOSSY_STUMP, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_HOLLOW_TRUNK, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_FLOWER_BUSH, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BUSH, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_1, 5);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_2, 5);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_SMALL_FLOWER_3, 5);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_1, 5);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_2, 5);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_BIG_FLOWER_3, 5);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_MUSHROOM_1, 100);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_MUSHROOM_2, 100);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_GRASS_1, 10);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_GRASS_2, 10);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_GRASS_3, 10);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_GRASS_4, 10);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_CACTUS_1, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_CACTUS_2, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_CACTUS_3, 0);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_ROCK_1, 100);
        tmpObjSubterranean.put(BlockConstants.BLOCK_CODE_ROCK_2, 100);
        OBJECT_WEIGHT_MAP_SUBTERRANEAN = Collections.unmodifiableMap(tmpObjSubterranean);
        
        // 初始化岩浆地形权重
        Map<Integer, Integer> tmpObjLava = new HashMap<>();
        tmpObjLava.put(BlockConstants.BLOCK_CODE_BLACK, 2000);
        tmpObjLava.put(BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE, 10);
        tmpObjLava.put(BlockConstants.BLOCK_CODE_WITHERED_TREE, 10);
        OBJECT_WEIGHT_MAP_LAVA = Collections.unmodifiableMap(tmpObjLava);
        
        // 初始化默认动物权重
        Map<Integer, Integer> tmpAnimalDefault = new HashMap<>();
        tmpAnimalDefault.put(BlockConstants.BLOCK_CODE_BLACK, 10000);
        ANIMAL_WEIGHT_MAP_DEFAULT = Collections.unmodifiableMap(tmpAnimalDefault);
        
        // 初始化泥土地形动物权重
        Map<Integer, Integer> tmpAnimalDirt = new HashMap<>();
        tmpAnimalDirt.put(BlockConstants.BLOCK_CODE_BLACK, 10000);
        tmpAnimalDirt.put(CreatureConstants.SKIN_COLOR_DOG, 10);
        ANIMAL_WEIGHT_MAP_DIRT = Collections.unmodifiableMap(tmpAnimalDirt);
        
        // 初始化草地地形动物权重
        Map<Integer, Integer> tmpAnimalGrass = new HashMap<>();
        tmpAnimalGrass.put(BlockConstants.BLOCK_CODE_BLACK, 10000);
        tmpAnimalGrass.put(CreatureConstants.SKIN_COLOR_MONKEY, 2);
        tmpAnimalGrass.put(CreatureConstants.SKIN_COLOR_CHICKEN, 20);
        tmpAnimalGrass.put(CreatureConstants.SKIN_COLOR_BUFFALO, 10);
        tmpAnimalGrass.put(CreatureConstants.SKIN_COLOR_SHEEP, 20);
        tmpAnimalGrass.put(CreatureConstants.SKIN_COLOR_CAT, 5);
        tmpAnimalGrass.put(CreatureConstants.SKIN_COLOR_HORSE, 10);
        ANIMAL_WEIGHT_MAP_GRASS = Collections.unmodifiableMap(tmpAnimalGrass);
        
        // 初始化雪地地形动物权重
        Map<Integer, Integer> tmpAnimalSnow = new HashMap<>();
        tmpAnimalSnow.put(BlockConstants.BLOCK_CODE_BLACK, 10000);
        tmpAnimalSnow.put(CreatureConstants.SKIN_COLOR_POLAR_BEAR, 10);
        ANIMAL_WEIGHT_MAP_SNOW = Collections.unmodifiableMap(tmpAnimalSnow);
        
        // 初始化沼泽地形动物权重
        Map<Integer, Integer> tmpAnimalSwamp = new HashMap<>();
        tmpAnimalSwamp.put(BlockConstants.BLOCK_CODE_BLACK, 10000);
        tmpAnimalSwamp.put(CreatureConstants.SKIN_COLOR_FROG, 50);
        ANIMAL_WEIGHT_MAP_SWAMP = Collections.unmodifiableMap(tmpAnimalSwamp);
        
        // 初始化粗糙地形动物权重
        Map<Integer, Integer> tmpAnimalRough = new HashMap<>();
        tmpAnimalRough.put(BlockConstants.BLOCK_CODE_BLACK, 10000);
        tmpAnimalRough.put(CreatureConstants.SKIN_COLOR_FOX, 10);
        tmpAnimalRough.put(CreatureConstants.SKIN_COLOR_WOLF, 10);
        tmpAnimalRough.put(CreatureConstants.SKIN_COLOR_TIGER, 10);
        tmpAnimalRough.put(CreatureConstants.SKIN_COLOR_BOAR, 10);
        ANIMAL_WEIGHT_MAP_ROUGH = Collections.unmodifiableMap(tmpAnimalRough);
        
        // 初始化地下地形动物权重
        Map<Integer, Integer> tmpAnimalSubterranean = new HashMap<>();
        tmpAnimalSubterranean.put(BlockConstants.BLOCK_CODE_BLACK, 10000);
        tmpAnimalSubterranean.put(CreatureConstants.SKIN_COLOR_RACOON, 5);
        ANIMAL_WEIGHT_MAP_SUBTERRANEAN = Collections.unmodifiableMap(tmpAnimalSubterranean);
    }
}