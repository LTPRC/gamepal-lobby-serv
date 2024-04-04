package com.github.ltprc.gamepal.util;

import com.alibaba.fastjson.JSON;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class BlockUtil {

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

    public static Coordinate convertWorldCoordinate2Coordinate(RegionInfo regionInfo, WorldCoordinate worldCoordinate) {
        Coordinate coordinate = new Coordinate(worldCoordinate.getCoordinate());
        adjustCoordinate(coordinate, worldCoordinate.getSceneCoordinate(), regionInfo.getHeight(), regionInfo.getWidth());
        return coordinate;
    }

    public static void adjustCoordinate(Coordinate coordinate, IntegerCoordinate integerCoordinate, int height, int width) {
        // Pos-y is south, neg-y is north
        coordinate.setX(coordinate.getX().add(BigDecimal.valueOf(integerCoordinate.getX() * width)));
        coordinate.setY(coordinate.getY().add(BigDecimal.valueOf(integerCoordinate.getY() * height)));
    }

    /**
     * Keep the coordinate inside the range of width multiply height based on its sceneCoordinate.
     * @param worldCoordinate
     * @param regionInfo
     */
    public static void fixWorldCoordinate(RegionInfo regionInfo, WorldCoordinate worldCoordinate) {
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
            case GamePalConstants.BLOCK_TYPE_TREE:
                rst = new IntegerCoordinate(1, 106);
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
            case GamePalConstants.BLOCK_TYPE_TREE:
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
            case GamePalConstants.BLOCK_TYPE_TREE:
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
        return JSON.parseObject(JSON.toJSONString(block), block.getClass());
    }

    /**
     * Ignore sceneCoordinate
     * @param worldBlock WorldBlock
     * @return Block
     */
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

    public static void copyWorldCoordinate(final WorldCoordinate from, WorldCoordinate to) {
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
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                newBlock.setType(GamePalConstants.BLOCK_TYPE_CEILING_DECORATION);
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_HIT_FIRE:
            case GamePalConstants.EVENT_CODE_HIT_ICE:
            case GamePalConstants.EVENT_CODE_HIT_ELECTRICITY:
            case GamePalConstants.EVENT_CODE_FIRE:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
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
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
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
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return calculateVerticalDistance(c1, c2);
    }

    public static BigDecimal calculateVerticalDistance(Coordinate c1, Coordinate c2) {
        return c2.getY().subtract(c1.getY());
    }

    public static BigDecimal calculateDistance(final RegionInfo regionInfo, final WorldCoordinate wc1,
                                               final WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return calculateDistance(c1, c2);
    }

    public static BigDecimal calculateDistance(final Coordinate c1, final Coordinate c2) {
        return BigDecimal.valueOf(Math.sqrt(Math.pow(c1.getX().subtract(c2.getX()).doubleValue(), 2)
                + Math.pow(c1.getY().subtract(c2.getY()).doubleValue(), 2)));
    }

    public static BigDecimal calculateAngle(final RegionInfo regionInfo, final WorldCoordinate wc1,
                                            final WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return calculateAngle(c1, c2);
    }

    /**
     * calculateAngle from c1 to c2 based on x-axis
     * @param c1
     * @param c2
     * @return in degrees
     */
    public static BigDecimal calculateAngle(final Coordinate c1, final Coordinate c2) {
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
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
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
            fixWorldCoordinate(regionInfo, wc3);
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

    public static List<IntegerCoordinate> preSelectSceneCoordinates(Region region, WorldCoordinate wc1,
                                                                       WorldCoordinate wc2) {
        List<IntegerCoordinate> rst = new ArrayList<>();
        int x1 = Math.min(wc1.getSceneCoordinate().getX(), wc2.getSceneCoordinate().getX());
        int x2 = Math.max(wc1.getSceneCoordinate().getX(), wc2.getSceneCoordinate().getX());
        int y1 = Math.min(wc1.getSceneCoordinate().getY(), wc2.getSceneCoordinate().getY());
        int y2 = Math.max(wc1.getSceneCoordinate().getY(), wc2.getSceneCoordinate().getY());
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                if (region.getScenes().containsKey(sceneCoordinate)) {
                    rst.add(sceneCoordinate);
                }
            }
        }
        return rst;
    }

    public static WorldCoordinate convertCoordinate2WorldCoordinate(RegionInfo regionInfo, IntegerCoordinate sceneCoordinate,
                                                         Coordinate coordinate) {
        WorldCoordinate wc = new WorldCoordinate();
        wc.setRegionNo(regionInfo.getRegionNo());
        wc.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        wc.setCoordinate(new Coordinate(coordinate));
        fixWorldCoordinate(regionInfo, wc);
        return wc;
    }

    public static boolean detectLineSquareCollision(RegionInfo regionInfo, WorldCoordinate wc1,
                                                    BigDecimal ballisticAngle, WorldCoordinate wc2, int blockType) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo() || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return false;
        }
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return detectLineSquareCollision(c1, ballisticAngle, c2, blockType);
    }

    public static boolean detectLineSquareCollision(Coordinate c1, BigDecimal ballisticAngle, Coordinate c2,
                                                    int blockType) {
        double thresholdDistance = 0.5D;
        if (blockType == GamePalConstants.BLOCK_TYPE_PLAYER || blockType == GamePalConstants.BLOCK_TYPE_TREE) {
            thresholdDistance = GamePalConstants.PLAYER_RADIUS.doubleValue();
        }
        if (ballisticAngle.compareTo(BigDecimal.valueOf(90D)) == 0
                || ballisticAngle.compareTo(BigDecimal.valueOf(270D)) == 0) {
            return c1.getX().subtract(c2.getX()).abs().doubleValue() < thresholdDistance;
        }
        double slope = -Math.tan(ballisticAngle.doubleValue() / 180 * Math.PI);
        double yLeft = slope * ((c2.getX().doubleValue() - 0.5D) - thresholdDistance - c1.getX().doubleValue())
                + c1.getY().doubleValue();
        double yRight = slope * ((c2.getX().doubleValue() + 0.5D) - thresholdDistance - c1.getX().doubleValue())
                + c1.getY().doubleValue();
        return !((yLeft - c2.getY().doubleValue() > thresholdDistance
                && yRight - c2.getY().doubleValue() > thresholdDistance)
                || (c2.getY().doubleValue() - yLeft > thresholdDistance
                && c2.getY().doubleValue() - yRight > thresholdDistance));
    }

    /**
     * Detect round collision
     * @param regionInfo regionInfo
     * @param wc1 Start point
     * @param wc2 End point
     * @param wc3 Obstacle center point
     * @param distance min distance between p1/p2 and p3
     * @return whether they collide
     */
    public static boolean detectCollision(RegionInfo regionInfo, WorldCoordinate wc1, WorldCoordinate wc2,
                                          WorldCoordinate wc3, BigDecimal distance) {
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        Coordinate c3 = convertWorldCoordinate2Coordinate(regionInfo, wc3);
        return detectCollision(c1, c2, c3, distance);
    }

    /**
     * Detect round collision
     * @param c1 Start point
     * @param c2 End point
     * @param c3 Obstacle center point
     * @param distance min distance between p1/p2 and p3
     * @return whether they collide
     */
    public static boolean detectCollision(Coordinate c1, Coordinate c2, Coordinate c3, BigDecimal distance) {
        if (Math.sqrt(Math.pow(c3.getX().subtract(c1.getX()).doubleValue(), 2)
                + Math.pow(c3.getY().subtract(c1.getY()).doubleValue(), 2)) < distance.doubleValue()) {
            // Already overlapped
            return false;
        }
        if (Math.sqrt(Math.pow(c3.getX().subtract(c2.getX()).doubleValue(), 2)
                + Math.pow(c3.getY().subtract(c2.getY()).doubleValue(), 2)) < distance.doubleValue()) {
            // Too close
            return true;
        }
        return false;
    }

    /**
     * Detect square collision
     * @param regionInfo regionInfo
     * @param wc1 Start point
     * @param wc2 End point
     * @param wc3 Obstacle center point
     * @param distance min distance between p1/p2 and p3
     * @param sideLength square side size
     * @return whether they collide
     */
    public static boolean detectCollisionSquare(RegionInfo regionInfo, WorldCoordinate wc1, WorldCoordinate wc2,
                                                WorldCoordinate wc3, BigDecimal distance, BigDecimal sideLength) {
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        Coordinate c3 = convertWorldCoordinate2Coordinate(regionInfo, wc3);
        return detectCollisionSquare(c1, c2, c3, distance, sideLength);
    }

    /**
     * Detect square collision
     * @param c1 Start point
     * @param c2 End point
     * @param c3 Obstacle center point
     * @param distance min distance between p1/p2 and p3
     * @param sideLength square side size
     * @return whether they collide
     */
    public static boolean detectCollisionSquare(Coordinate c1, Coordinate c2, Coordinate c3, BigDecimal distance,
                                                BigDecimal sideLength) {
        double newDistance = distance.doubleValue() + sideLength.doubleValue() / 2;
        if (Math.abs(c3.getX().subtract(c1.getX()).doubleValue()) < newDistance
                && Math.abs(c3.getY().subtract(c1.getY()).doubleValue()) < newDistance) {
            // Already overlapped
            return false;
        }
        if (Math.abs(c3.getX().subtract(c2.getX()).doubleValue()) < newDistance
                && Math.abs(c3.getY().subtract(c2.getY()).doubleValue()) <= newDistance) {
            // Too close
            return true;
        }
        return false;
    }

    public static boolean checkBlockSolid(int blockType) {
        switch (blockType) {
            case GamePalConstants.BLOCK_TYPE_GROUND:
            case GamePalConstants.BLOCK_TYPE_DROP:
            case GamePalConstants.BLOCK_TYPE_GROUND_DECORATION:
            case GamePalConstants.BLOCK_TYPE_WALL_DECORATION:
            case GamePalConstants.BLOCK_TYPE_CEILING_DECORATION:
            case GamePalConstants.BLOCK_TYPE_HOLLOW_WALL:
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                return false;
            case GamePalConstants.BLOCK_TYPE_PLAYER:
            case GamePalConstants.BLOCK_TYPE_TREE:
            default:
                return true;
        }
    }

    public static Queue<Block> createRankingQueue() {
        Queue<Block> rankingQueue = new PriorityQueue<>((o1, o2) -> {
            IntegerCoordinate level1 = BlockUtil.ConvertBlockType2Level(o1.getType());
            IntegerCoordinate level2 = BlockUtil.ConvertBlockType2Level(o2.getType());
            if (!Objects.equals(level1.getX(), level2.getX())) {
                return level1.getX() - level2.getX();
            }
            // Please use equals() instead of == 24/02/10
            if (!o1.getY().equals(o2.getY())) {
                return o1.getY().compareTo(o2.getY());
            }
            return level1.getY() - level2.getY();
        });
        return rankingQueue;
    }

    public static WorldBlock createEventBlock(RegionInfo regionInfo, PlayerInfo playerInfo, int eventType,
                                       int eventLocationType) {
        WorldBlock eventBlock = new WorldBlock();
        eventBlock.setType(eventType);
        eventBlock.setCode(playerInfo.getId());
        eventBlock.setRegionNo(playerInfo.getRegionNo());
        IntegerCoordinate newSceneCoordinate = new IntegerCoordinate();
        newSceneCoordinate.setX(playerInfo.getSceneCoordinate().getX());
        newSceneCoordinate.setY(playerInfo.getSceneCoordinate().getY());
        eventBlock.setSceneCoordinate(newSceneCoordinate);
        // BigDecimal is immutable, no need to copy a new BigDecimal instance 24/04/04
        eventBlock.setCoordinate(new Coordinate(playerInfo.getCoordinate()));
        switch (eventLocationType) {
            case GamePalConstants.EVENT_LOCATION_TYPE_ADJACENT:
                break;
            case GamePalConstants.EVENT_LOCATION_TYPE_MELEE:
                eventBlock.getCoordinate().setX(eventBlock.getCoordinate().getX()
                        .add(BigDecimal.valueOf((Math.random())
                                * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                eventBlock.getCoordinate().setY(eventBlock.getCoordinate().getY()
                        .subtract(BigDecimal.valueOf((Math.random())
                                * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                break;
            case GamePalConstants.EVENT_LOCATION_TYPE_SHOOT:
                eventBlock.getCoordinate().setX(eventBlock.getCoordinate().getX()
                        .add(BigDecimal.valueOf((Math.random()
                                + GamePalConstants.EVENT_MAX_DISTANCE_SHOOT.intValue())
                                * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                eventBlock.getCoordinate().setY(eventBlock.getCoordinate().getY()
                        .subtract(BigDecimal.valueOf((Math.random()
                                + GamePalConstants.EVENT_MAX_DISTANCE_SHOOT.intValue())
                                * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                break;
        }
        BlockUtil.fixWorldCoordinate(regionInfo, eventBlock);
        return eventBlock;
    }
}
