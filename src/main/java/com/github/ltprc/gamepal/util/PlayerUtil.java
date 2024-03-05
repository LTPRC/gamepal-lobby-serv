package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.*;

import java.math.BigDecimal;

public class PlayerUtil {

    @Deprecated
    public static int getCoordinateRelation(SceneModel from, int toSceneNo) {
        if (null != from.getNorthwest() && toSceneNo == from.getNorthwest()) {
            return 0;
        } else if (null != from.getNorth() && toSceneNo == from.getNorth()) {
            return 1;
        } else if (null != from.getNortheast() && toSceneNo == from.getNortheast()) {
            return 2;
        } else if (null != from.getWest() && toSceneNo == from.getWest()) {
            return 3;
        } else if (null != from.getCenter() && toSceneNo == from.getCenter()) {
            return 4;
        } else if (null != from.getEast() && toSceneNo == from.getEast()) {
            return 5;
        } else if (null != from.getSouthwest() && toSceneNo == from.getSouthwest()) {
            return 6;
        } else if (null != from.getSouth() && toSceneNo == from.getSouth()) {
            return 7;
        } else if (null != from.getSoutheast() && toSceneNo == from.getSoutheast()) {
            return 8;
        } else {
            return -1;
        }
    }

    public static IntegerCoordinate getCoordinateRelation(IntegerCoordinate from, IntegerCoordinate to) {
        return new IntegerCoordinate(to.getX() - from.getX(), to.getY() - from.getY());
    }

    @Deprecated
    public static int getCoordinateRelationOld(IntegerCoordinate from, IntegerCoordinate to) {
        if (from.getY() - to.getY() == 1) {
            if (from.getX() - to.getX() == 1) {
                return 0;
            } else if (from.getX() - to.getX() == 0) {
                return 1;
            } else if (from.getX() - to.getX() == -1) {
                return 2;
            }
        } else if (from.getY() - to.getY() == 0) {
            if (from.getX() - to.getX() == 1) {
                return 3;
            } else if (from.getX() - to.getX() == 0) {
                return 4;
            } else if (from.getX() - to.getX() == -1) {
                return 5;
            }
        } else if (from.getY() - to.getY() == -1) {
            if (from.getX() - to.getX() == 1) {
                return 6;
            } else if (from.getX() - to.getX() == 0) {
                return 7;
            } else if (from.getX() - to.getX() == -1) {
                return 8;
            }
        }
        return -1;
    }

    public static void adjustCoordinate(Coordinate coordinate, IntegerCoordinate integerCoordinate, BigDecimal height, BigDecimal width) {
        // Pos-y is south, neg-y is north
        coordinate.setX(coordinate.getX().add(height.multiply(BigDecimal.valueOf(integerCoordinate.getX()))));
        coordinate.setY(coordinate.getY().add(width.multiply(BigDecimal.valueOf(integerCoordinate.getY()))));
    }

    /**
     * Only support 3*3 matrix
     * @param coordinate
     * @param relationValue
     * @param height
     * @param width
     */
    @Deprecated
    public static void adjustCoordinateOld(Coordinate coordinate, int relationValue, BigDecimal height, BigDecimal width) {
        // Pos-y is south, neg-y is north
        switch (relationValue) {
            case 0:
                coordinate.setY(coordinate.getY().subtract(height));
                coordinate.setX(coordinate.getX().subtract(width));
                break;
            case 1:
                coordinate.setY(coordinate.getY().subtract(height));
                break;
            case 2:
                coordinate.setY(coordinate.getY().subtract(height));
                coordinate.setX(coordinate.getX().add(width));
                break;
            case 3:
                coordinate.setX(coordinate.getX().subtract(width));
                break;
            case 4:
                break;
            case 5:
                coordinate.setX(coordinate.getX().add(width));
                break;
            case 6:
                coordinate.setY(coordinate.getY().add(height));
                coordinate.setX(coordinate.getX().subtract(width));
                break;
            case 7:
                coordinate.setY(coordinate.getY().add(height));
                break;
            case 8:
                coordinate.setY(coordinate.getY().add(height));
                coordinate.setX(coordinate.getX().add(width));
                break;
            case -1:
            default:
                break;
        }
    }

