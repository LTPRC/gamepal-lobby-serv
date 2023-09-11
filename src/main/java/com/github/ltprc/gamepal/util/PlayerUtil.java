package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.model.map.world.WorldDrop;
import com.github.ltprc.gamepal.model.map.world.WorldTeleport;

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
        coordinate.setX(coordinate.getX().add(width.multiply(BigDecimal.valueOf(integerCoordinate.getX()))));
        coordinate.setY(coordinate.getY().add(width.multiply(BigDecimal.valueOf(integerCoordinate.getY()))));
    }

    /**
     * Only support 3*3 matrix
     * @param coordinate
     * @param relationValue
     * @param height
     * @param width
     */
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
     * X stands for bottom, center, top, and y stands for detailed height
     * @param type
     * @return
     */
    public static IntegerCoordinate ConvertBlockType2Level(int type) {
        IntegerCoordinate rst = new IntegerCoordinate();
        switch(type) {
            case GamePalConstants.BLOCK_TYPE_GROUND:
            case GamePalConstants.BLOCK_TYPE_GROUND_DECORATION:
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                rst.setX(GamePalConstants.LAYER_BOTTOM);
                break;
            case GamePalConstants.BLOCK_TYPE_WALL:
            case GamePalConstants.BLOCK_TYPE_WALL_DECORATION:
            case GamePalConstants.BLOCK_TYPE_PLAYER:
            case GamePalConstants.BLOCK_TYPE_DROP:
            default:
                rst.setX(GamePalConstants.LAYER_CENTER);
                break;
            case GamePalConstants.BLOCK_TYPE_CEILING:
            case GamePalConstants.BLOCK_TYPE_CEILING_DECORATION:
                rst.setX(GamePalConstants.LAYER_TOP);
                break;
        }
        switch(type) {
            case GamePalConstants.BLOCK_TYPE_GROUND:
            case GamePalConstants.BLOCK_TYPE_WALL:
            case GamePalConstants.BLOCK_TYPE_CEILING:
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
}
