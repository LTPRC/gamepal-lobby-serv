package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.model.creature.PerceptionInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.structure.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class BlockUtil {

    private BlockUtil() {}

    public static IntegerCoordinate getCoordinateRelation(IntegerCoordinate from, IntegerCoordinate to) {
        return new IntegerCoordinate(to.getX() - from.getX(), to.getY() - from.getY());
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
        while (worldCoordinate.getCoordinate().getX().compareTo(BigDecimal.valueOf(-0.5D)) < 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() - 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().add(new BigDecimal(regionInfo.getWidth())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(BigDecimal.valueOf(regionInfo.getWidth() - 0.5)) >= 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() + 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().subtract(new BigDecimal(regionInfo.getWidth())));
        }
    }

    public static void fixWorldCoordinateReal(RegionInfo regionInfo, WorldCoordinate worldCoordinate) {
        while (worldCoordinate.getCoordinate().getY().compareTo(BigDecimal.ZERO) < 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() - 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().add(new BigDecimal(regionInfo.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getY().compareTo(new BigDecimal(regionInfo.getHeight())) >= 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() + 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().subtract(new BigDecimal(regionInfo.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(BigDecimal.ZERO) < 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() - 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().add(new BigDecimal(regionInfo.getWidth())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(BigDecimal.valueOf(regionInfo.getWidth())) >= 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() + 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().subtract(new BigDecimal(regionInfo.getWidth())));
        }
    }

    public static Coordinate convertWorldCoordinate2Coordinate(RegionInfo regionInfo, WorldCoordinate worldCoordinate) {
        Coordinate coordinate = new Coordinate(worldCoordinate.getCoordinate());
        adjustCoordinate(coordinate, worldCoordinate.getSceneCoordinate(), regionInfo.getHeight(), regionInfo.getWidth());
        return coordinate;
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
        double deltaX = c2.getX().subtract(c1.getX()).doubleValue();
        double deltaY = c2.getY().subtract(c1.getY()).doubleValue();
        double rst = Math.acos(deltaX / Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2))) / Math.PI * 180;;
        if (deltaY > 0) {
            rst = 360 - rst;
        } else if (deltaY == 0) {
            if (deltaX > 0) {
                rst = 0;
            } else if (deltaX < 0) {
                rst = 180;
            } else {
                rst = 270;
            }
        }
        return BigDecimal.valueOf(rst);
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
        double rst = Math.abs(a2 - a1);
        return rst >= 180D ? 360D - rst : rst;
    }

    public static List<WorldCoordinate> collectEquidistantPoints(RegionInfo regionInfo, WorldCoordinate wc1,
                                                                 WorldCoordinate wc2, int amount) {
        List<WorldCoordinate> rst = new ArrayList<>();
        if (amount < 2) {
            return rst;
        }
        BigDecimal deltaWidth = calculateHorizontalDistance(regionInfo, wc1, wc2);
        BigDecimal deltaHeight = calculateVerticalDistance(regionInfo, wc1, wc2);
        if (null == deltaWidth || null == deltaHeight) {
            return rst;
        }
        deltaWidth = deltaWidth.divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        deltaHeight = deltaHeight.divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        WorldCoordinate wc3 = new WorldCoordinate(wc1);
        for (int i = 1; i < amount; i++) {
            wc3.getCoordinate().setX(wc3.getCoordinate().getX().add(deltaWidth));
            wc3.getCoordinate().setY(wc3.getCoordinate().getY().add(deltaHeight));
            fixWorldCoordinate(regionInfo, wc3);
            rst.add(new WorldCoordinate(wc3));
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
            rst.add(new Coordinate(c3));
        }
        return rst;
    }

    public static Set<IntegerCoordinate> preSelectSceneCoordinates(Region region, WorldCoordinate wc1,
                                                                    WorldCoordinate wc2) {
        Set<IntegerCoordinate> rst = new HashSet<>();
        if (wc1.getRegionNo() != region.getRegionNo() || wc2.getRegionNo() != region.getRegionNo()) {
            return rst;
        }
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

    /**
     *
     * @param regionInfo
     * @param from
     * @param block1
     * @param block2
     * @param correctBlock1
     * @return
     */
    public static boolean detectLineCollision(RegionInfo regionInfo, WorldCoordinate from, Block block1,
                                              Block block2, boolean correctBlock1) {
        if (from.getRegionNo() != regionInfo.getRegionNo()
                || block1.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()
                || block2.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()) {
            return false;
        }
//        BigDecimal eventDirection =
//                BlockUtil.calculateAngle(from.getCoordinate(), block1.getWorldCoordinate().getCoordinate());
        Coordinate coordinate0 = convertWorldCoordinate2Coordinate(regionInfo, from);
        Coordinate coordinate1 = convertWorldCoordinate2Coordinate(regionInfo, block1.getWorldCoordinate());
        Coordinate coordinate2 = convertWorldCoordinate2Coordinate(regionInfo, block2.getWorldCoordinate());
//        if (eventDirection.compareTo(BigDecimal.valueOf(90D)) == 0
//                || eventDirection.compareTo(BigDecimal.valueOf(270D)) == 0) {
//            boolean rst = coordinate1.getX().subtract(coordinate2.getX()).abs().doubleValue()
//                    < block1.getBlockInfo().getStructure().getShape().getRadius().getX()
//                    .add(block2.getBlockInfo().getStructure().getShape().getRadius().getX()).doubleValue();
//            if (rst && correctBlock1) {
//                block1.getWorldCoordinate().getCoordinate().setX(block1.getWorldCoordinate().getCoordinate().getX()
//                        .subtract(coordinate1.getX()).add(coordinate3.getX()));
//                block1.getWorldCoordinate().getCoordinate().setY(block1.getWorldCoordinate().getCoordinate().getY()
//                        .subtract(coordinate1.getY()).add(coordinate3.getY()));
//            }
//            return rst;
//        }
        Coordinate coordinate3 = findClosestPoint(coordinate0, coordinate1, coordinate2);
        Block block3 = new Block(block1);
        WorldCoordinate worldCoordinate3 = BlockUtil.locateCoordinateWithDirectionAndDistance(regionInfo,
                block1.getWorldCoordinate(),
                BlockUtil.calculateAngle(coordinate1, coordinate3), BlockUtil.calculateDistance(coordinate1, coordinate3));
        block3.setWorldCoordinate(worldCoordinate3);
        if (detectCollision(regionInfo, block3, block2)) {
            if (correctBlock1) {
                block1.setWorldCoordinate(block3.getWorldCoordinate());
            }
            return true;
        }
        return false;
    }

    /**
     * 通义千问
     * @param coordinate0
     * @param coordinate1
     * @param coordinate2
     * @return
     */
    public static Coordinate findClosestPoint(Coordinate coordinate0, Coordinate coordinate1, Coordinate coordinate2) {
        Coordinate segmentVector = new Coordinate(coordinate1.getX().subtract(coordinate0.getX()), coordinate1.getY().subtract(coordinate0.getY()));
        Coordinate pointVector = new Coordinate(coordinate2.getX().subtract(coordinate0.getX()), coordinate2.getY().subtract(coordinate0.getY()));

        BigDecimal segmentLengthSquared = segmentVector.getX().pow(2).add(segmentVector.getY().pow(2));
        if (segmentLengthSquared.equals(BigDecimal.ZERO)) {
            return new Coordinate(coordinate0);
        }
        BigDecimal dotProduct = dotProduct(segmentVector, pointVector);

        BigDecimal t;
        try {
            t = dotProduct.divide(segmentLengthSquared, RoundingMode.HALF_UP);
        } catch (ArithmeticException ignored) {
            t = BigDecimal.ONE.negate();
        }
        if (t.compareTo(BigDecimal.ZERO) < 0) {
            return coordinate0; // 投影点在线段外，取起点
        } else if (t.compareTo(BigDecimal.ONE) > 0) {
            return coordinate1; // 投影点在线段外，取终点
        } else {
            BigDecimal newX = coordinate0.getX().add(segmentVector.getX().multiply(t));
            BigDecimal newY = coordinate0.getY().add(segmentVector.getY().multiply(t));
            return new Coordinate(newX.round(new MathContext(3, RoundingMode.FLOOR)),
                    newY.round(new MathContext(3, RoundingMode.FLOOR)));
        }
    }

    /**
     * 通义千问
     * @param coordinate1
     * @param coordinate2
     * @return
     */
    public static BigDecimal dotProduct(Coordinate coordinate1, Coordinate coordinate2) {
        return coordinate1.getX().multiply(coordinate2.getX()).add(coordinate1.getY().multiply(coordinate2.getY()));
    }

    /**
     * No need to consider structure's center
     * @param c1
     * @param ballisticAngle
     * @param c2
     * @return
     */
    public static BigDecimal calculateBallisticDistance(Coordinate c1, BigDecimal ballisticAngle, Coordinate c2) {
        if (ballisticAngle.compareTo(BigDecimal.valueOf(90D)) == 0
                || ballisticAngle.compareTo(BigDecimal.valueOf(270D)) == 0) {
            return c1.getX().subtract(c2.getX()).abs();
        }
        double slope = -Math.tan(ballisticAngle.doubleValue() / 180 * Math.PI);
        return BigDecimal.valueOf(Math.abs(-slope * c2.getX().doubleValue() - c2.getY().doubleValue()
                + c1.getY().doubleValue() + slope * c1.getX().doubleValue()) / Math.sqrt(slope * slope + 1));
    }

    public static boolean detectCollision(RegionInfo regionInfo, Block block1, Block block2) {
        Coordinate coordinate1 = convertWorldCoordinate2Coordinate(regionInfo, block1.getWorldCoordinate());
        Coordinate coordinate2 = convertWorldCoordinate2Coordinate(regionInfo, block2.getWorldCoordinate());
        Shape shape1 = block1.getBlockInfo().getStructure().getShape();
        Shape shape2 = block2.getBlockInfo().getStructure().getShape();
        if (BlockConstants.STRUCTURE_SHAPE_TYPE_SQUARE == shape1.getShapeType()) {
            shape1.setShapeType(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE);
            shape1.getRadius().setY(shape1.getRadius().getX());
        }
        if (BlockConstants.STRUCTURE_SHAPE_TYPE_SQUARE == shape2.getShapeType()) {
            shape2.setShapeType(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE);
            shape2.getRadius().setY(shape2.getRadius().getX());
        }
        // Round vs. round
        if (BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND == shape1.getShapeType()
                && BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND == shape2.getShapeType()) {
            return BlockUtil.calculateDistance(coordinate1, coordinate2).doubleValue()
                    < shape1.getRadius().getX().add(shape2.getRadius().getX()).doubleValue();
        }
        // Rectangle vs. rectangle
        if (BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE == shape1.getShapeType()
                && BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE == shape2.getShapeType()) {
            return Math.abs(coordinate1.getX().subtract(coordinate2.getX()).doubleValue())
                    < shape1.getRadius().getX().add(shape2.getRadius().getX()).doubleValue()
                    && Math.abs(coordinate1.getY().subtract(coordinate2.getY()).doubleValue())
                    < shape1.getRadius().getY().add(shape2.getRadius().getY()).doubleValue();
        }
        // Round vs. rectangle
        if (BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND == shape2.getShapeType()) {
            return detectCollision(regionInfo, block2, block1);
        }
//        return (coordinate1.getX().add(shape1.getRadius().getX()).doubleValue() >= coordinate2.getX().subtract(shape2.getRadius().getX()).doubleValue()
//                || coordinate1.getX().subtract(shape1.getRadius().getX()).doubleValue() <= coordinate2.getX().add(shape2.getRadius().getX()).doubleValue())
//                && (coordinate1.getY().add(shape1.getRadius().getY()).doubleValue() >= coordinate2.getY().subtract(shape2.getRadius().getY()).doubleValue()
//                || coordinate1.getY().subtract(shape1.getRadius().getY()).doubleValue() <= coordinate2.getY().add(shape2.getRadius().getY()).doubleValue());
        return ((coordinate1.getX().doubleValue() > coordinate2.getX().subtract(shape2.getRadius().getX()).doubleValue()
                && coordinate1.getX().doubleValue() < coordinate2.getX().add(shape2.getRadius().getX()).doubleValue()
                && coordinate1.getY().add(shape1.getRadius().getY()).doubleValue() > coordinate2.getY().subtract(shape2.getRadius().getY()).doubleValue()
                && coordinate1.getY().subtract(shape1.getRadius().getY()).doubleValue() < coordinate2.getY().add(shape2.getRadius().getY()).doubleValue())
                || (coordinate1.getX().add(shape1.getRadius().getX()).doubleValue() > coordinate2.getX().subtract(shape2.getRadius().getX()).doubleValue()
                && coordinate1.getX().subtract(shape1.getRadius().getX()).doubleValue() < coordinate2.getX().add(shape2.getRadius().getX()).doubleValue()
                && coordinate1.getY().doubleValue() > coordinate2.getY().subtract(shape2.getRadius().getY()).doubleValue()
                && coordinate1.getY().doubleValue() < coordinate2.getY().add(shape2.getRadius().getY()).doubleValue()));
//                && (); // 4 apexes
    }

    public static boolean checkBlockTypeInteractive(int blockType) {
        switch (blockType) {
            case BlockConstants.BLOCK_TYPE_NORMAL:
            case BlockConstants.BLOCK_TYPE_EFFECT:
            case BlockConstants.BLOCK_TYPE_DROP:
            case BlockConstants.BLOCK_TYPE_TELEPORT:
            case BlockConstants.BLOCK_TYPE_BUILDING:
            case BlockConstants.BLOCK_TYPE_TREE:
                return false;
            default:
                return true;
        }
    }

    public static Queue<Block> createRankingQueue() {
        return new PriorityQueue<>((o1, o2) -> {
            if (!Objects.equals(o1.getBlockInfo().getStructure().getLayer() / 10,
                    o2.getBlockInfo().getStructure().getLayer() / 10)) {
                return o1.getBlockInfo().getStructure().getLayer() / 10
                        - o2.getBlockInfo().getStructure().getLayer() / 10;
            }
            if (!Objects.equals(o1.getWorldCoordinate().getSceneCoordinate().getY(),
                    o2.getWorldCoordinate().getSceneCoordinate().getY())) {
                return o1.getWorldCoordinate().getSceneCoordinate().getY()
                        .compareTo(o2.getWorldCoordinate().getSceneCoordinate().getY());
            }
            if (!Objects.equals(o1.getWorldCoordinate().getCoordinate().getY(),
                    o2.getWorldCoordinate().getCoordinate().getY())) {
                return o1.getWorldCoordinate().getCoordinate().getY()
                        .compareTo(o2.getWorldCoordinate().getCoordinate().getY());
            }
            return o1.getBlockInfo().getStructure().getLayer() % 10 - o2.getBlockInfo().getStructure().getLayer() % 10;
        });
    }

    public static WorldCoordinate locateCoordinateWithDirectionAndDistance(RegionInfo regionInfo,
                                                                           WorldCoordinate worldCoordinate,
                                                                           BigDecimal direction, BigDecimal distance) {
        WorldCoordinate rst = new WorldCoordinate(worldCoordinate);
        rst.setCoordinate(locateCoordinateWithDirectionAndDistance(rst.getCoordinate(), direction, distance));
        BlockUtil.fixWorldCoordinate(regionInfo, rst);
        return rst;
    }

    public static Coordinate locateCoordinateWithDirectionAndDistance(Coordinate coordinate, BigDecimal direction,
                                                                      BigDecimal distance) {
        double angle = direction.doubleValue() / 180 * Math.PI;
        return new Coordinate(coordinate.getX().add(BigDecimal.valueOf(distance.doubleValue() * Math.cos(angle))),
                coordinate.getY().subtract(BigDecimal.valueOf(distance.doubleValue() * Math.sin(angle))));
    }

    public static IntegerCoordinate convertCoordinate2BasicIntegerCoordinate(WorldCoordinate worldCoordinate) {
        return new IntegerCoordinate(
                worldCoordinate.getCoordinate().getX().intValue(),
                worldCoordinate.getCoordinate().getY().intValue());
    }

    public static IntegerCoordinate convertCoordinate2ClosestIntegerCoordinate(WorldCoordinate worldCoordinate) {
        return new IntegerCoordinate(
                worldCoordinate.getCoordinate().getX().add(BigDecimal.valueOf(0.5D)).intValue(),
                worldCoordinate.getCoordinate().getY().add(BigDecimal.valueOf(0.5D)).intValue());
    }

    public static int convertEventCode2Layer(int eventCode) {
        int layer;
        switch (eventCode) {
            case GamePalConstants.EVENT_CODE_EXPLODE:
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_BLEED:
            case GamePalConstants.EVENT_CODE_UPGRADE:
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_DISTURB:
            case GamePalConstants.EVENT_CODE_SACRIFICE:
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                layer = BlockConstants.STRUCTURE_LAYER_TOP_DECORATION;
                break;
            default:
                layer = BlockConstants.STRUCTURE_LAYER_MIDDLE_DECORATION;
                break;
        }
        return layer;
    }

    public static void updatePerceptionInfo(PerceptionInfo perceptionInfo, int worldTime) {
        BigDecimal visionRadius = CreatureConstants.DEFAULT_NIGHT_VISION_RADIUS;
        if (worldTime >= GamePalConstants.WORLD_TIME_SUNRISE_BEGIN
                && worldTime < GamePalConstants.WORLD_TIME_SUNRISE_END) {
            visionRadius = visionRadius.add(BigDecimal.valueOf(CreatureConstants.DEFAULT_DAYTIME_VISION_RADIUS
                    .subtract(CreatureConstants.DEFAULT_NIGHT_VISION_RADIUS).doubleValue()
                    * (worldTime - GamePalConstants.WORLD_TIME_SUNRISE_BEGIN)
                    / (GamePalConstants.WORLD_TIME_SUNRISE_END - GamePalConstants.WORLD_TIME_SUNRISE_BEGIN)));
        } else if (worldTime >= GamePalConstants.WORLD_TIME_SUNSET_BEGIN
                && worldTime < GamePalConstants.WORLD_TIME_SUNSET_END) {
            visionRadius = visionRadius.add(BigDecimal.valueOf(CreatureConstants.DEFAULT_DAYTIME_VISION_RADIUS
                    .subtract(CreatureConstants.DEFAULT_NIGHT_VISION_RADIUS).doubleValue()
                    * (GamePalConstants.WORLD_TIME_SUNSET_END - worldTime)
                    / (GamePalConstants.WORLD_TIME_SUNSET_END - GamePalConstants.WORLD_TIME_SUNSET_BEGIN)));
        } else if (worldTime >= GamePalConstants.WORLD_TIME_SUNRISE_END
                && worldTime < GamePalConstants.WORLD_TIME_SUNSET_BEGIN) {
            visionRadius = CreatureConstants.DEFAULT_DAYTIME_VISION_RADIUS;
        }
        perceptionInfo.setDistinctVisionRadius(visionRadius);
        perceptionInfo.setIndistinctVisionRadius(perceptionInfo.getDistinctVisionRadius()
                .multiply(BigDecimal.valueOf(1.2)));
        perceptionInfo.setDistinctVisionAngle(CreatureConstants.DEFAULT_DISTINCT_VISION_ANGLE);
        perceptionInfo.setIndistinctVisionAngle(CreatureConstants.DEFAULT_INDISTINCT_VISION_ANGLE);
        perceptionInfo.setDistinctHearingRadius(CreatureConstants.DEFAULT_DISTINCT_HEARING_RADIUS);
        perceptionInfo.setIndistinctHearingRadius(CreatureConstants.DEFAULT_INDISTINCT_HEARING_RADIUS);
    }

    public static boolean checkPerceptionCondition(final RegionInfo regionInfo, final Block player1,
                                                   final PerceptionInfo perceptionInfo1, final Block block2) {
        if (player1.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_PLAYER) {
            return true;
        }
        if (player1.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()
                || block2.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()) {
            return false;
        }
        BigDecimal distance = BlockUtil.calculateDistance(regionInfo, player1.getWorldCoordinate(), block2.getWorldCoordinate());
        BigDecimal angle = BlockUtil.calculateAngle(regionInfo, player1.getWorldCoordinate(), block2.getWorldCoordinate());
        if (null == distance || null == angle) {
            return false;
        }
        if (distance.compareTo(perceptionInfo1.getDistinctHearingRadius()) <= 0) {
            return true;
        }
        if (block2.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER) {
            return distance.compareTo(perceptionInfo1.getDistinctVisionRadius()) <= 0
                    && BlockUtil.compareAnglesInDegrees(angle.doubleValue(),
                    player1.getMovementInfo().getFaceDirection().doubleValue())
                    < perceptionInfo1.getDistinctVisionAngle().doubleValue() / 2;
        } else {
            return distance.compareTo(perceptionInfo1.getIndistinctVisionRadius()) <= 0;
        }
    }

    public static void calculateMaxSpeed(MovementInfo movementInfo) {
        BigDecimal maxSpeed = BlockConstants.MAX_SPEED_DEFAULT;
        switch (movementInfo.getFloorCode()) {
            case BlockConstants.BLOCK_CODE_SWAMP:
                maxSpeed = maxSpeed.multiply(BigDecimal.valueOf(0.2));
                break;
            case BlockConstants.BLOCK_CODE_SAND:
                maxSpeed = maxSpeed.multiply(BigDecimal.valueOf(0.4));
                break;
            case BlockConstants.BLOCK_CODE_SNOW:
            case BlockConstants.BLOCK_CODE_LAVA:
                maxSpeed = maxSpeed.multiply(BigDecimal.valueOf(0.6));
                break;
            case BlockConstants.BLOCK_CODE_ROUGH:
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
            case BlockConstants.BLOCK_CODE_WATER:
                maxSpeed = maxSpeed.multiply(BigDecimal.valueOf(0.8));
                break;
            case BlockConstants.BLOCK_CODE_NOTHING:
            case BlockConstants.BLOCK_CODE_GRASS:
            case BlockConstants.BLOCK_CODE_DIRT:
            default:
                break;
        }
        movementInfo.setMaxSpeed(maxSpeed);
    }

    public static boolean checkMaterialCollision(int structureMaterial1, int structureMaterial2) {
        switch (structureMaterial1) {
            case BlockConstants.STRUCTURE_MATERIAL_FLESH:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_FLESH;
            case BlockConstants.STRUCTURE_MATERIAL_MAGNUM:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_MAGNUM;
            case BlockConstants.STRUCTURE_MATERIAL_PLASMA:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID;
            case BlockConstants.STRUCTURE_MATERIAL_SOLID:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_MAGNUM
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_PLASMA;
            case BlockConstants.STRUCTURE_MATERIAL_HOLLOW:
            default:
                return false;
        }
    }

    public static BlockInfo generateSceneObjectBlockInfo(int blockCode) {
        BlockInfo blockInfo = null;
        String id = UUID.randomUUID().toString();
        Shape roundShape = new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        switch (blockCode) {
            case BlockConstants.PLANT_INDEX_BIG_PINE:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-0",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))));
                break;
            case BlockConstants.PLANT_INDEX_BIG_OAK:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-0",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))));
                break;
            case BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-0",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))));
                break;
            case BlockConstants.PLANT_INDEX_PINE:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-2",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))));
                break;
            case BlockConstants.PLANT_INDEX_OAK:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-2",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))));
                break;
            case BlockConstants.PLANT_INDEX_WITHERED_TREE:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-2",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))));
                break;
            case BlockConstants.PLANT_INDEX_PALM:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-2",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))));
                break;
            case BlockConstants.PLANT_INDEX_RAFFLESIA:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-4",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_STUMP:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-4",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape));
                break;
            case BlockConstants.PLANT_INDEX_MOSSY_STUMP:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-4",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape));
                break;
            case BlockConstants.PLANT_INDEX_HOLLOW_TRUNK:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-4",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape));
                break;
            case BlockConstants.PLANT_INDEX_FLOWER_BUSH:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-4",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.PLANT_INDEX_BUSH:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-4",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.PLANT_INDEX_SMALL_FLOWER_1:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-5",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_SMALL_FLOWER_2:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-5",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_SMALL_FLOWER_3:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-5",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_BIG_FLOWER_1:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-5",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_BIG_FLOWER_2:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-5",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_BIG_FLOWER_3:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-5",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_MUSHROOM_1:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-6",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_MUSHROOM_2:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-6",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_GRASS_1:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-7",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_GRASS_2:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-7",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_GRASS_3:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-7",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_GRASS_4:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-7",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM));
                break;
            case BlockConstants.PLANT_INDEX_CACTUS_1:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-8",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.PLANT_INDEX_CACTUS_2:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-8",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.PLANT_INDEX_CACTUS_3:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id,
                        BlockConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-8",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.ROCK_INDEX_1:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_BUILDING, id,
                        BlockConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape));
                break;
            case BlockConstants.ROCK_INDEX_2:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_BUILDING, id,
                        BlockConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                BlockConstants.STRUCTURE_LAYER_BOTTOM,
                                roundShape));
                break;
            default:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, id, "1000",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
        }
        return blockInfo;
    }

    public static String convertBlockType2ItemNo(int type) {
        return BlockConstants.BLOCK_TYPE_ITEM_NO_MAP.get(type);
    }

    public static Integer convertItemNo2BlockType(String itemNo) {
        return BlockConstants.ITEM_NO_BLOCK_TYPE_MAP.get(itemNo);
    }

    public static BlockInfo generateBlockInfo(int type) {
        BlockInfo blockInfo = null;
        switch (type) {
            case BlockConstants.BLOCK_TYPE_NORMAL:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, "", "1000",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE_DECORATION));
                break;