    /**
     * Keep the coordinate inside the range of width multiply height based on its sceneCoordinate.
     * @param worldCoordinate
     */
    public static void fixWorldCoordinate(WorldCoordinate worldCoordinate, Region region) {
        while (worldCoordinate.getCoordinate().getY().compareTo(new BigDecimal(-1)) < 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() - 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().add(new BigDecimal(region.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getY().compareTo(new BigDecimal(region.getHeight() - 1)) >= 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() + 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().subtract(new BigDecimal(region.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(new BigDecimal(-0.5)) < 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() - 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().add(new BigDecimal(region.getWidth())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(new BigDecimal(region.getWidth() - 0.5)) >= 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() + 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().subtract(new BigDecimal(region.getWidth())));
        }
    }

    public static IntegerCoordinate ConvertBlockType2Level(int type) {
        IntegerCoordinate rst = new IntegerCoordinate(0, 0);
        switch (type) {
            case GamePalConstants.BLOCK_TYPE_GROUND:
                rst = new IntegerCoordinate(0, 100);
                break;
            case GamePalConstants.BLOCK_TYPE_WALL:
                rst = new IntegerCoordinate(1, 105);
                break;
            case GamePalConstants.BLOCK_TYPE_PLAYER:
                rst = new IntegerCoordinate(1, 200);
                break;
            case GamePalConstants.BLOCK_TYPE_DROP:
                rst = new IntegerCoordinate(1, 300);
                break;
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                rst = new IntegerCoordinate(0, 200);
                break;
            case GamePalConstants.BLOCK_TYPE_BED:
                rst = new IntegerCoordinate(1, 110);
                break;
            case GamePalConstants.BLOCK_TYPE_TOILET:
                rst = new IntegerCoordinate(1, 111);
                break;
            case GamePalConstants.BLOCK_TYPE_DRESSER:
                rst = new IntegerCoordinate(1, 112);
                break;
            case GamePalConstants.BLOCK_TYPE_WORKSHOP:
                rst = new IntegerCoordinate(1, 113);
                break;
            case GamePalConstants.BLOCK_TYPE_GAME:
                rst = new IntegerCoordinate(1, 114);
                break;
            case GamePalConstants.BLOCK_TYPE_STORAGE:
                rst = new IntegerCoordinate(1, 115);
                break;
            case GamePalConstants.BLOCK_TYPE_COOKER:
                rst = new IntegerCoordinate(1, 116);
                break;
            case GamePalConstants.BLOCK_TYPE_SINK:
                rst = new IntegerCoordinate(1, 117);
                break;
            case GamePalConstants.BLOCK_TYPE_CEILING:
                rst = new IntegerCoordinate(2, 100);
                break;
            case GamePalConstants.BLOCK_TYPE_GROUND_DECORATION:
                rst = new IntegerCoordinate(0, 150);
                break;
            case GamePalConstants.BLOCK_TYPE_WALL_DECORATION:
                rst = new IntegerCoordinate(1, 150);
                break;
            case GamePalConstants.BLOCK_TYPE_CEILING_DECORATION:
                rst = new IntegerCoordinate(2, 150);
                break;
            case GamePalConstants.BLOCK_TYPE_BLOCKED_GROUND:
                rst = new IntegerCoordinate(0, 105);
                break;
            case GamePalConstants.BLOCK_TYPE_HOLLOW_WALL:
                rst = new IntegerCoordinate(1, 100);
                break;
            case GamePalConstants.BLOCK_TYPE_BLOCKED_CEILING:
                rst = new IntegerCoordinate(2, 105);
                break;
            default:
        }
        return rst;
    }

    /**
     * X stands for bottom, center, top, and y stands for detailed height
     * @param type
     * @return
     */
    public static IntegerCoordinate ConvertBlockType2LevelOld(int type) {
        IntegerCoordinate rst = new IntegerCoordinate();
        switch (type) {
            case GamePalConstants.BLOCK_TYPE_GROUND:
            case GamePalConstants.BLOCK_TYPE_GROUND_DECORATION:
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
            case GamePalConstants.BLOCK_TYPE_BLOCKED_GROUND:
                rst.setX(GamePalConstants.LAYER_BOTTOM);
                break;
            case GamePalConstants.BLOCK_TYPE_WALL:
            case GamePalConstants.BLOCK_TYPE_WALL_DECORATION:
            case GamePalConstants.BLOCK_TYPE_PLAYER:
            case GamePalConstants.BLOCK_TYPE_DROP:
            case GamePalConstants.BLOCK_TYPE_HOLLOW_WALL:
            default:
                rst.setX(GamePalConstants.LAYER_CENTER);
                break;
            case GamePalConstants.BLOCK_TYPE_CEILING:
            case GamePalConstants.BLOCK_TYPE_CEILING_DECORATION:
            case GamePalConstants.BLOCK_TYPE_BLOCKED_CEILING:
                rst.setX(GamePalConstants.LAYER_TOP);
                break;
        }
        switch (type) {
            case GamePalConstants.BLOCK_TYPE_GROUND:
            case GamePalConstants.BLOCK_TYPE_WALL:
            case GamePalConstants.BLOCK_TYPE_CEILING:
            case GamePalConstants.BLOCK_TYPE_BLOCKED_GROUND:
            case GamePalConstants.BLOCK_TYPE_HOLLOW_WALL:
            case GamePalConstants.BLOCK_TYPE_BLOCKED_CEILING:
                rst.setY(0);
                break;
            case GamePalConstants.BLOCK_TYPE_GROUND_DECORATION:
            case GamePalConstants.BLOCK_TYPE_WALL_DECORATION:
            case GamePalConstants.BLOCK_TYPE_CEILING_DECORATION:
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
            default:
                rst.setY(10);
                break;
            case GamePalConstants.BLOCK_TYPE_PLAYER:
            case GamePalConstants.BLOCK_TYPE_DROP:
                rst.setY(20);
                break;
        }
        return rst;
    }

    public static Block copyBlock(Block block) {
        switch (block.getType()) {
            case GamePalConstants.BLOCK_TYPE_DROP:
                Drop newDrop = new Drop();
                newDrop.setType(block.getType());
                newDrop.setCode(block.getCode());
                newDrop.setId(block.getId());
                newDrop.setX(block.getX());
                newDrop.setY(block.getY());
                newDrop.setAmount(((Drop) block).getAmount());
                newDrop.setItemNo(((Drop) block).getItemNo());
                return newDrop;
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                Teleport newTeleport = new Teleport();
                newTeleport.setType(block.getType());
                newTeleport.setCode(block.getCode());
                newTeleport.setId(block.getId());
                newTeleport.setX(block.getX());
                newTeleport.setY(block.getY());
                newTeleport.setTo(((Teleport) block).getTo());
                return newTeleport;
            default:
                Block newBlock = new Block();
                newBlock.setType(block.getType());
                newBlock.setCode(block.getCode());
                newBlock.setId(block.getId());
                newBlock.setX(block.getX());
                newBlock.setY(block.getY());
                return newBlock;
        }
    }

    public static Block convertWorldBlock2Block(WorldBlock worldBlock) {
        switch (worldBlock.getType()) {
            case GamePalConstants.BLOCK_TYPE_DROP:
                Drop newDrop = new Drop();
                newDrop.setType(worldBlock.getType());
                newDrop.setCode(worldBlock.getCode());
                newDrop.setId(worldBlock.getId());
                newDrop.setX(worldBlock.getCoordinate().getX());
                newDrop.setY(worldBlock.getCoordinate().getY());
                newDrop.setAmount(((WorldDrop) worldBlock).getAmount());
                newDrop.setItemNo(((WorldDrop) worldBlock).getItemNo());
                return newDrop;
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                Teleport newTeleport = new Teleport();
                newTeleport.setType(worldBlock.getType());
                newTeleport.setCode(worldBlock.getCode());
                newTeleport.setId(worldBlock.getId());
                newTeleport.setX(worldBlock.getCoordinate().getX());
                newTeleport.setY(worldBlock.getCoordinate().getY());
                newTeleport.setTo(((WorldTeleport) worldBlock).getTo());
                return newTeleport;
            default:
                Block newBlock = new Block();
                newBlock.setType(worldBlock.getType());
                newBlock.setCode(worldBlock.getCode());
                newBlock.setId(worldBlock.getId());
                newBlock.setX(worldBlock.getCoordinate().getX());
                newBlock.setY(worldBlock.getCoordinate().getY());
                return newBlock;
        }
    }

    public static Event convertWorldEvent2Event(WorldEvent worldEvent) {
        Event newEvent = new Event();
        newEvent.setUserCode(worldEvent.getUserCode());
        newEvent.setCode(worldEvent.getCode());
        newEvent.setFrame(worldEvent.getFrame());
        newEvent.setX(worldEvent.getCoordinate().getX());
        newEvent.setY(worldEvent.getCoordinate().getY());
        return newEvent;
    }

    public static void copyWorldCoordinate(WorldCoordinate from, WorldCoordinate to) {
        to.setRegionNo(from.getRegionNo());
        Coordinate coordinate = from.getCoordinate();
        if (null != coordinate) {
            to.setCoordinate(new Coordinate(coordinate.getX(), coordinate.getY()));
        } else {
            to.setCoordinate(null);
        }
        IntegerCoordinate sceneCoordinate = from.getSceneCoordinate();
        if (null != sceneCoordinate) {
            to.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY()));
        } else {
            to.setSceneCoordinate(null);
        }
    }

    public static Block generateBlockByEvent(Event event) {
        Block newBlock = new Block();
        newBlock.setX(event.getX());
        newBlock.setY(event.getY());
        newBlock.setId(String.valueOf(event.getFrame()));
        newBlock.setCode(String.valueOf(event.getCode()));
        switch (event.getCode()) {
            case GamePalConstants.EVENT_CODE_HIT:
            case GamePalConstants.EVENT_CODE_HIT_FIRE:
            case GamePalConstants.EVENT_CODE_HIT_ICE:
            case GamePalConstants.EVENT_CODE_HIT_ELECTRICITY:
            case GamePalConstants.EVENT_CODE_UPGRADE:
            case GamePalConstants.EVENT_CODE_FIRE:
            case GamePalConstants.EVENT_CODE_SHOOT:
            case GamePalConstants.EVENT_CODE_BLEED:
            case GamePalConstants.EVENT_CODE_HEAL:
                newBlock.setType(GamePalConstants.BLOCK_TYPE_WALL_DECORATION);
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
            case GamePalConstants.EVENT_CODE_BLOCK:
                newBlock.setType(GamePalConstants.BLOCK_TYPE_CEILING_DECORATION);
                break;
            default:
                newBlock.setType(GamePalConstants.BLOCK_TYPE_WALL_DECORATION);
                break;
        }
        return newBlock;
    }
}
