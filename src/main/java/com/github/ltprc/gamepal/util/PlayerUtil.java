package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
     * @param regionInfo
     */
    public static void fixWorldCoordinate(WorldCoordinate worldCoordinate, RegionInfo regionInfo) {
        while (worldCoordinate.getCoordinate().getY().compareTo(new BigDecimal(-1)) < 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() - 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().add(new BigDecimal(regionInfo.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getY().compareTo(new BigDecimal(regionInfo.getHeight() - 1)) >= 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() + 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().subtract(new BigDecimal(regionInfo.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(new BigDecimal(-0.5)) < 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() - 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().add(new BigDecimal(regionInfo.getWidth())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(new BigDecimal(regionInfo.getWidth() - 0.5)) >= 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() + 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().subtract(new BigDecimal(regionInfo.getWidth())));
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
    public static IntegerCoordinate convertBlockType2LevelOld(int type) {
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
        newEvent.setFrameMax(worldEvent.getFrameMax());
        newEvent.setPeriod(worldEvent.getPeriod());
        newEvent.setX(worldEvent.getCoordinate().getX());
        newEvent.setY(worldEvent.getCoordinate().getY());
        return newEvent;
    }

    public static void copyWorldCoordinate(WorldCoordinate from, WorldCoordinate to) {
        to.setRegionNo(from.getRegionNo());
        Coordinate coordinate = from.getCoordinate();
        if (null != coordinate) {
            to.setCoordinate(new Coordinate(coordinate));
        } else {
            to.setCoordinate(null);
        }
        IntegerCoordinate sceneCoordinate = from.getSceneCoordinate();
        if (null != sceneCoordinate) {
            to.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
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
            case GamePalConstants.EVENT_CODE_EXPLODE:
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_BLEED:
            case GamePalConstants.EVENT_CODE_UPGRADE:
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_DISTURB:
            case GamePalConstants.EVENT_CODE_SACRIFICE:
            case GamePalConstants.EVENT_CODE_TAIL_SMOKE:
                newBlock.setType(GamePalConstants.BLOCK_TYPE_CEILING_DECORATION);
                break;
            case GamePalConstants.EVENT_CODE_HIT:
            case GamePalConstants.EVENT_CODE_HIT_FIRE:
            case GamePalConstants.EVENT_CODE_HIT_ICE:
            case GamePalConstants.EVENT_CODE_HIT_ELECTRICITY:
            case GamePalConstants.EVENT_CODE_FIRE:
            case GamePalConstants.EVENT_CODE_SHOOT:
            default:
                newBlock.setType(GamePalConstants.BLOCK_TYPE_WALL_DECORATION);
                break;
        }
        return newBlock;
    }

    public static BigDecimal calculateHorizontalDistance(RegionInfo regionInfo, WorldCoordinate wc1, WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = new Coordinate();
        c1.setX(wc1.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getX() * regionInfo.getWidth())));
        Coordinate c2 = new Coordinate();
        c2.setX(wc2.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getX() * regionInfo.getWidth())));
        return calculateHorizontalDistance(c1, c2);
    }

    public static BigDecimal calculateHorizontalDistance(Coordinate c1, Coordinate c2) {
        return c2.getX().subtract(c1.getX());
    }

    public static BigDecimal calculateVerticalDistance(RegionInfo regionInfo, WorldCoordinate wc1, WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = new Coordinate();
        c1.setY(wc1.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getY() * regionInfo.getHeight())));
        Coordinate c2 = new Coordinate();
        c2.setY(wc2.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getY() * regionInfo.getHeight())));
        return calculateVerticalDistance(c1, c2);
    }

    public static BigDecimal calculateVerticalDistance(Coordinate c1, Coordinate c2) {
        return c2.getY().subtract(c1.getY());
    }

    public static BigDecimal calculateDistance(RegionInfo regionInfo, WorldCoordinate wc1, WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = new Coordinate();
        c1.setX(wc1.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getX() * regionInfo.getWidth())));
        c1.setY(wc1.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getY() * regionInfo.getHeight())));
        Coordinate c2 = new Coordinate();
        c2.setX(wc2.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getX() * regionInfo.getWidth())));
        c2.setY(wc2.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getY() * regionInfo.getHeight())));
        return calculateDistance(c1, c2);
    }

    public static BigDecimal calculateDistance(Coordinate c1, Coordinate c2) {
        return BigDecimal.valueOf(Math.sqrt(Math.pow(c1.getX().subtract(c2.getX()).doubleValue(), 2)
                + Math.pow(c1.getY().subtract(c2.getY()).doubleValue(), 2)));
    }

    public static BigDecimal calculateAngle(RegionInfo regionInfo, WorldCoordinate wc1, WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = new Coordinate();
        c1.setX(wc1.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getX() * regionInfo.getWidth())));
        c1.setY(wc1.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getY() * regionInfo.getHeight())));
        Coordinate c2 = new Coordinate();
        c2.setX(wc2.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getX() * regionInfo.getWidth())));
        c2.setY(wc2.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getY() * regionInfo.getHeight())));
        return calculateAngle(c1, c2);
    }

    /**
     * calculateAngle from c1 to c2 based on x-axis
     * @param c1
     * @param c2
     * @return in degrees
     */
    public static BigDecimal calculateAngle(Coordinate c1, Coordinate c2) {
        if (c1.getX().equals(c2.getX())) {
            switch (c1.getY().compareTo(c2.getY())) {
                case 1:
                    return BigDecimal.valueOf(90D);
                case -1:
                    return BigDecimal.valueOf(270D);
                default:
                    return BigDecimal.ZERO;
            }
        }
        if (c1.getY().equals(c2.getY())) {
            switch (c1.getX().compareTo(c2.getX())) {
                case 1:
                    return BigDecimal.valueOf(180D);
                default:
                    return BigDecimal.ZERO;
            }
        }
        BigDecimal rst = BigDecimal.valueOf(Math.atan(c2.getY().subtract(c1.getY())
                .divide(c1.getX().subtract(c2.getX()), 2, RoundingMode.HALF_UP).doubleValue()) / Math.PI * 180);
        if (c1.getX().compareTo(c2.getX()) > 0) {
            rst = rst.add(BigDecimal.valueOf(180D));
        }
        if (c1.getX().compareTo(c2.getX()) < 0 && c1.getY().compareTo(c2.getY()) < 0) {
            rst = rst.add(BigDecimal.valueOf(360D));
        }
        return rst;
    }

    public static double compareAnglesInDegrees(double a1, double a2) {
        while (a1 < 0) {
            a1 += 360;
        }
        while (a1 >= 360) {
            a1 -= 360;
        }
        while (a2 < 0) {
            a2 += 360;
        }
        while (a2 >= 360) {
            a2 -= 360;
        }
        return Math.abs(a2 - a1);
    }

    public static BigDecimal calculateBallisticDistance(RegionInfo regionInfo, WorldCoordinate wc1,
                                                        BigDecimal ballisticAngle, WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo() || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = new Coordinate();
        c1.setX(wc1.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getX() * regionInfo.getWidth())));
        c1.setY(wc1.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc1.getSceneCoordinate().getY() * regionInfo.getHeight())));
        Coordinate c2 = new Coordinate();
        c2.setX(wc2.getCoordinate().getX()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getX() * regionInfo.getWidth())));
        c2.setY(wc2.getCoordinate().getY()
                .add(BigDecimal.valueOf(wc2.getSceneCoordinate().getY() * regionInfo.getHeight())));
        return calculateBallisticDistance(c1, ballisticAngle, c2);
    }

    public static BigDecimal calculateBallisticDistance(Coordinate c1, BigDecimal ballisticAngle, Coordinate c2) {
        if (ballisticAngle.compareTo(BigDecimal.valueOf(90D)) == 0
                || ballisticAngle.compareTo(BigDecimal.valueOf(270D)) == 0) {
            return c1.getX().subtract(c2.getX()).abs();
        }
        double slope = -Math.tan(ballisticAngle.doubleValue() / 180 * Math.PI);
        return BigDecimal.valueOf(Math.abs(slope * c2.getX().doubleValue() - c2.getY().doubleValue()
                + c1.getY().doubleValue() - slope * c1.getX().doubleValue()) / Math.sqrt(slope * slope + 1));
    }

    public static List<WorldCoordinate> collectEquidistantPoints(RegionInfo regionInfo, WorldCoordinate wc1,
                                                                 WorldCoordinate wc2, int amount) {
        List<WorldCoordinate> rst = new ArrayList<>();
        if (wc1.getRegionNo() != regionInfo.getRegionNo() || wc2.getRegionNo() != regionInfo.getRegionNo()
                || amount < 2) {
            return rst;
        }
        BigDecimal deltaWidth = calculateHorizontalDistance(regionInfo, wc1, wc2)
                .divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        BigDecimal deltaHeight = calculateVerticalDistance(regionInfo, wc1, wc2)
                .divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        WorldCoordinate wc3 = new WorldCoordinate();
        copyWorldCoordinate(wc1, wc3);
        for (int i = 1; i < amount; i++) {
            wc3.getCoordinate().setX(wc3.getCoordinate().getX().add(deltaWidth));
            wc3.getCoordinate().setY(wc3.getCoordinate().getY().add(deltaHeight));
            fixWorldCoordinate(wc3, regionInfo);
            WorldCoordinate wc4 = new WorldCoordinate();
            copyWorldCoordinate(wc3, wc4);
            rst.add(wc4);
        }
        return rst;
    }

    public static List<Coordinate> collectEquidistantPoints(Coordinate c1, Coordinate c2, int amount) {
        List<Coordinate> rst = new ArrayList<>();
        if (amount < 2) {
            return rst;
        }
        BigDecimal deltaWidth = calculateHorizontalDistance(c1, c2)
                .divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        BigDecimal deltaHeight = calculateVerticalDistance(c1, c2)
                .divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        Coordinate c3 = new Coordinate(c1);
        for (int i = 1; i < amount; i++) {
            c3.setX(c3.getX().add(deltaWidth));
            c3.setY(c3.getY().add(deltaHeight));
            Coordinate c4 = new Coordinate(c3);
            rst.add(c4);
        }
        return rst;
    }
}
