package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.StructuredBlock;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.coordinate.PlanarCoordinate;
import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

@Component
public class BlockFactory {

    private BlockFactory() {}

    public static BlockInfo createBlockInfoByCode(int blockCode) {
        long timestamp = System.currentTimeMillis();
        int blockType = BlockUtil.convertBlockCode2Type(blockCode);
        String id = UUID.randomUUID().toString();
        BlockInfo blockInfo = new BlockInfo(blockType, id, blockCode, timestamp);
        initializeBlockInfoHp(blockInfo, timestamp);
        blockInfo.setFrame(0, timestamp);
        defineFrameMax(blockInfo, timestamp);
        definePeriod(blockInfo, timestamp);
        return blockInfo;
    }

    public static void initializeBlockInfoHp(BlockInfo blockInfo, long timestamp) {
        int hpMax = BlockConstants.HP_DEFAULT;
        switch (blockInfo.getType()) {
            case BlockConstants.BLOCK_TYPE_PLAYER:
                hpMax = 1000;
                break;
            default:
                break;
        }
        blockInfo.setHpMax(hpMax, timestamp);
        blockInfo.getHp().set(blockInfo.getHpMax().get());
    }

    public static void defineFrameMax(BlockInfo blockInfo, long timestamp) {
        int frameMax;
        switch (blockInfo.getType()) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
                switch (blockInfo.getCode()) {
                    case BlockConstants.BLOCK_CODE_SPARK_SHORT:
                        frameMax = 5;
                        break;
                    case BlockConstants.BLOCK_CODE_LIGHT_SMOKE:
                        frameMax = 6;
                        break;
                    case BlockConstants.BLOCK_CODE_SPARK:
                    case BlockConstants.BLOCK_CODE_SHOCK:
                        frameMax = 10;
                        break;
                    case BlockConstants.BLOCK_CODE_DECAY:
                    case BlockConstants.BLOCK_CODE_CHEER:
                    case BlockConstants.BLOCK_CODE_CURSE:
                        frameMax = 50;
                        break;
                    case BlockConstants.BLOCK_CODE_BLEED_SEVERE:
                        frameMax = 250;
                        break;
                    case BlockConstants.BLOCK_CODE_TIMED_BOMB:
                        frameMax = blockInfo.getId().hashCode() % BlockConstants.TIMED_BOMB_FRAME_MAX_MAX;
                        break;
                    default:
                        frameMax = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                frameMax = GamePalConstants.DROP_DISAPPEAR_THRESHOLD_IN_FRAME;
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR_DECORATION:
                switch (blockInfo.getCode()) {
                    case BlockConstants.BLOCK_CODE_BLEED_SEVERE:
                        frameMax = 250;
                        break;
                    case BlockConstants.BLOCK_CODE_DROP_SHADOW:
                    case BlockConstants.BLOCK_CODE_BUBBLE:
                        frameMax = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                        break;
                    default:
                        frameMax = BlockConstants.FRAME_MAX_INFINITE_DEFAULT;
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_PLASMA:
                switch (blockInfo.getCode()) {
                    case BlockConstants.BLOCK_CODE_EXPLODE:
                    case BlockConstants.BLOCK_CODE_SPRAY:
                        frameMax = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                        break;
                    case BlockConstants.BLOCK_CODE_FIRE:
                        frameMax = BlockConstants.PERIOD_DYNAMIC_DEFAULT * 5;
                        break;
                    default:
                        frameMax = BlockConstants.FRAME_MAX_INFINITE_DEFAULT;
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_MELEE:
            case BlockConstants.BLOCK_TYPE_SHOOT:
                frameMax = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                break;
            case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
                frameMax = BlockConstants.PERIOD_DYNAMIC_DEFAULT * 3;
                break;
            default:
                frameMax = BlockConstants.FRAME_MAX_INFINITE_DEFAULT;
                break;
        }
        blockInfo.setFrameMax(frameMax, timestamp);
    }

    public static void definePeriod(BlockInfo blockInfo, long timestamp) {
        int period;
        switch (blockInfo.getType()) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
                switch (blockInfo.getCode()) {
                    case BlockConstants.BLOCK_CODE_SPARK_SHORT:
                        period = 5;
                        break;
                    case BlockConstants.BLOCK_CODE_LIGHT_SMOKE:
                        period = 6;
                        break;
                    case BlockConstants.BLOCK_CODE_SPARK:
                    case BlockConstants.BLOCK_CODE_SHOCK:
                        period = 10;
                        break;
                    case BlockConstants.BLOCK_CODE_DECAY:
                    case BlockConstants.BLOCK_CODE_CHEER:
                    case BlockConstants.BLOCK_CODE_CURSE:
                        period = 50;
                        break;
                    default:
                        period = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                period = GamePalConstants.DROP_DISAPPEAR_THRESHOLD_IN_FRAME;
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR_DECORATION:
                switch (blockInfo.getCode()) {
                    case BlockConstants.BLOCK_CODE_BLEED_SEVERE:
                        period = 250;
                        break;
                    case BlockConstants.BLOCK_CODE_DROP_SHADOW:
                    case BlockConstants.BLOCK_CODE_BUBBLE:
                        period = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                        break;
                    default:
                        period = BlockConstants.FRAME_MAX_INFINITE_DEFAULT;
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_PLASMA:
                period = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                break;
            case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
                period = BlockConstants.PERIOD_DYNAMIC_DEFAULT * 3;
                break;
            default:
                period = BlockConstants.PERIOD_STATIC_DEFAULT;
                break;
        }
        blockInfo.setPeriod(period, timestamp);
    }

    public static Structure createStructureByCode(int blockCode) {
        Structure structure;
        Shape shape;
        PlanarCoordinate imageSize;
        int blockType = BlockUtil.convertBlockCode2Type(blockCode);
        Shape roundShape = new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BlockConstants.ROUND_SCENE_OBJECT_RADIUS, BlockConstants.ROUND_SCENE_OBJECT_RADIUS,
                        BlockConstants.Z_DEFAULT));
        switch (blockType) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE, convertEventCode2Layer(blockCode),
                        convertEventCode2Shape(blockCode), convertEventCode2ImageSize(blockCode));
                break;
            case BlockConstants.BLOCK_TYPE_PLAYER:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BlockConstants.PLAYER_RADIUS, BlockConstants.PLAYER_RADIUS,
                                        BlockConstants.PLAYER_HEIGHT)),
                        new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D),
                                        BlockConstants.Z_DEFAULT)),
                        new PlanarCoordinate(BigDecimal.valueOf(0.5D), BigDecimal.valueOf(0.5D)));
                break;
            case BlockConstants.BLOCK_TYPE_TRAP:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE);
                break;
            case BlockConstants.BLOCK_TYPE_TELEPORT:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_TARGET_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE);
                break;
            case BlockConstants.BLOCK_TYPE_GAME:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_BOTTOM);
                break;
            case BlockConstants.BLOCK_TYPE_BED:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_SINGLE_BED:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.5D),
                                                BlockConstants.Z_DEFAULT)));
                        break;
                    default:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE);
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_TOILET:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.5D),
                                        BlockConstants.Z_DEFAULT)));
                break;
            case BlockConstants.BLOCK_TYPE_DRESSER:
            case BlockConstants.BLOCK_TYPE_STORAGE:
            case BlockConstants.BLOCK_TYPE_COOKER:
            case BlockConstants.BLOCK_TYPE_SINK:
            case BlockConstants.BLOCK_TYPE_CONTAINER:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE);
                break;
            case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
            case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, roundShape);
                break;
            case BlockConstants.BLOCK_TYPE_BUILDING:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_SIGN:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE, roundShape);
                        break;
                    case BlockConstants.BLOCK_CODE_STOVE:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.5D),
                                                BlockConstants.Z_DEFAULT)));
                        break;
                    case BlockConstants.BLOCK_CODE_BENCH:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.2D),
                                                BlockConstants.Z_DEFAULT)));
                        break;
                    case BlockConstants.BLOCK_CODE_DESK_1:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.valueOf(0.3D), BigDecimal.valueOf(0.2D),
                                                BlockConstants.Z_DEFAULT)));
                        break;
                    case BlockConstants.BLOCK_CODE_DESK_2:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.valueOf(0.2D), BigDecimal.valueOf(0.3D),
                                                BlockConstants.Z_DEFAULT)));
                        break;
                    case BlockConstants.BLOCK_CODE_ASH_PILE:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                        new Coordinate(BigDecimal.valueOf(0.4D), BigDecimal.valueOf(0.4D),
                                                BlockConstants.Z_DEFAULT)));
                        break;
                    case BlockConstants.BLOCK_CODE_WORKSHOP_EMPTY:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.valueOf(0.4D), BigDecimal.valueOf(0.2D),
                                                BlockConstants.Z_DEFAULT)));
                        break;
                    case BlockConstants.BLOCK_CODE_TABLE_1:
                    case BlockConstants.BLOCK_CODE_TABLE_2:
                    case BlockConstants.BLOCK_CODE_TABLE_3:
                    default:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE);
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                new Coordinate(BigDecimal.valueOf(0.2D), BigDecimal.valueOf(0.4D),
                                        BlockConstants.Z_DEFAULT)));
                break;
            case BlockConstants.BLOCK_TYPE_TREE:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_BIG_PINE:
                    case BlockConstants.BLOCK_CODE_BIG_OAK:
                    case BlockConstants.BLOCK_CODE_BIG_WITHERED_TREE:
                    case BlockConstants.BLOCK_CODE_PINE:
                    case BlockConstants.BLOCK_CODE_OAK:
                    case BlockConstants.BLOCK_CODE_WITHERED_TREE:
                    case BlockConstants.BLOCK_CODE_PALM:
                        imageSize = new PlanarCoordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(3));
                        break;
                    default:
                        imageSize = new PlanarCoordinate(BigDecimal.ONE, BigDecimal.valueOf(2));
                        break;
                }
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_ALL,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        roundShape, imageSize);
                break;
            case BlockConstants.BLOCK_TYPE_SPEAKER:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BlockConstants.ROUND_SCENE_OBJECT_RADIUS,
                                        BlockConstants.ROUND_SCENE_OBJECT_RADIUS,
                                        BlockConstants.Z_DEFAULT)));
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_BOTTOM);
                break;
            case BlockConstants.BLOCK_TYPE_ROCK:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        roundShape);
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, new Shape(),
                        new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR_DECORATION:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_BLEED_SEVERE:
                        shape = new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BigDecimal.valueOf(0.15D), BigDecimal.valueOf(0.3D),
                                        BigDecimal.valueOf(2)));
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM_DECORATION, shape,
                                new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE));
                        break;
                    case BlockConstants.BLOCK_CODE_DROP_SHADOW:
                    case BlockConstants.BLOCK_CODE_BUBBLE:
                        shape = new Shape();
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE, shape,
                                new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE));
                        break;
                    default:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE);
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_WALL:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, new Shape(),
                        new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
            case BlockConstants.BLOCK_TYPE_WALL_DECORATION:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE_DECORATION);
                break;
            case BlockConstants.BLOCK_TYPE_PLASMA:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE);
                break;
            case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_TOP_DECORATION);
                break;
            case BlockConstants.BLOCK_TYPE_MELEE:
            case BlockConstants.BLOCK_TYPE_SHOOT:
                structure = new Structure(convertMeleeShootCode2Material(blockCode),
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BlockConstants.EVENT_DEFAULT_RADIUS, BlockConstants.EVENT_DEFAULT_RADIUS,
                                BlockConstants.Z_DEFAULT)), new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
            case BlockConstants.BLOCK_TYPE_NORMAL:
            default:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, new Shape(),
                        new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
        }
        return structure;
    }

    public static int convertMeleeShootCode2Material(int eventCode) {
        int structureMaterial;
        switch (eventCode) {
            case BlockConstants.BLOCK_CODE_MELEE_HIT:
            case BlockConstants.BLOCK_CODE_MELEE_KICK:
            case BlockConstants.BLOCK_CODE_MELEE_SCRATCH:
            case BlockConstants.BLOCK_CODE_MELEE_SMASH:
            case BlockConstants.BLOCK_CODE_SHOOT_HIT:
            case BlockConstants.BLOCK_CODE_SHOOT_ARROW:
            case BlockConstants.BLOCK_CODE_SHOOT_SLUG:
            case BlockConstants.BLOCK_CODE_SHOOT_THROW_JUNK:
                structureMaterial = BlockConstants.STRUCTURE_MATERIAL_PARTICLE;
                break;
            case BlockConstants.BLOCK_CODE_MELEE_CLEAVE:
            case BlockConstants.BLOCK_CODE_MELEE_CHOP:
            case BlockConstants.BLOCK_CODE_MELEE_PICK:
            case BlockConstants.BLOCK_CODE_MELEE_STAB:
            case BlockConstants.BLOCK_CODE_SHOOT_MAGNUM:
            case BlockConstants.BLOCK_CODE_SHOOT_ROCKET:
            case BlockConstants.BLOCK_CODE_SHOOT_FIRE:
            case BlockConstants.BLOCK_CODE_SHOOT_SPRAY:
                structureMaterial = BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH;
                break;
            default:
                structureMaterial = BlockConstants.STRUCTURE_MATERIAL_NONE;
                break;
        }
        return structureMaterial;
    }

    public static int convertEventCode2Layer(int eventCode) {
        int layer;
        switch (eventCode) {
            case BlockConstants.BLOCK_CODE_EXPLODE:
            case BlockConstants.BLOCK_CODE_BLOCK:
            case BlockConstants.BLOCK_CODE_HEAL:
            case BlockConstants.BLOCK_CODE_DECAY:
            case BlockConstants.BLOCK_CODE_CHEER:
            case BlockConstants.BLOCK_CODE_CURSE:
            case BlockConstants.BLOCK_CODE_SPARK:
            case BlockConstants.BLOCK_CODE_SPARK_SHORT:
            case BlockConstants.BLOCK_CODE_LIGHT_SMOKE:
            case BlockConstants.BLOCK_CODE_DISINTEGRATE:
            case BlockConstants.BLOCK_CODE_BLEED:
                layer = BlockConstants.STRUCTURE_LAYER_TOP_DECORATION;
                break;
            case BlockConstants.BLOCK_CODE_UPGRADE:
            case BlockConstants.BLOCK_CODE_SACRIFICE:
                layer = BlockConstants.STRUCTURE_LAYER_BOTTOM_DECORATION;
                break;
            default:
                layer = BlockConstants.STRUCTURE_LAYER_MIDDLE;
                break;
        }
        return layer;
    }

    public static Shape convertEventCode2Shape(int eventCode) {
        Shape shape;
        switch (eventCode) {
            case BlockConstants.BLOCK_CODE_CHEER:
            case BlockConstants.BLOCK_CODE_CURSE:
            case BlockConstants.BLOCK_CODE_NOISE:
                shape =  new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.valueOf(5D), BigDecimal.valueOf(5D),
                                BlockConstants.Z_DEFAULT));
                break;
            case BlockConstants.BLOCK_CODE_SPARK_SHORT:
                shape = new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.ONE, BigDecimal.ONE,
                                BigDecimal.valueOf(0.1D)));
                break;
            case BlockConstants.BLOCK_CODE_SHOCK:
                shape = new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2),
                                BlockConstants.Z_DEFAULT));
                break;
            default:
                shape = new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BlockConstants.EVENT_DEFAULT_RADIUS, BlockConstants.EVENT_DEFAULT_RADIUS,
                                BlockConstants.Z_DEFAULT));
                break;
        }
        return shape;
    }

    public static PlanarCoordinate convertEventCode2ImageSize(int eventCode) {
        PlanarCoordinate imageSize;
        switch (eventCode) {
            case BlockConstants.BLOCK_CODE_BLOCK:
            case BlockConstants.BLOCK_CODE_UPGRADE:
                imageSize = new PlanarCoordinate(BigDecimal.ONE, BigDecimal.valueOf(2));
                break;
            default:
                imageSize = new PlanarCoordinate(BigDecimal.ONE, BigDecimal.ONE);
                break;
        }
        return imageSize;
    }

    public static Queue<StructuredBlock> createRankingQueue(final RegionInfo regionInfo) {
        return new PriorityQueue<>((o1, o2) -> {
            Block block1 = o1.getBlock();
            Block block2 = o2.getBlock();
            Structure structure1 = o1.getStructure();
            Structure structure2 = o2.getStructure();
            if (block1.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()
                    || block2.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()) {
                return block1.getBlockInfo().getCode() - block2.getBlockInfo().getCode();
            }
            int layer1 = structure1.getLayer();
            int layer2 = structure2.getLayer();
            if (layer1 / 10 != layer2 / 10) {
                return layer1 / 10 - layer2 / 10;
            }
            Coordinate coordinate1 = BlockUtil.convertWorldCoordinate2Coordinate(regionInfo, block1.getWorldCoordinate());
            Coordinate coordinate2 = BlockUtil.convertWorldCoordinate2Coordinate(regionInfo, block2.getWorldCoordinate());
            BigDecimal minDepth1 = coordinate1.getY()
                    .add(coordinate1.getZ())
                    .add(structure1.getShape().getRadius().getY())
                    .subtract(structure1.getShape().getRadius().getZ());
            BigDecimal minDepth2 = coordinate2.getY()
                    .add(coordinate2.getZ())
                    .add(structure2.getShape().getRadius().getY())
                    .subtract(structure2.getShape().getRadius().getZ());
            int compareMinDepth = minDepth1.compareTo(minDepth2);
            if (compareMinDepth != 0) {
                return compareMinDepth;
            }
            int layerDiff = layer1 % 10 - layer2 % 10;
            if (layerDiff != 0) {
                return layerDiff;
            }
            return block1.getBlockInfo().getId().compareTo(block2.getBlockInfo().getId());
        });
    }
}
