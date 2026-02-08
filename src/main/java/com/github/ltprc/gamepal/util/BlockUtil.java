package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.ItemConstants;
import com.github.ltprc.gamepal.factory.BlockFactory;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.coordinate.PlanarCoordinate;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockUtil {

    private BlockUtil() {}

    public static IntegerCoordinate getCoordinateRelation(IntegerCoordinate from, IntegerCoordinate to) {
        return new IntegerCoordinate(to.getX() - from.getX(), to.getY() - from.getY());
    }

    public static Coordinate adjustCoordinate(Coordinate coordinate, IntegerCoordinate integerCoordinate, int height,
                                              int width) {
        // Pos-y is south, neg-y is north
        return new Coordinate(
                coordinate.getX().add(BigDecimal.valueOf(
                        integerCoordinate.getX()).multiply(BigDecimal.valueOf(height))),
                coordinate.getY().add(BigDecimal.valueOf(
                        integerCoordinate.getY()).multiply(BigDecimal.valueOf(width))),
                coordinate.getZ());
    }

    /**
     * Keep the coordinate inside the range of width multiply height based on its sceneCoordinate.
     * X-Y block coordinate is valued from [-0.5, 9.5)
     * @param regionInfo
     * @param worldCoordinate
     */
    public static void fixWorldCoordinate(RegionInfo regionInfo, WorldCoordinate worldCoordinate) {
        fixWorldCoordinateWithThreshold(regionInfo, worldCoordinate, BigDecimal.valueOf(-0.5D),
                BigDecimal.valueOf(regionInfo.getWidth() - 0.5D), BigDecimal.valueOf(-0.5D),
                BigDecimal.valueOf(regionInfo.getHeight() - 0.5D));
    }

    /**
     * Keep the coordinate inside the range of width multiply height based on its sceneCoordinate.
     * X-Y block coordinate is valued from [0, 10)
     * @param regionInfo
     * @param worldCoordinate
     */
    @Deprecated
    public static void fixWorldCoordinateReal(RegionInfo regionInfo, WorldCoordinate worldCoordinate) {
        fixWorldCoordinateWithThreshold(regionInfo, worldCoordinate, BigDecimal.ZERO,
                BigDecimal.valueOf(regionInfo.getWidth()), BigDecimal.ZERO, BigDecimal.valueOf(regionInfo.getHeight()));
    }

    /**
     * Keep the coordinate inside the range of width multiply height based on its sceneCoordinate.
     * X-Y block coordinate is valued from [xMin, xMax) and [xMin, xMax)
     * @param regionInfo
     * @param worldCoordinate
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     */
    private static void fixWorldCoordinateWithThreshold(RegionInfo regionInfo, WorldCoordinate worldCoordinate,
                                                        BigDecimal xMin, BigDecimal xMax,
                                                        BigDecimal yMin, BigDecimal yMax) {
        while (worldCoordinate.getCoordinate().getY().compareTo(yMin) < 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() - 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().add(BigDecimal.valueOf(regionInfo.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getY().compareTo(yMax) >= 0) {
            worldCoordinate.getSceneCoordinate().setY(worldCoordinate.getSceneCoordinate().getY() + 1);
            worldCoordinate.getCoordinate()
                    .setY(worldCoordinate.getCoordinate().getY().subtract(BigDecimal.valueOf(regionInfo.getHeight())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(xMin) < 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() - 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().add(BigDecimal.valueOf(regionInfo.getWidth())));
        }
        while (worldCoordinate.getCoordinate().getX().compareTo(xMax) >= 0) {
            worldCoordinate.getSceneCoordinate().setX(worldCoordinate.getSceneCoordinate().getX() + 1);
            worldCoordinate.getCoordinate()
                    .setX(worldCoordinate.getCoordinate().getX().subtract(BigDecimal.valueOf(regionInfo.getWidth())));
        }
    }

    public static Coordinate convertWorldCoordinate2Coordinate(RegionInfo regionInfo, WorldCoordinate worldCoordinate) {
        return adjustCoordinate(worldCoordinate.getCoordinate(), worldCoordinate.getSceneCoordinate(),
                regionInfo.getHeight(), regionInfo.getWidth());
    }

    /**
     * Z-axis added
     * @param from
     * @param to
     */
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

    public static BigDecimal calculateXDistance(RegionInfo regionInfo, WorldCoordinate wc1,
                                                         WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return calculateXDistance(c1, c2);
    }

    public static BigDecimal calculateXDistance(Coordinate c1, Coordinate c2) {
        return c2.getX().subtract(c1.getX());
    }

    public static BigDecimal calculateYDistance(RegionInfo regionInfo, WorldCoordinate wc1,
                                                       WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return calculateYDistance(c1, c2);
    }

    public static BigDecimal calculateYDistance(Coordinate c1, Coordinate c2) {
        return c2.getY().subtract(c1.getY());
    }

    public static BigDecimal calculatePlanarDistance(final RegionInfo regionInfo, final WorldCoordinate wc1,
                                                     final WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return calculatePlanarDistance(c1, c2);
    }

    public static BigDecimal calculatePlanarDistance(final Coordinate c1, final Coordinate c2) {
        return BigDecimal.valueOf(Math.sqrt(Math.pow(c1.getX().subtract(c2.getX()).doubleValue(), 2)
                + Math.pow(c1.getY().subtract(c2.getY()).doubleValue(), 2)));
    }

    /**
     * Z-axis added
     * @param regionInfo
     * @param wc1
     * @param wc2
     * @return
     */
    public static BigDecimal calculateZDistance(RegionInfo regionInfo, WorldCoordinate wc1,
                                                         WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        return calculateZDistance(wc1.getCoordinate(), wc2.getCoordinate());
    }

    /**
     * Z-axis added
     * @param c1
     * @param c2
     * @return
     */
    public static BigDecimal calculateZDistance(Coordinate c1, Coordinate c2) {
        return c2.getZ().subtract(c1.getZ());
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
                + Math.pow(c1.getY().subtract(c2.getY()).doubleValue(), 2)
                + Math.pow(c1.getZ().subtract(c2.getZ()).doubleValue(), 2)));
    }

    public static BigDecimal calculateAngle(final RegionInfo regionInfo, final WorldCoordinate wc1,
                                            final WorldCoordinate wc2) {
        if (wc1.getRegionNo() != regionInfo.getRegionNo()
                || wc2.getRegionNo() != regionInfo.getRegionNo()) {
            return null;
        }
        Coordinate c1 = convertWorldCoordinate2Coordinate(regionInfo, wc1);
        Coordinate c2 = convertWorldCoordinate2Coordinate(regionInfo, wc2);
        return calculateAngle(new PlanarCoordinate(c1.getX(), c1.getY()), new PlanarCoordinate(c2.getX(), c2.getY()));
    }

    /**
     * calculateAngle from c1 to c2 based on x-axis
     * @param c1
     * @param c2
     * @return in degrees
     */
    public static BigDecimal calculateAngle(final PlanarCoordinate c1, final PlanarCoordinate c2) {
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

    /**
     * Z-axis added
     * @param regionInfo
     * @param wc1
     * @param wc2
     * @param amount
     * @return
     */
    public static List<WorldCoordinate> collectEquidistantPoints(RegionInfo regionInfo, WorldCoordinate wc1,
                                                                 WorldCoordinate wc2, int amount) {
        List<WorldCoordinate> rst = new ArrayList<>();
        if (amount < 2) {
            return rst;
        }
        BigDecimal deltaWidth = calculateXDistance(regionInfo, wc1, wc2);
        BigDecimal deltaHeight = calculateYDistance(regionInfo, wc1, wc2);
        BigDecimal deltaZ = calculateZDistance(regionInfo, wc1, wc2);
        if (null == deltaWidth || null == deltaHeight || null == deltaZ) {
            return rst;
        }
        deltaWidth = deltaWidth.divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        deltaHeight = deltaHeight.divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        deltaZ = deltaZ.divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        WorldCoordinate wc3 = new WorldCoordinate(wc1);
        for (int i = 1; i < amount - 1; i++) {
            wc3.getCoordinate().setX(wc3.getCoordinate().getX().add(deltaWidth));
            wc3.getCoordinate().setY(wc3.getCoordinate().getY().add(deltaHeight));
            wc3.getCoordinate().setZ(wc3.getCoordinate().getZ().add(deltaZ));
            fixWorldCoordinate(regionInfo, wc3);
            rst.add(new WorldCoordinate(wc3));
        }
        return rst;
    }

    /**
     * Z-axis added
     * @param c1
     * @param c2
     * @param amount
     * @return
     */
    public static List<Coordinate> collectEquidistantPoints(Coordinate c1, Coordinate c2, int amount) {
        List<Coordinate> rst = new ArrayList<>();
        if (amount < 2) {
            return rst;
        }
        BigDecimal deltaWidth = calculateXDistance(c1, c2)
                .divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        BigDecimal deltaHeight = calculateYDistance(c1, c2)
                .divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        BigDecimal deltaZ = calculateZDistance(c1, c2)
                .divide(BigDecimal.valueOf(amount), 2, RoundingMode.HALF_UP);
        Coordinate c3 = new Coordinate(c1);
        for (int i = 1; i < amount; i++) {
            c3.setX(c3.getX().add(deltaWidth));
            c3.setY(c3.getY().add(deltaHeight));
            c3.setZ(c3.getZ().add(deltaZ));
            rst.add(new Coordinate(c3));
        }
        return rst;
    }

    public static Set<IntegerCoordinate> preSelectLinearSceneCoordinates(Region region, WorldCoordinate wc1,
                                                                         WorldCoordinate wc2) {
        Set<IntegerCoordinate> rst = new HashSet<>();
        if (wc1.getRegionNo() != region.getRegionNo() || wc2.getRegionNo() != region.getRegionNo()) {
            return rst;
        }

        int x0 = wc1.getSceneCoordinate().getX();
        int y0 = wc1.getSceneCoordinate().getY();
        int x1 = wc2.getSceneCoordinate().getX();
        int y1 = wc2.getSceneCoordinate().getY();

        // ===== Bresenham 走格子线：只加入线经过的 scene =====
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0;
        int y = y0;

        while (true) {
            rst.add(new IntegerCoordinate(x, y));
            if (x == x1 && y == y1) {
                break;
            }
            int e2 = err << 1;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }

        // ===== 扩一圈（保持你原先 -1..+1 的容错范围）=====
        Set<IntegerCoordinate> expanded = expandSceneCoordinates(rst, 1);

        // ===== 过滤掉 region 不存在的 scene =====
        Set<IntegerCoordinate> filtered = new HashSet<>();
        for (IntegerCoordinate sc : expanded) {
            if (region.getScenes().containsKey(sc)) {
                filtered.add(sc);
            }
        }
        return filtered;
    }

    public static Set<IntegerCoordinate> expandSceneCoordinates(Set<IntegerCoordinate> sceneCoordinates, int range) {
        if (range <= 0) {
            return sceneCoordinates;
        }
        Set<IntegerCoordinate> set = new HashSet<>();
        for (IntegerCoordinate sceneCoordinate : sceneCoordinates) {
            for (int i = sceneCoordinate.getY() - 1; i <= sceneCoordinate.getY() + 1; i++) {
                for (int j = sceneCoordinate.getX() - 1; j <= sceneCoordinate.getX() + 1; j++) {
                    set.add(new IntegerCoordinate(j, i));
                }
            }
        }
        return expandSceneCoordinates(set, range - 1);
    }

    /**
     * 通义千问
     * @param coordinate0
     * @param coordinate1
     * @param coordinate2
     * @return
     */
    public static Coordinate findClosestPoint(Coordinate coordinate0, Coordinate coordinate1, Coordinate coordinate2) {
        Coordinate segmentVector = new Coordinate(coordinate1.getX().subtract(coordinate0.getX()),
                coordinate1.getY().subtract(coordinate0.getY()), coordinate1.getZ().subtract(coordinate0.getZ()));
        Coordinate pointVector = new Coordinate(coordinate2.getX().subtract(coordinate0.getX()),
                coordinate2.getY().subtract(coordinate0.getY()), coordinate2.getZ().subtract(coordinate0.getZ()));

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
            return new Coordinate(
                    newX.round(new MathContext(3, RoundingMode.FLOOR)),
                    newY.round(new MathContext(3, RoundingMode.FLOOR)),
                    coordinate0.getZ());
        }
    }

    /**
     * 计算两个三维坐标的点积（内积）
     *
     * @param coordinate1 第一个坐标向量
     * @param coordinate2 第二个坐标向量
     * @return 点积结果
     */
    private static BigDecimal dotProduct(Coordinate coordinate1, Coordinate coordinate2) {
        return coordinate1.getX().multiply(coordinate2.getX())
                .add(coordinate1.getY().multiply(coordinate2.getY()))
                .add(coordinate1.getZ().multiply(coordinate2.getZ()));
    }

    /**
     * 计算三维向量的模长（大小）
     * @param coordinate 向量坐标
     * @return 向量模长
     */
    private static BigDecimal vectorMagnitude(Coordinate coordinate) {
        double x = coordinate.getX().doubleValue();
        double y = coordinate.getY().doubleValue();
        double z = coordinate.getZ().doubleValue();
        return BigDecimal.valueOf(Math.sqrt(x * x + y * y + z * z));
    }

    /**
     * 计算从from到to的位移向量在从from到target方向上的投影（WorldCoordinate版本）
     * 即：将位移向量 (to - from) 投影到目标向量 (target - from) 上
     *
     * @param regionInfo 区域信息
     * @param from 起始点世界坐标
     * @param to 终点世界坐标
     * @param target 目标方向点世界坐标
     * @return 投影后的三维坐标向量
     */
    public static Coordinate calculateDisplacementProjection(RegionInfo regionInfo, WorldCoordinate from,
                                                             WorldCoordinate to, WorldCoordinate target) {
        // 确保在同一区域内
        if (from.getRegionNo() != to.getRegionNo() || from.getRegionNo() != target.getRegionNo()) {
            return new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        
        return calculateDisplacementProjection(convertWorldCoordinate2Coordinate(regionInfo, from),
                convertWorldCoordinate2Coordinate(regionInfo, to),
                convertWorldCoordinate2Coordinate(regionInfo, target));
    }

    /**
     * 计算从from到to的位移向量在从from到target方向上的投影
     * 即：将位移向量 (to - from) 投影到目标向量 (target - from) 上
     * 
     * @param from 起始点坐标
     * @param to 终点坐标
     * @param target 目标方向点坐标
     * @return 投影后的三维坐标向量
     */
    public static Coordinate calculateDisplacementProjection(Coordinate from, Coordinate to, Coordinate target) {
        // 计算位移向量：from -> to
        Coordinate displacement = new Coordinate(
                to.getX().subtract(from.getX()),
                to.getY().subtract(from.getY()),
                to.getZ().subtract(from.getZ())
        );
        
        // 计算目标向量：from -> target
        Coordinate targetVector = new Coordinate(
                target.getX().subtract(from.getX()),
                target.getY().subtract(from.getY()),
                target.getZ().subtract(from.getZ())
        );
        
        // 计算目标向量的模长平方
        BigDecimal targetMagnitudeSquared = vectorMagnitude(targetVector).pow(2);
        
        // 防止除零错误
        if (targetMagnitudeSquared.compareTo(BigDecimal.ZERO) <= 0) {
            return new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        
        // 计算点积
        BigDecimal dotProduct = dotProduct(displacement, targetVector);
        
        // 计算投影系数：(displacement · targetVector) / |targetVector|²
        BigDecimal projectionScale = dotProduct.divide(targetMagnitudeSquared, 10, RoundingMode.HALF_UP);
        
        // 计算投影向量：scale * targetVector
        Coordinate projection = new Coordinate(
                targetVector.getX().multiply(projectionScale),
                targetVector.getY().multiply(projectionScale),
                targetVector.getZ().multiply(projectionScale)
        );
        
        return projection;
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
            case BlockConstants.BLOCK_TYPE_PLASMA:
            case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
            case BlockConstants.BLOCK_TYPE_MELEE:
            case BlockConstants.BLOCK_TYPE_SHOOT:
            case BlockConstants.BLOCK_TYPE_EXPLOSION:
                return false;
            default:
                return true;
        }
    }

    public static boolean checkBlockTypeGravity(int blockType) {
        switch (blockType) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
            case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
            case BlockConstants.BLOCK_TYPE_MELEE:
            case BlockConstants.BLOCK_TYPE_SHOOT:
            case BlockConstants.BLOCK_TYPE_EXPLOSION:
                return false;
            default:
                return true;
        }
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
        return new Coordinate(
                coordinate.getX().add(BigDecimal.valueOf(distance.doubleValue() * Math.cos(angle))),
                coordinate.getY().subtract(BigDecimal.valueOf(distance.doubleValue() * Math.sin(angle))),
                coordinate.getZ());
    }

    @Deprecated
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

    public static boolean checkMaterials(boolean relocate, int structureMaterial1, int structureMaterial2) {
        return relocate ? BlockUtil.checkMaterialStopMovement(structureMaterial1, structureMaterial2)
                : BlockUtil.checkMaterialCollision(structureMaterial1, structureMaterial2);
    }

    /**
     * Positive object's material VS. Negative object's material
     * @param structureMaterial1 Positive object's material
     * @param structureMaterial2 Negative object's material
     * @return boolean
     */
    public static boolean checkMaterialStopMovement(int structureMaterial1, int structureMaterial2) {
        switch (structureMaterial1) {
            case BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_ALL
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET;
            case BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH:
            case BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_ALL
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET;
            default:
                return checkMaterialCollision(structureMaterial1, structureMaterial2);
        }
    }

    /**
     * Positive object's material VS. Negative object's material
     * @param structureMaterial1 Positive object's material
     * @param structureMaterial2 Negative object's material
     * @return boolean
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
            case BlockConstants.STRUCTURE_MATERIAL_SOLID_NO_FLESH:
            case BlockConstants.STRUCTURE_MATERIAL_PARTICLE_NO_FLESH:
                return structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_ALL
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_SOLID_FLESH
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET
                        || structureMaterial2 == BlockConstants.STRUCTURE_MATERIAL_TARGET_FLESH;
            case BlockConstants.STRUCTURE_MATERIAL_NONE:
            case BlockConstants.STRUCTURE_MATERIAL_TARGET:
            case BlockConstants.STRUCTURE_MATERIAL_TARGET_FLESH:
            default:
                return false;
        }
    }

    public static String convertBlockInfo2ItemNo(BlockInfo blockInfo) {
        return ItemConstants.ITEM_PACK_MAP.get(blockInfo.getCode());
    }

    public static BlockInfo convertItemNo2BlockInfo(String itemNo) {
        int blockCode = ItemConstants.ITEM_BUILD_MAP.get(itemNo);
        return BlockFactory.createBlockInfoByCode(blockCode);
    }

    public static int convertBlockCode2Type(int blockCode) {
        return BlockConstants.BLOCK_CODE_TYPE_MAP.getOrDefault(blockCode, BlockConstants.BLOCK_TYPE_FLOOR);
    }

    public static boolean detectPlanarCollision(RegionInfo regionInfo, WorldCoordinate worldCoordinate1,
                                                WorldCoordinate worldCoordinate2, Structure structure1,
                                                Structure structure2) {
        if (worldCoordinate1.getRegionNo() != regionInfo.getRegionNo()
                || worldCoordinate2.getRegionNo() != regionInfo.getRegionNo()) {
            return false;
        }
        Coordinate coordinate1 = BlockUtil.convertWorldCoordinate2Coordinate(regionInfo, worldCoordinate1);
        Coordinate coordinate2 = BlockUtil.convertWorldCoordinate2Coordinate(regionInfo, worldCoordinate2);
        Shape shape1 = structure1.getShape();
        Shape shape2 = structure2.getShape();
        return detectPlanarCollision(coordinate1, coordinate2, shape1, shape2);
    }

    private static boolean detectPlanarCollision(Coordinate coordinate1, Coordinate coordinate2, Shape shape1,
                                                 Shape shape2) {
        // ===== 坐标（double）=====
        final double x1 = coordinate1.getX().doubleValue();
        final double y1 = coordinate1.getY().doubleValue();
        final double x2 = coordinate2.getX().doubleValue();
        final double y2 = coordinate2.getY().doubleValue();

        final double dx = Math.abs(x1 - x2);
        final double dy = Math.abs(y1 - y2);

        // ===== 类型（只识别 ROUND，其它都当 RECTANGLE）=====
        final boolean round1 = shape1.getShapeType() == BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND;
        final boolean round2 = shape2.getShapeType() == BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND;

        // ===== 半径/半边长（double）=====
        // ROUND: 用 radius.x 作为半径
        // RECTANGLE: 用 radius.x/radius.y 作为半宽/半高
        final double r1x = shape1.getRadius().getX().doubleValue();
        final double r1y = shape1.getRadius().getY().doubleValue();
        final double r2x = shape2.getRadius().getX().doubleValue();
        final double r2y = shape2.getRadius().getY().doubleValue();

        // ===== 圆-圆 =====
        if (round1 && round2) {
            final double rr = r1x + r2x;
            return (dx * dx + dy * dy) < (rr * rr); // 用平方避免 sqrt
        }

        // ===== 矩形-矩形（或非圆都当矩形）=====
        if (!round1 && !round2) {
            return dx < (r1x + r2x) && dy < (r1y + r2y);
        }

        // ===== 圆-矩形（保证 shape1 是圆，shape2 是矩形）=====
        if (!round1) {
            // swap
            return detectPlanarCollision(coordinate2, coordinate1, shape2, shape1);
        }

        // 现在：shape1 圆，shape2 矩形
        final double circleR = r1x;
        final double rectHalfW = r2x;
        final double rectHalfH = r2y;

        // 快速排除（外接判断）
        if (dx >= (rectHalfW + circleR) || dy >= (rectHalfH + circleR)) {
            return false;
        }
        // 圆心投影落在矩形范围内 => 必碰
        if (dx < rectHalfW || dy < rectHalfH) {
            return true;
        }
        // 检查角点
        final double cornerDx = dx - rectHalfW;
        final double cornerDy = dy - rectHalfH;
        return (cornerDx * cornerDx + cornerDy * cornerDy) < (circleR * circleR);
    }

    public static boolean detectVerticalCollision(WorldCoordinate worldCoordinate1, WorldCoordinate worldCoordinate2,
                                                  Structure structure1, Structure structure2) {
        if (worldCoordinate1.getRegionNo() != worldCoordinate2.getRegionNo()) {
            return false;
        }

        final double z1 = worldCoordinate1.getCoordinate().getZ().doubleValue();
        final double z2 = worldCoordinate2.getCoordinate().getZ().doubleValue();

        final double h1 = structure1.getShape().getRadius().getZ().doubleValue();
        final double h2 = structure2.getShape().getRadius().getZ().doubleValue();

        return (z1 + h1) > z2 && (z2 + h2) > z1;
    }

    public static void limitSpeed(Coordinate speed, BigDecimal speedXAbs, BigDecimal speedYAbs, BigDecimal speedZAbs) {
        if (null != speedXAbs) {
            BigDecimal currentX = speed.getX();
            BigDecimal limitedX;
            if (currentX.abs().compareTo(speedXAbs) > 0) {
                if (currentX.compareTo(BigDecimal.ZERO) >= 0) {
                    limitedX = speedXAbs;
                } else {
                    limitedX = speedXAbs.negate();
                }
            } else {
                limitedX = currentX;
            }
            speed.setX(limitedX);
        }
        if (null != speedYAbs) {
            BigDecimal currentY = speed.getY();
            BigDecimal limitedY;
            if (currentY.abs().compareTo(speedYAbs) > 0) {
                if (currentY.compareTo(BigDecimal.ZERO) >= 0) {
                    limitedY = speedYAbs;
                } else {
                    limitedY = speedYAbs.negate();
                }
            } else {
                limitedY = currentY;
            }
            speed.setY(limitedY);
        }
        if (null != speedZAbs) {
            BigDecimal currentZ = speed.getZ();
            BigDecimal limitedZ;
            if (currentZ.abs().compareTo(speedZAbs) > 0) {
                if (currentZ.compareTo(BigDecimal.ZERO) >= 0) {
                    limitedZ = speedZAbs;
                } else {
                    limitedZ = speedZAbs.negate();
                }
            } else {
                limitedZ = currentZ;
            }
            speed.setZ(limitedZ);
        }
    }

    public static BigDecimal changeAngle(BigDecimal oldAngle, BigDecimal deltaAngle) {
        return oldAngle.add(deltaAngle).remainder(BigDecimal.valueOf(360));
    }
}
