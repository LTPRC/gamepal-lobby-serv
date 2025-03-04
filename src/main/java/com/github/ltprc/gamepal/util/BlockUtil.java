package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.model.creature.PerceptionInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
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

    public static Coordinate adjustCoordinate(Coordinate coordinate, IntegerCoordinate integerCoordinate, int height,
                                              int width) {
        // Pos-y is south, neg-y is north
        return new Coordinate(coordinate.getX().add(BigDecimal.valueOf(
                integerCoordinate.getX()).multiply(BigDecimal.valueOf(width))),
                coordinate.getY().add(BigDecimal.valueOf(
                        integerCoordinate.getY()).multiply(BigDecimal.valueOf(height))));
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
        return adjustCoordinate(worldCoordinate.getCoordinate(), worldCoordinate.getSceneCoordinate(),
                regionInfo.getHeight(), regionInfo.getWidth());
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

    public static BigDecimal calculateHorizontalDistance(RegionInfo regionInfo, WorldCoordinate wc1,
                                                         WorldCoordinate wc2) {
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

    public static BigDecimal calculateVerticalDistance(RegionInfo regionInfo, WorldCoordinate wc1,
                                                       WorldCoordinate wc2) {
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
        for (int i = x1 - 1; i <= x2 + 1; i++) {
            for (int j = y1 - 1; j <= y2 + 1; j++) {
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
        BlockUtil.copyWorldCoordinate(worldCoordinate3, block3.getWorldCoordinate());
        if (detectCollision(regionInfo, block3, block2)) {
            if (correctBlock1) {
                BlockUtil.copyWorldCoordinate(worldCoordinate3, block1.getWorldCoordinate());
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
//            return Math.abs(coordinate1.getX().subtract(coordinate2.getX()).doubleValue())
//                    < shape1.getRadius().getX().add(shape2.getRadius().getX()).doubleValue()
//                    && Math.abs(coordinate1.getY().subtract(coordinate2.getY()).doubleValue())
//                    < shape1.getRadius().getY().add(shape2.getRadius().getY()).doubleValue();
            return BlockUtil.calculateHorizontalDistance(coordinate1, coordinate2).abs().doubleValue()
                    < shape1.getRadius().getX().add(shape2.getRadius().getX()).doubleValue()
                    && BlockUtil.calculateVerticalDistance(coordinate1, coordinate2).abs().doubleValue()
                    < shape1.getRadius().getY().add(shape2.getRadius().getY()).doubleValue();
        }
        // Round vs. rectangle
        if (BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND == shape2.getShapeType()) {
            return detectCollision(regionInfo, block2, block1);
        }
//        return ((coordinate1.getX().doubleValue() > coordinate2.getX().subtract(shape2.getRadius().getX()).doubleValue()
//                && coordinate1.getX().doubleValue() < coordinate2.getX().add(shape2.getRadius().getX()).doubleValue()
//                && coordinate1.getY().add(shape1.getRadius().getY()).doubleValue() > coordinate2.getY().subtract(shape2.getRadius().getY()).doubleValue()
//                && coordinate1.getY().subtract(shape1.getRadius().getY()).doubleValue() < coordinate2.getY().add(shape2.getRadius().getY()).doubleValue())
//                || (coordinate1.getX().add(shape1.getRadius().getX()).doubleValue() > coordinate2.getX().subtract(shape2.getRadius().getX()).doubleValue()
//                && coordinate1.getX().subtract(shape1.getRadius().getX()).doubleValue() < coordinate2.getX().add(shape2.getRadius().getX()).doubleValue()
//                && coordinate1.getY().doubleValue() > coordinate2.getY().subtract(shape2.getRadius().getY()).doubleValue()
//                && coordinate1.getY().doubleValue() < coordinate2.getY().add(shape2.getRadius().getY()).doubleValue()));
////                && (); // 4 apexes
        boolean isInsideRectangle1 = BlockUtil.calculateHorizontalDistance(coordinate1, coordinate2).abs().doubleValue()
                < shape1.getRadius().getX().multiply(BigDecimal.valueOf(2)).add(shape2.getRadius().getX()).doubleValue()
                && BlockUtil.calculateVerticalDistance(coordinate1, coordinate2).abs().doubleValue()
                < shape2.getRadius().getY().doubleValue();
        boolean isInsideRectangle2 = BlockUtil.calculateHorizontalDistance(coordinate1, coordinate2).abs().doubleValue()
                < shape2.getRadius().getX().doubleValue()
                && BlockUtil.calculateVerticalDistance(coordinate1, coordinate2).abs().doubleValue()
                < shape1.getRadius().getY().multiply(BigDecimal.valueOf(2)).add(shape2.getRadius().getY()).doubleValue();
        boolean isInsideRound1 = BlockUtil.calculateDistance(coordinate1,
                new Coordinate(coordinate2.getX().subtract(shape2.getRadius().getX()),
                        coordinate2.getY().subtract(shape2.getRadius().getY()))).doubleValue()
                < shape1.getRadius().getX().doubleValue();
        boolean isInsideRound2 = BlockUtil.calculateDistance(coordinate1,
                new Coordinate(coordinate2.getX().add(shape2.getRadius().getX()),
                        coordinate2.getY().subtract(shape2.getRadius().getY()))).doubleValue()
                < shape1.getRadius().getX().doubleValue();
        boolean isInsideRound3 = BlockUtil.calculateDistance(coordinate1,
                new Coordinate(coordinate2.getX().subtract(shape2.getRadius().getX()),
                        coordinate2.getY().add(shape2.getRadius().getY()))).doubleValue()
                < shape1.getRadius().getX().doubleValue();
        boolean isInsideRound4 = BlockUtil.calculateDistance(coordinate1,
                new Coordinate(coordinate2.getX().add(shape2.getRadius().getX()),
                        coordinate2.getY().add(shape2.getRadius().getY()))).doubleValue()
                < shape1.getRadius().getX().doubleValue();
        return isInsideRectangle1 || isInsideRectangle2 || isInsideRound1 || isInsideRound2 || isInsideRound3 || isInsideRound4;
    }

    public static boolean checkBlockTypeRegistrable(int blockType) {
        return checkBlockTypeInteractive(blockType)
                || blockType == BlockConstants.BLOCK_TYPE_DROP
                || blockType == BlockConstants.BLOCK_TYPE_TELEPORT;
    }

    public static boolean checkBlockTypeInteractive(int blockType) {
        switch (blockType) {
            case BlockConstants.BLOCK_TYPE_NORMAL:
            case BlockConstants.BLOCK_TYPE_EFFECT:
            case BlockConstants.BLOCK_TYPE_DROP:
            case BlockConstants.BLOCK_TYPE_TELEPORT:
            case BlockConstants.BLOCK_TYPE_BUILDING:
            case BlockConstants.BLOCK_TYPE_TREE:
            case BlockConstants.BLOCK_TYPE_ROCK:
            case BlockConstants.BLOCK_TYPE_TRAP:
            case BlockConstants.BLOCK_TYPE_FLOOR:
            case BlockConstants.BLOCK_TYPE_FLOOR_DECORATION:
            case BlockConstants.BLOCK_TYPE_WALL:
            case BlockConstants.BLOCK_TYPE_WALL_DECORATION:
            case BlockConstants.BLOCK_TYPE_CEILING:
            case BlockConstants.BLOCK_TYPE_CEILING_DECORATION:
            case BlockConstants.BLOCK_TYPE_PLASMA:
                return false;
            default:
                return true;
        }
    }

    public static Queue<Block> createRankingQueue(final RegionInfo regionInfo) {
        return new PriorityQueue<>((o1, o2) -> {
            if (!Objects.equals(o1.getBlockInfo().getStructure().getLayer() / 10,
                    o2.getBlockInfo().getStructure().getLayer() / 10)) {
                return o1.getBlockInfo().getStructure().getLayer() / 10
                        - o2.getBlockInfo().getStructure().getLayer() / 10;
            }
            BigDecimal verticalDistance = calculateVerticalDistance(regionInfo, o1.getWorldCoordinate(),
                    o2.getWorldCoordinate());
            if (null != verticalDistance && !verticalDistance.equals(BigDecimal.ZERO)) {
                return BigDecimal.ZERO.compareTo(verticalDistance);
            }
            BigDecimal horizontalDistance = calculateHorizontalDistance(regionInfo, o1.getWorldCoordinate(),
                    o2.getWorldCoordinate());
            if (null != horizontalDistance && !horizontalDistance.equals(BigDecimal.ZERO)) {
                return BigDecimal.ZERO.compareTo(horizontalDistance);
            }
            int layerDiff = o1.getBlockInfo().getStructure().getLayer() % 10
                    - o2.getBlockInfo().getStructure().getLayer() % 10;
            if (layerDiff != 0) {
                return layerDiff;
            }
            return o1.getBlockInfo().getCode() - o2.getBlockInfo().getCode();
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

    private static int convertEventCode2Layer(int eventCode) {
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
            case BlockConstants.BLOCK_CODE_BLEED_SEVERE:
            case BlockConstants.BLOCK_CODE_WAVE:
                layer = BlockConstants.STRUCTURE_LAYER_BOTTOM_DECORATION;
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
                .multiply(BigDecimal.valueOf(2)));
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

    /**
     * Positive object's material VS. Negative object's material
     * @param structureMaterial1 Positive object's material
     * @param structureMaterial2 Negative object's material
     * @return
     */
    public static boolean checkMaterialCollision(int structureMaterial1, int structureMaterial2) {
        switch (structureMaterial1) {
            case BlockConstants.STRUCTURE_MATERIAL_ALL:
                return true;
            case BlockConstants.STRUCTURE_MATERIAL_SOLID:
            case BlockConstants.STRUCTURE_MATERIAL_PARTICLE:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_ALL
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET;
            case BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_ALL
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET_FLESH;
            case BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH:
            case BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_ALL
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET;
            case BlockConstants.STRUCTURE_MATERIAL_NONE:
            case BlockConstants.STRUCTURE_MATERIAL_TARGET:
            case BlockConstants.STRUCTURE_MATERIAL_TARGET_FLESH:
            default:
                return false;
        }
    }

    public static String convertBlockInfo2ItemNo(BlockInfo blockInfo) {
        return ItemConstants.ITEM_PACK_MAP.get(Integer.valueOf(blockInfo.getCode()));
    }

    public static BlockInfo convertItemNo2BlockInfo(String itemNo) {
        int blockCode = ItemConstants.ITEM_BUILD_MAP.get(itemNo);
        return createBlockInfoByCode(blockCode);
    }

    public static int convertBlockCode2Type(int blockCode) {
        return BlockConstants.BLOCK_CODE_TYPE_MAP.getOrDefault(blockCode, BlockConstants.BLOCK_TYPE_FLOOR);
    }

    public static BlockInfo createBlockInfoByCode(int blockCode) {
        int blockType = BlockUtil.convertBlockCode2Type(blockCode);
        String id = BlockUtil.checkBlockTypeRegistrable(blockType) ? UUID.randomUUID().toString() : "";
        Structure structure;
        int structureMaterial;
        Shape roundShape = new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BlockConstants.ROUND_SCENE_OBJECT_RADIUS, BlockConstants.ROUND_SCENE_OBJECT_RADIUS));
        switch (blockType) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_MELEE_HIT:
                    case BlockConstants.BLOCK_CODE_MELEE_KICK:
                    case BlockConstants.BLOCK_CODE_MELEE_SCRATCH:
                    case BlockConstants.BLOCK_CODE_MELEE_SMASH:
                    case BlockConstants.BLOCK_CODE_SHOOT_HIT:
                    case BlockConstants.BLOCK_CODE_SHOOT_ARROW:
                    case BlockConstants.BLOCK_CODE_SHOOT_SLUG:
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
                Coordinate imageSize;
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_BLOCK:
                    case BlockConstants.BLOCK_CODE_UPGRADE:
                    case BlockConstants.BLOCK_CODE_SPRAY:
                        imageSize = new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2));
                        break;
                    default:
                        imageSize = new Coordinate(BigDecimal.ONE, BigDecimal.ONE);
                        break;
                }
                structure = new Structure(structureMaterial, BlockUtil.convertEventCode2Layer(blockCode),
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                new Coordinate(BlockConstants.EVENT_RADIUS, BlockConstants.EVENT_RADIUS)), imageSize);
                break;
            case BlockConstants.BLOCK_TYPE_PLAYER:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                new Coordinate(BlockConstants.PLAYER_RADIUS, BlockConstants.PLAYER_RADIUS)),
                        new Coordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, new Shape(),
                        new Coordinate(BigDecimal.valueOf(0.5D), BigDecimal.valueOf(0.5D)));
                break;
            case BlockConstants.BLOCK_TYPE_TRAP:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE);
                break;
            case BlockConstants.BLOCK_TYPE_TELEPORT:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_TARGET_FLESH,
                        BlockConstants.STRUCTURE_LAYER_BOTTOM);
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
                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.5D))));
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
                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.5D))));
                break;
            case BlockConstants.BLOCK_TYPE_DRESSER:
            case BlockConstants.BLOCK_TYPE_STORAGE:
            case BlockConstants.BLOCK_TYPE_COOKER:
            case BlockConstants.BLOCK_TYPE_SINK:
            case BlockConstants.BLOCK_TYPE_CONTAINER:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_HUMAN_REMAIN_DEFAULT:
                    case BlockConstants.BLOCK_CODE_ANIMAL_REMAIN_DEFAULT:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE, roundShape);
                        break;
                    default:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE);
                        break;
                }
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
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.5D))));
                        break;
                    case BlockConstants.BLOCK_CODE_BENCH:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.2D))));
                        break;
                    case BlockConstants.BLOCK_CODE_DESK_1:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BigDecimal.valueOf(0.3D), BigDecimal.valueOf(0.2D))));
                        break;
                    case BlockConstants.BLOCK_CODE_DESK_2:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BigDecimal.valueOf(0.2D), BigDecimal.valueOf(0.3D))));
                        break;
                    case BlockConstants.BLOCK_CODE_ASH_PILE:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BigDecimal.valueOf(0.4D), BigDecimal.valueOf(0.4D))));
                        break;
                    case BlockConstants.BLOCK_CODE_WORKSHOP_EMPTY:
                        structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE,
                                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE,
                                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                        new Coordinate(BigDecimal.valueOf(0.4D), BigDecimal.valueOf(0.2D))));
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
                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                new Coordinate(BigDecimal.valueOf(0.2D), BigDecimal.valueOf(0.4D))));
                break;
            case BlockConstants.BLOCK_TYPE_TREE:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_ALL,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        roundShape,
                        new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(3)));
                break;
            case BlockConstants.BLOCK_TYPE_SPEAKER:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                new Coordinate(BlockConstants.ROUND_SCENE_OBJECT_RADIUS,
                                        BlockConstants.ROUND_SCENE_OBJECT_RADIUS)));
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
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_GROUND, new Shape(),
                        new Coordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR_DECORATION:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_GROUND_DECORATION);
                break;
            case BlockConstants.BLOCK_TYPE_WALL:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, new Shape(),
                        new Coordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
            case BlockConstants.BLOCK_TYPE_WALL_DECORATION:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE_DECORATION);
                break;
            case BlockConstants.BLOCK_TYPE_CEILING:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_TOP);
                break;
            case BlockConstants.BLOCK_TYPE_CEILING_DECORATION:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_TOP_DECORATION);
                break;
            case BlockConstants.BLOCK_TYPE_PLASMA:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE);
                break;
            case BlockConstants.BLOCK_TYPE_NORMAL:
            default:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE, new Shape(),
                        new Coordinate(BigDecimal.ONE, BigDecimal.ONE));
                break;
        }
        BlockInfo blockInfo = new BlockInfo(blockType, id, blockCode, structure);
        initializeBlockInfoHp(blockInfo);
        return blockInfo;
    }

    public static void initializeBlockInfoHp(BlockInfo blockInfo) {
        int hpMax = BlockConstants.HP_DEFAULT;
        switch (blockInfo.getType()) {
            case BlockConstants.BLOCK_TYPE_PLAYER:
                hpMax = 1000;
                break;
            default:
                break;
        }
        blockInfo.getHpMax().set(hpMax);
        blockInfo.getHp().set(blockInfo.getHpMax().get());
    }

    public static MovementInfo createMovementInfoByCode(final int blockCode) {
        int blockType = BlockUtil.convertBlockCode2Type(blockCode);
        int period;
        switch (blockType) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_SPARK_SHORT:
                        period = 5;
                        break;
                    case BlockConstants.BLOCK_CODE_LIGHT_SMOKE:
                        period = 6;
                        break;
                    case BlockConstants.BLOCK_CODE_SHOCK:
                        period = 10;
                        break;
                    case BlockConstants.BLOCK_CODE_DECAY:
                    case BlockConstants.BLOCK_CODE_CHEER:
                    case BlockConstants.BLOCK_CODE_CURSE:
                        period = 50;
                        break;
                    case BlockConstants.BLOCK_CODE_BLEED_SEVERE:
                        period = 250;
                        break;
                    default:
                        period = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                period = GamePalConstants.DROP_DISAPPEAR_THRESHOLD_IN_FRAME;
                break;
            case BlockConstants.BLOCK_TYPE_PLASMA:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_FIRE:
                        period = BlockConstants.PERIOD_DYNAMIC_DEFAULT;
                        break;
                    default:
                        period = BlockConstants.PERIOD_STATIC_DEFAULT;
                        break;
                }
                break;
            default:
                period = BlockConstants.PERIOD_STATIC_DEFAULT;
                break;
        }
        int frameMax;
        switch (blockType) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
            case BlockConstants.BLOCK_TYPE_DROP:
                frameMax = period;
                break;
            case BlockConstants.BLOCK_TYPE_PLASMA:
                switch (blockCode) {
                    case BlockConstants.BLOCK_CODE_FIRE:
                        frameMax = period * 5;
                        break;
                    default:
                        frameMax = BlockConstants.FRAME_MAX_INFINITE_DEFAULT;
                        break;
                }
                break;
            default:
                frameMax = BlockConstants.FRAME_MAX_INFINITE_DEFAULT;
                break;
        }
        return new MovementInfo(new Coordinate(),
                BlockConstants.MAX_SPEED_DEFAULT,
                BlockConstants.MAX_SPEED_DEFAULT.multiply(BlockConstants.ACCELERATION_MAX_SPEED_RATIO),
                BlockConstants.FACE_DIRECTION_DEFAULT,
                BlockConstants.FLOOR_CODE_DEFAULT,
                BlockConstants.FRAME_DEFAULT,
                frameMax,
                period);
    }
}