//            case BlockConstants.BLOCK_TYPE_EVENT:
//                // TODO
//                break;
            case BlockConstants.BLOCK_TYPE_PLAYER:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_PLAYER, "", "1000",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_FLESH,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BlockConstants.PLAYER_RADIUS, BlockConstants.PLAYER_RADIUS))));
                break;
            case BlockConstants.BLOCK_TYPE_BED:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_BED, "", "3006",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_TOILET:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TOILET, "", "3008",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_DRESSER:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_DRESSER, "", "3010",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_GAME:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_GAME, "", "3021",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_MAGNUM, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_STORAGE:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_STORAGE, "", "3002",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_COOKER:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_COOKER, "", "3004",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_SINK:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_SINK, "", "3005",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_CONTAINER:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_CONTAINER, "", "3001",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_RADIO:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_RADIO, "", "1000",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_MAGNUM, BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BlockConstants.PLAYER_RADIUS, BlockConstants.PLAYER_RADIUS))));
                break;
            case BlockConstants.BLOCK_TYPE_BUILDING:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_BUILDING, "", "1000",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_TREE:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_TREE, "", "1000",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_WORKSHOP, "", "4001",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL, "", "4002",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO, "", "4003",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT, "", "4004",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM, "", "4005",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE, "", "4006",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE));
                break;
            default:
                break;
        }
        return blockInfo;
    }
}
