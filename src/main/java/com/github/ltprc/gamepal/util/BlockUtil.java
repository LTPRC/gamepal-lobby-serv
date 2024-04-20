package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PerceptionInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.structure.*;
import com.github.ltprc.gamepal.model.map.world.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class BlockUtil {

    private BlockUtil() {}

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

    public static void adjustCoordinate(Coordinate coordinate, IntegerCoordinate integerCoordinate, int height, int width) {
        // Pos-y is south, neg-y is north
        coordinate.setX(coordinate.getX().add(BigDecimal.valueOf((long) integerCoordinate.getX() * width)));
        coordinate.setY(coordinate.getY().add(BigDecimal.valueOf((long) integerCoordinate.getY() * height)));
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

    /**
     * Ignore sceneCoordinate
     * @param regionInfo RegionInfo
     * @param worldBlock WorldBlock
     * @param convertSceneCoordinate whether sceneCoordinate will be converted into new block coordinate
     * @return Block
     */
    public static Block convertWorldBlock2Block(RegionInfo regionInfo, WorldBlock worldBlock,
                                                boolean convertSceneCoordinate) {
        Block newBlock = new Block(worldBlock.getType(), worldBlock.getId(), worldBlock.getCode(),
                worldBlock.getStructure(), convertSceneCoordinate
                ? convertWorldCoordinate2Coordinate(regionInfo, worldBlock) : worldBlock.getCoordinate());
        switch (worldBlock.getType()) {
            case GamePalConstants.BLOCK_TYPE_DROP:
                newBlock = new Drop(((WorldDrop) worldBlock).getItemNo(), ((WorldDrop) worldBlock).getAmount(), newBlock);
                break;
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                newBlock = new Teleport(((WorldTeleport) worldBlock).getTo(), newBlock);
                break;
            default:
                break;
        }
        return newBlock;
    }
    public static WorldBlock convertBlock2WorldBlock(Block block, int regionNo, IntegerCoordinate sceneCoordinate,
                                                     Coordinate coordinate) {
        WorldBlock worldBlock = new WorldBlock(block.getType(), block.getId(), block.getCode(),
                block.getStructure(), new WorldCoordinate(regionNo, sceneCoordinate, coordinate));
        switch (block.getType()) {
            case GamePalConstants.BLOCK_TYPE_DROP:
                worldBlock = new WorldDrop(((Drop) block).getItemNo(), ((Drop) block).getAmount(), worldBlock);
                break;
            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                worldBlock = new WorldTeleport(((Teleport) block).getTo(), worldBlock);
                break;
            default:
                break;
        }
        return worldBlock;
    }

    public static Event convertWorldEvent2Event(WorldEvent worldEvent) {
        return new Event(worldEvent.getUserCode(), worldEvent.getCode(), worldEvent.getFrame(),
                worldEvent.getFrameMax(), worldEvent.getPeriod(), worldEvent.getCoordinate());
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
            if (c1.getX().compareTo(c2.getX()) == 1) {
                return BigDecimal.valueOf(180D);
            }
            return BigDecimal.ZERO;
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

    public static WorldCoordinate convertCoordinate2WorldCoordinate(RegionInfo regionInfo,
                                                                    IntegerCoordinate sceneCoordinate,
                                                                    Coordinate coordinate) {
        WorldCoordinate wc = new WorldCoordinate();
        wc.setRegionNo(regionInfo.getRegionNo());
        wc.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        wc.setCoordinate(new Coordinate(coordinate));
        fixWorldCoordinate(regionInfo, wc);
        return wc;
    }

    public static boolean detectLineSquareCollision(RegionInfo regionInfo, WorldBlock worldBlock1,
                                                    BigDecimal ballisticAngle, WorldBlock worldBlock2) {
        if (worldBlock1.getRegionNo() != regionInfo.getRegionNo()
                || worldBlock2.getRegionNo() != regionInfo.getRegionNo()) {
            return false;
        }
        Block block1 = convertWorldBlock2Block(regionInfo, worldBlock1, true);
        Block block2 = convertWorldBlock2Block(regionInfo, worldBlock2, true);
        return detectLineSquareCollision(block1, ballisticAngle, block2);
    }

    public static boolean detectLineSquareCollision(Block oldBlock1, BigDecimal ballisticAngle, Block oldBlock2) {
        Block block1 = new Block(oldBlock1.getType(), oldBlock1.getId(), oldBlock1.getCode(),
                oldBlock1.getStructure(),
                new Coordinate(oldBlock1.getX().add(oldBlock1.getStructure().getShape().getCenter().getX()),
                        oldBlock1.getY().add(oldBlock1.getStructure().getShape().getCenter().getY())));
        Block block2 = new Block(oldBlock2.getType(), oldBlock2.getId(), oldBlock2.getCode(),
                oldBlock2.getStructure(),
                new Coordinate(oldBlock2.getX().add(oldBlock2.getStructure().getShape().getCenter().getX()),
                        oldBlock2.getY().add(oldBlock2.getStructure().getShape().getCenter().getY())));
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_SQUARE == block1.getStructure().getShape().getShapeType()) {
            block1.getStructure().getShape().setShapeType(GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE);
            block1.getStructure().getShape().getRadius().setY(block1.getStructure().getShape().getRadius().getX());
        }
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_SQUARE == block2.getStructure().getShape().getShapeType()) {
            block2.getStructure().getShape().setShapeType(GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE);
            block2.getStructure().getShape().getRadius().setY(block2.getStructure().getShape().getRadius().getX());
        }
        if (ballisticAngle.compareTo(BigDecimal.valueOf(90D)) == 0
                || ballisticAngle.compareTo(BigDecimal.valueOf(270D)) == 0) {
            return block1.getX().subtract(block2.getX()).abs().doubleValue()
                    < block1.getStructure().getShape().getRadius().getX()
                    .add(block2.getStructure().getShape().getRadius().getX()).doubleValue();
        }
        // Round vs. round
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND == block1.getStructure().getShape().getShapeType()
                && GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND == block2.getStructure().getShape().getShapeType()) {
            return calculateBallisticDistance(block1, ballisticAngle, block2)
                    .compareTo(block1.getStructure().getShape().getRadius().getX()
                            .add(block2.getStructure().getShape().getRadius().getX())) < 0;
        }
        double slope = -Math.tan(ballisticAngle.doubleValue() / 180 * Math.PI);
        BigDecimal xLeft = block2.getX().subtract(block2.getStructure().getShape().getRadius().getX());
        BigDecimal xRight = block2.getX().add(block2.getStructure().getShape().getRadius().getX());
        BigDecimal yLeft = xLeft.subtract(block1.getX()).multiply(BigDecimal.valueOf(slope)).add(block1.getY());
        BigDecimal yRight = xRight.subtract(block1.getX()).multiply(BigDecimal.valueOf(slope)).add(block1.getY());
        // Rectangle vs. rectangle
//        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE == block1.getStructure().getShape().getShapeType()
//                && GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE == block2.getStructure().getShape().getShapeType()) {
//        if ((yLeft.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
//                .add(block2.getStructure().getShape().getRadius().getY()).negate()) > 0
//                && yRight.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
//                .add(block2.getStructure().getShape().getRadius().getY())) < 0)
//                || (yLeft.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
//                .add(block2.getStructure().getShape().getRadius().getY())) < 0
//                && yRight.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
//                .add(block2.getStructure().getShape().getRadius().getY()).negate()) > 0)) {
//                return true;
//            }
//            return false;
//        }
        // Round vs. rectangle
//        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND == block2.getStructure().getShape().getShapeType()) {
//            return detectLineSquareCollision(oldBlock2, ballisticAngle, oldBlock1);
//        }
        // TODO make round vs. rectangle specific
        return (yLeft.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
                .add(block2.getStructure().getShape().getRadius().getY()).negate()) > 0
                && yRight.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
                .add(block2.getStructure().getShape().getRadius().getY())) < 0)
                || (yLeft.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
                .add(block2.getStructure().getShape().getRadius().getY())) < 0
                && yRight.subtract(block2.getY()).compareTo(block1.getStructure().getShape().getRadius().getY()
                .add(block2.getStructure().getShape().getRadius().getY()).negate()) > 0);
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

    public static boolean detectCollision(RegionInfo regionInfo, WorldBlock worldBlock1, WorldBlock worldBlock2) {
        Block block1 = convertWorldBlock2Block(regionInfo, worldBlock1, true);
        Block block2 = convertWorldBlock2Block(regionInfo, worldBlock2, true);
        return detectCollision(block1, block2);
    }

    public static boolean detectCollision(Block oldBlock1, Block oldBlock2) {
        Block block1 = new Block(oldBlock1.getType(), oldBlock1.getId(), oldBlock1.getCode(),
                oldBlock1.getStructure(),
                new Coordinate(oldBlock1.getX().add(oldBlock1.getStructure().getShape().getCenter().getX()),
                        oldBlock1.getY().add(oldBlock1.getStructure().getShape().getCenter().getY())));
        Block block2 = new Block(oldBlock2.getType(), oldBlock2.getId(), oldBlock2.getCode(),
                oldBlock2.getStructure(),
                new Coordinate(oldBlock2.getX().add(oldBlock2.getStructure().getShape().getCenter().getX()),
                        oldBlock2.getY().add(oldBlock2.getStructure().getShape().getCenter().getY())));
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_SQUARE == block1.getStructure().getShape().getShapeType()) {
            block1.getStructure().getShape().setShapeType(GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE);
            block1.getStructure().getShape().getRadius().setY(block1.getStructure().getShape().getRadius().getX());
        }
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_SQUARE == block2.getStructure().getShape().getShapeType()) {
            block2.getStructure().getShape().setShapeType(GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE);
            block2.getStructure().getShape().getRadius().setY(block2.getStructure().getShape().getRadius().getX());
        }
        // Round vs. round
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND == block1.getStructure().getShape().getShapeType()
                && GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND == block2.getStructure().getShape().getShapeType()) {
            return Math.sqrt(Math.pow(block1.getX().subtract(block2.getX()).doubleValue(), 2)
                    + Math.pow(block1.getY().subtract(block2.getY()).doubleValue(), 2))
                    <= block1.getStructure().getShape().getRadius().getX()
                    .add(block2.getStructure().getShape().getRadius().getX()).doubleValue();
        }
        // Rectangle vs. rectangle
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE == block1.getStructure().getShape().getShapeType()
                && GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE == block2.getStructure().getShape().getShapeType()) {
            return Math.abs(block1.getX().subtract(block2.getX()).doubleValue())
                    <= block1.getStructure().getShape().getRadius().getX()
                    .add(block2.getStructure().getShape().getRadius().getX()).doubleValue()
                    && Math.abs(block1.getY().subtract(block2.getY()).doubleValue())
                    <= block1.getStructure().getShape().getRadius().getY()
                    .add(block2.getStructure().getShape().getRadius().getY()).doubleValue();
        }
        // Round vs. rectangle
        if (GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND == block2.getStructure().getShape().getShapeType()) {
            return detectCollision(oldBlock2, oldBlock1);
        }
        return block1.getX().add(block1.getStructure().getShape().getRadius().getX()).doubleValue()
                >= block2.getX().subtract(block2.getStructure().getShape().getRadius().getX()).doubleValue()
                && block1.getX().subtract(block1.getStructure().getShape().getRadius().getX()).doubleValue()
                <= block2.getX().add(block2.getStructure().getShape().getRadius().getX()).doubleValue()
                && block1.getY().add(block1.getStructure().getShape().getRadius().getY()).doubleValue()
                >= block2.getY().subtract(block2.getStructure().getShape().getRadius().getY()).doubleValue()
                && block1.getY().subtract(block1.getStructure().getShape().getRadius().getY()).doubleValue()
                <= block2.getY().add(block2.getStructure().getShape().getRadius().getY()).doubleValue();
    }

    public static boolean checkBlockTypeInteractive(int blockType) {
        switch (blockType) {
            case GamePalConstants.BLOCK_TYPE_PLAYER:
            case GamePalConstants.BLOCK_TYPE_BED:
            case GamePalConstants.BLOCK_TYPE_TOILET:
            case GamePalConstants.BLOCK_TYPE_DRESSER:
            case GamePalConstants.BLOCK_TYPE_WORKSHOP:
            case GamePalConstants.BLOCK_TYPE_GAME:
            case GamePalConstants.BLOCK_TYPE_STORAGE:
            case GamePalConstants.BLOCK_TYPE_COOKER:
            case GamePalConstants.BLOCK_TYPE_SINK:
                return true;
            default:
                return false;
        }
    }

    public static Queue<Block> createRankingQueue() {
        return new PriorityQueue<>((o1, o2) -> {
            if (!Objects.equals(o1.getStructure().getLayer() / 10, o2.getStructure().getLayer() / 10)) {
                return o1.getStructure().getLayer() / 10 - o2.getStructure().getLayer() / 10;
            }
            if (!Objects.equals(o1.getY(), o2.getY())) {
                return o1.getY().compareTo(o2.getY());
            }
            return o1.getStructure().getLayer() % 10 - o2.getStructure().getLayer() % 10;
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

    public static WorldBlock convertEvent2WorldBlock(RegionInfo regionInfo, String userCode, int eventCode,
                                                     WorldCoordinate worldCoordinate) {
        WorldBlock block = new WorldBlock(GamePalConstants.BLOCK_TYPE_NORMAL, userCode, String.valueOf(eventCode),
                new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                convertEventCode2Layer(eventCode)), worldCoordinate);
        BlockUtil.fixWorldCoordinate(regionInfo, block);
        return block;
    }

    public static Block convertEvent2Block(Event event) {
        Block block = new Block(GamePalConstants.BLOCK_TYPE_EVENT, null,
                event.getCode() + "-" + event.getFrame(),
                new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW, convertEventCode2Layer(event.getCode())),
                event);
        return block;
    }

    private static int convertEventCode2Layer(int eventCode) {
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
                layer = GamePalConstants.STRUCTURE_LAYER_TOP_DECORATION;
                break;
            default:
                layer = GamePalConstants.STRUCTURE_LAYER_MIDDLE_DECORATION;
                break;
        }
        return layer;
    }

    public static WorldEvent createWorldEvent(String userCode, int code, WorldCoordinate worldCoordinate) {
        WorldEvent event = new WorldEvent();
        BlockUtil.copyWorldCoordinate(worldCoordinate, event);
        event.setUserCode(userCode);
        event.setCode(code);
        event.setFrame(0);
        switch (code) {
            case GamePalConstants.EVENT_CODE_FIRE:
                // Infinite 25-frame event
                event.setPeriod(25);
                event.setFrameMax(-1);
                break;
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_DISTURB:
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                // Finite 50-frame event
                event.setPeriod(50);
                event.setFrameMax(50);
                break;
            default:
                // Finite 25-frame event
                event.setPeriod(25);
                event.setFrameMax(25);
                break;
        }
        return event;
    }

    public static void updateVisionRadius(PerceptionInfo perceptionInfo, int worldTime) {
        BigDecimal visionRadius = GamePalConstants.DEFAULT_NIGHT_VISION_RADIUS;
        if (worldTime >= GamePalConstants.WORLD_TIME_SUNRISE_BEGIN
                && worldTime < GamePalConstants.WORLD_TIME_SUNRISE_END) {
            visionRadius.add(GamePalConstants.DEFAULT_DAYTIME_VISION_RADIUS
                    .subtract(GamePalConstants.DEFAULT_NIGHT_VISION_RADIUS)
                    .multiply(BigDecimal.valueOf(worldTime - GamePalConstants.WORLD_TIME_SUNRISE_BEGIN)
                    .divide(BigDecimal.valueOf(GamePalConstants.WORLD_TIME_SUNRISE_END
                            - GamePalConstants.WORLD_TIME_SUNRISE_BEGIN), RoundingMode.CEILING)));
        } else if (worldTime >= GamePalConstants.WORLD_TIME_SUNSET_BEGIN
                && worldTime < GamePalConstants.WORLD_TIME_SUNSET_END) {
            visionRadius.add(GamePalConstants.DEFAULT_DAYTIME_VISION_RADIUS
                    .subtract(GamePalConstants.DEFAULT_NIGHT_VISION_RADIUS)
                    .multiply(BigDecimal.valueOf(GamePalConstants.WORLD_TIME_SUNSET_END - worldTime)
                            .divide(BigDecimal.valueOf(GamePalConstants.WORLD_TIME_SUNSET_END
                                    - GamePalConstants.WORLD_TIME_SUNSET_BEGIN), RoundingMode.CEILING)));
        } else if (worldTime >= GamePalConstants.WORLD_TIME_SUNRISE_END
                && worldTime < GamePalConstants.WORLD_TIME_SUNSET_BEGIN) {
            visionRadius = GamePalConstants.DEFAULT_DAYTIME_VISION_RADIUS;
        }
        perceptionInfo.setDistinctVisionRadius(visionRadius);
        perceptionInfo.setIndistinctVisionRadius(perceptionInfo.getDistinctVisionRadius().multiply(BigDecimal.valueOf(1.5)));
        perceptionInfo.setHearingRadius(GamePalConstants.DEFAULT_HEARING_RADIUS);
    }
}
