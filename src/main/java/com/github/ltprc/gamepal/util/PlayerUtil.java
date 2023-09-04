package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.SceneModel;

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

    public static int getCoordinateRelation(IntegerCoordinate from, IntegerCoordinate to) {
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

    public static void adjustCoordinate(Coordinate coordinate, int relationValue, BigDecimal height, BigDecimal width) {
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

    public static int ConvertBlockType2Level(int type) {
        switch(type) {
            case GamePalConstants.BLOCK_TYPE_GROUND:
            default:
                return 10;
            case GamePalConstants.BLOCK_TYPE_WALL:
                return 50;
            case GamePalConstants.BLOCK_TYPE_PLAYER:
                return 80;
            case GamePalConstants.BLOCK_TYPE_DROP:
                return 70;
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                return 20;
        }
    }
}
