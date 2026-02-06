package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.MovementConstants;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.coordinate.PlanarCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.scene.Scene;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WebSocketService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
public class MovementManagerImpl implements MovementManager {

    private static final Log logger = LogFactory.getLog(MovementManagerImpl.class);
    private static final Random random = new Random();

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private WorldService worldService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void settleCreatureAcceleration(GameWorld world, Block block, PlanarCoordinate accelerationCoordinate,
                                           int movementMode) {
        BigDecimal altitude = sceneManager.getAltitude(world, block.getWorldCoordinate());
        if (block.getWorldCoordinate().getCoordinate().getZ().compareTo(altitude) > 0) {
            return;
        }
        if (accelerationCoordinate.getX().equals(BigDecimal.ZERO)
                && accelerationCoordinate.getY().equals(BigDecimal.ZERO)) {
            BlockUtil.limitSpeed(block.getMovementInfo().getSpeed(), BigDecimal.ZERO, BigDecimal.ZERO, null);
        } else {
            BigDecimal maxPlanarSpeed;
            switch (movementMode) {
                case MovementConstants.MOVEMENT_MODE_STAND_GROUND:
                    maxPlanarSpeed = BigDecimal.ZERO;
                    break;
                case MovementConstants.MOVEMENT_MODE_WALK:
                    maxPlanarSpeed = block.getMovementInfo().getMaxPlanarSpeed().multiply(BigDecimal.valueOf(0.5D));
                    break;
                case MovementConstants.MOVEMENT_MODE_DEFAULT:
                    maxPlanarSpeed = block.getMovementInfo().getMaxPlanarSpeed();
                    break;
                default:
                    maxPlanarSpeed = null;
                    break;
            }
            settleAcceleration(world, block, new Coordinate(accelerationCoordinate.getX(),
                    accelerationCoordinate.getY(), BigDecimal.ZERO), maxPlanarSpeed, null);
        }
    }

    /**
     * Negative means downwards
     * @param world
     * @param block
     */
    @Override
    public void settleGravityAcceleration(GameWorld world, Block block) {
        if (!BlockUtil.checkBlockTypeGravity(block.getBlockInfo().getType())) {
            return;
        }
        BigDecimal altitude = sceneManager.getAltitude(world, block.getWorldCoordinate());
        if (block.getWorldCoordinate().getCoordinate().getZ().compareTo(altitude) > 0) {
            settleAcceleration(world, block, new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO,
                    block.getMovementInfo().getVerticalAcceleration()), null,
                    block.getMovementInfo().getMaxVerticalSpeed());
        } else {
            BlockUtil.limitSpeed(block.getMovementInfo().getSpeed(), null, null, BigDecimal.ZERO);
        }
    }

    @Override
    public void settleAcceleration(GameWorld world, Block block, Coordinate acceleration, BigDecimal maxPlanarSpeed,
                                   BigDecimal maxVerticalSpeed) {
//        block.getMovementInfo().getSpeed().setX(block.getMovementInfo().getSpeed().getX().add(acceleration.getX()));
//        block.getMovementInfo().getSpeed().setY(block.getMovementInfo().getSpeed().getY().add(acceleration.getY()));
//        BigDecimal altitude = sceneManager.getAltitude(world, block.getWorldCoordinate());
//        BigDecimal newAltitude = block.getWorldCoordinate().getCoordinate().getZ()
//                .add(block.getMovementInfo().getSpeed().getZ());
//        if (altitude.compareTo(newAltitude) > 0) {
//            block.getMovementInfo().getSpeed().setZ(BigDecimal.ZERO);
//        } else {
//            block.getMovementInfo().getSpeed().setZ(block.getMovementInfo().getSpeed().getZ().add(acceleration.getZ()));
//        }
        BigDecimal speedX = block.getMovementInfo().getSpeed().getX().add(acceleration.getX());
        BigDecimal speedY = block.getMovementInfo().getSpeed().getY().add(acceleration.getY());

        // 更新方向
        block.getMovementInfo().setFaceDirection(BlockUtil.calculateAngle(new PlanarCoordinate(),
                new PlanarCoordinate(acceleration.getX(), acceleration.getY())));
        
        // 计算最大速度分量
        BigDecimal maxSpeedXAbs = null == maxPlanarSpeed ? null : maxPlanarSpeed.multiply(BigDecimal.valueOf(
                Math.cos(block.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI))).abs();
        BigDecimal maxSpeedYAbs = null == maxPlanarSpeed ? null : maxPlanarSpeed.multiply(BigDecimal.valueOf(
                Math.sin(block.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI)).negate()).abs();
        
        // 限制speedX和speedY使其绝对值不超过对应的最大值
        if (maxSpeedXAbs != null && speedX.abs().compareTo(maxSpeedXAbs) > 0) {
            speedX = speedX.compareTo(BigDecimal.ZERO) >= 0 ? maxSpeedXAbs : maxSpeedXAbs.negate();
        }
        if (maxSpeedYAbs != null && speedY.abs().compareTo(maxSpeedYAbs) > 0) {
            speedY = speedY.compareTo(BigDecimal.ZERO) >= 0 ? maxSpeedYAbs : maxSpeedYAbs.negate();
        }

        BigDecimal speedZ = block.getMovementInfo().getSpeed().getZ().add(acceleration.getZ());
        if (maxVerticalSpeed != null && speedZ.abs().compareTo(maxVerticalSpeed) > 0) {
            speedZ = speedZ.compareTo(BigDecimal.ZERO) >= 0 ? maxVerticalSpeed : maxVerticalSpeed.negate();
        }
        
        // 更新速度分量
        block.getMovementInfo().setSpeed(new Coordinate(speedX, speedY, speedZ));
    }

    @Override
    public void settleSpeed(GameWorld world, Block worldMovingBlock) {
        if (BigDecimal.ZERO.equals(worldMovingBlock.getMovementInfo().getSpeed().getX())
                && BigDecimal.ZERO.equals(worldMovingBlock.getMovementInfo().getSpeed().getY())
                && BigDecimal.ZERO.equals(worldMovingBlock.getMovementInfo().getSpeed().getZ())) {
            return;
        }

        // Settle planar speed
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        Set<Block> blockers = new HashSet<>();
        WorldCoordinate destination = null;
        boolean xCollision = false;
        boolean yCollision = false;

        Block expectedNewBlockX = new Block(worldMovingBlock);
        Block expectedNewBlockY = new Block(worldMovingBlock);
        Block expectedNewBlockXY = new Block(worldMovingBlock);
        expectedNewBlockX.getWorldCoordinate().setCoordinate(new Coordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY(),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()));
        expectedNewBlockY.getWorldCoordinate().setCoordinate(new Coordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX(),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getY()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()));
        expectedNewBlockXY.getWorldCoordinate().setCoordinate(new Coordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getY()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()));
        BlockUtil.fixWorldCoordinate(region, expectedNewBlockX.getWorldCoordinate());
        BlockUtil.fixWorldCoordinate(region, expectedNewBlockY.getWorldCoordinate());
        BlockUtil.fixWorldCoordinate(region, expectedNewBlockXY.getWorldCoordinate());

        // Linear selection on pre-selected blocks
        Set<Block> preSelectedBlocks = new HashSet<>();
        preSelectedBlocks.addAll(sceneManager.collectBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockXY));
        preSelectedBlocks.addAll(sceneManager.collectBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockX));
        preSelectedBlocks.addAll(sceneManager.collectBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockY));

        if (sceneManager.getAltitude(world, expectedNewBlockX.getWorldCoordinate())
                .subtract(expectedNewBlockX.getWorldCoordinate().getCoordinate().getZ())
                .compareTo(MovementConstants.MAX_VERTICAL_STEP_DEFAULT) > 0) {
            xCollision = true;
        }
        if (sceneManager.getAltitude(world, expectedNewBlockY.getWorldCoordinate())
                .subtract(expectedNewBlockY.getWorldCoordinate().getCoordinate().getZ())
                .compareTo(MovementConstants.MAX_VERTICAL_STEP_DEFAULT) > 0) {
            yCollision = true;
        }
        if (sceneManager.getAltitude(world, expectedNewBlockXY.getWorldCoordinate())
                .subtract(expectedNewBlockXY.getWorldCoordinate().getCoordinate().getZ())
                .compareTo(MovementConstants.MAX_VERTICAL_STEP_DEFAULT) > 0) {
            xCollision = true;
            yCollision = true;
        }
        boolean planarTerrainCollision = xCollision || yCollision;

        for (Block block : preSelectedBlocks) {
            if (detectCollision(world, worldMovingBlock, block, false)) {
                blockers.add(block);
                continue;
            }
            if (xCollision && yCollision) {
                break;
            }
            if (!xCollision) {
                if (detectCollision(world, expectedNewBlockX, block, true)) {
                    xCollision = true;
                }
                if (detectCollision(world, expectedNewBlockX, block, false)) {
                    blockers.add(block);
                }
            }
            if (!yCollision) {
                if (detectCollision(world, expectedNewBlockY, block, true)) {
                    yCollision = true;
                }
                if (detectCollision(world, expectedNewBlockY, block, false)) {
                    blockers.add(block);
                }
            }
            if (!xCollision || !yCollision) {
                if (detectCollision(world, expectedNewBlockXY, block, true)) {
                    xCollision = true;
                    yCollision = true;
                }
                if (detectCollision(world, expectedNewBlockXY, block, false)) {
                    blockers.add(block);
                }
            }
        }
        if (xCollision) {
            BlockUtil.limitSpeed(worldMovingBlock.getMovementInfo().getSpeed(), BigDecimal.ZERO, null, null);
        }
        if (yCollision) {
            BlockUtil.limitSpeed(worldMovingBlock.getMovementInfo().getSpeed(), null, BigDecimal.ZERO, null);
        }

        // Settle vertical speed
        Block expectedNewBlockXYZ = new Block(worldMovingBlock);
        expectedNewBlockXYZ.getWorldCoordinate().setCoordinate(new Coordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getY()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getZ())));
        BlockUtil.fixWorldCoordinate(region, expectedNewBlockXYZ.getWorldCoordinate());
        BigDecimal altitude = sceneManager.getAltitude(world, expectedNewBlockXYZ.getWorldCoordinate());
        BigDecimal newAltitude = expectedNewBlockXYZ.getWorldCoordinate().getCoordinate().getZ();
        boolean zCollision = altitude.compareTo(newAltitude) > 0;
        boolean verticalTerrainCollision = zCollision;

        for (Block block : preSelectedBlocks) {
            if (detectCollision(world, worldMovingBlock, block, false)) {
                blockers.add(block);
                continue;
            }
            if (zCollision) {
                break;
            } else {
                if (detectCollision(world, expectedNewBlockXYZ, block, true)) {
                    zCollision = true;
                }
                if (detectCollision(world, expectedNewBlockXYZ, block, false)) {
                    blockers.add(block);
                }
            }
        }
        if (zCollision) {
            BlockUtil.limitSpeed(worldMovingBlock.getMovementInfo().getSpeed(), null, null, BigDecimal.ZERO);
        }

        // Settle worldMovingBlock position
        Optional<Block> teleport = blockers.stream()
                .filter(blocker -> blocker.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_TELEPORT)
                .findFirst();
        if (teleport.isPresent()) {
            destination = world.getTeleportMap().get(teleport.get().getBlockInfo().getId());
            BlockUtil.fixWorldCoordinate(region, destination);
            settleCoordinate(world, worldMovingBlock, destination, true);
        } else if (!BigDecimal.ZERO.equals(worldMovingBlock.getMovementInfo().getSpeed().getX())
                || !BigDecimal.ZERO.equals(worldMovingBlock.getMovementInfo().getSpeed().getY())
                || !BigDecimal.ZERO.equals(worldMovingBlock.getMovementInfo().getSpeed().getZ())) {
            blockers.forEach(blocker -> {
                BigDecimal relativeVelocity = BigDecimal.valueOf(Math.sqrt(
                        worldMovingBlock.getMovementInfo().getSpeed().getX()
                                .subtract(blocker.getMovementInfo().getSpeed().getX()).pow(2)
                                .add(worldMovingBlock.getMovementInfo().getSpeed().getY()
                                        .subtract(blocker.getMovementInfo().getSpeed().getY()).pow(2))
                                .add(worldMovingBlock.getMovementInfo().getSpeed().getZ()
                                        .subtract(blocker.getMovementInfo().getSpeed().getZ()).pow(2))
                                .doubleValue()));
                settleBlockCollision(world, worldMovingBlock, blocker, relativeVelocity);
            });
            destination = new WorldCoordinate(
                    worldMovingBlock.getWorldCoordinate().getRegionNo(),
                    worldMovingBlock.getWorldCoordinate().getSceneCoordinate(),
                    new Coordinate(
                            worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                                    .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                            worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                                    .add(worldMovingBlock.getMovementInfo().getSpeed().getY()),
                            altitude.compareTo(newAltitude) > 0 ? altitude : newAltitude));
            if (planarTerrainCollision && verticalTerrainCollision) {
                settleTerrainCollision(world, worldMovingBlock, destination, BigDecimal.valueOf(Math.sqrt(
                        worldMovingBlock.getMovementInfo().getSpeed().getX().pow(2)
                                .add(worldMovingBlock.getMovementInfo().getSpeed().getY().pow(2))
                                .add(worldMovingBlock.getMovementInfo().getSpeed().getZ().pow(2))
                                .doubleValue())));
            } else if (planarTerrainCollision) {
                settleTerrainCollision(world, worldMovingBlock, destination, BigDecimal.valueOf(Math.sqrt(
                        worldMovingBlock.getMovementInfo().getSpeed().getX().pow(2)
                                .add(worldMovingBlock.getMovementInfo().getSpeed().getY().pow(2))
                                .doubleValue())));
            } else if (verticalTerrainCollision) {
                settleTerrainCollision(world, worldMovingBlock, destination, BigDecimal.valueOf(Math.sqrt(
                        worldMovingBlock.getMovementInfo().getSpeed().getZ().pow(2).doubleValue())));
            }
            BlockUtil.fixWorldCoordinate(region, destination);
            settleCoordinate(world, worldMovingBlock, destination, false);
        }
    }

    @Override
    public void settleCoordinate(GameWorld world, Block worldMovingBlock, final WorldCoordinate newWorldCoordinate,
                                 boolean isTeleport) {
        final WorldCoordinate oldWorldCoordinate = new WorldCoordinate(worldMovingBlock.getWorldCoordinate());
        worldService.expandByCoordinate(world, oldWorldCoordinate, newWorldCoordinate, 1);
        boolean isRegionChanged = oldWorldCoordinate.getRegionNo() != newWorldCoordinate.getRegionNo();
        boolean isSceneChanged = isRegionChanged
                || !oldWorldCoordinate.getSceneCoordinate().getX()
                .equals(newWorldCoordinate.getSceneCoordinate().getX())
                || !oldWorldCoordinate.getSceneCoordinate().getY()
                .equals(newWorldCoordinate.getSceneCoordinate().getY());

        BlockUtil.copyWorldCoordinate(newWorldCoordinate, worldMovingBlock.getWorldCoordinate());
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        syncFloorCode(world, worldMovingBlock);
        if (worldMovingBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER) {
            if (world.getPlayerInfoMap().get(worldMovingBlock.getBlockInfo().getId()).getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN) {
                if (isTeleport) {
                    sceneManager.updateBlockAltitude(world, worldMovingBlock);
                    world.getFlagMap().get(worldMovingBlock.getBlockInfo().getId())[FlagConstants.FLAG_UPDATE_MOVEMENT] = true;
                }
                // Check location change
                if (isSceneChanged) {
                    webSocketService.resetPlayerBlockMapByUser(worldMovingBlock.getBlockInfo().getId());
                    world.getFlagMap().get(worldMovingBlock.getBlockInfo().getId())[FlagConstants.FLAG_UPDATE_GRIDS] = true;
                    Scene scene = region.getScenes().get(worldMovingBlock.getWorldCoordinate().getSceneCoordinate());
                    playerService.generateNotificationMessage(worldMovingBlock.getBlockInfo().getId(),
                            "来到【" + region.getName() + "-" + scene.getName() + "】");
                }
            }
        } else if (isSceneChanged) {
            region.getScenes().get(newWorldCoordinate.getSceneCoordinate()).getBlocks()
                    .put(worldMovingBlock.getBlockInfo().getId(), worldMovingBlock);
            region.getScenes().get(oldWorldCoordinate.getSceneCoordinate()).getBlocks()
                    .remove(worldMovingBlock.getBlockInfo().getId());
        }
    }

    @Override
    public void syncFloorCode(GameWorld world, Block worldMovingBlock) {
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        if (null == region) {
            logger.error(ErrorUtil.ERROR_1027);
            worldService.expandRegion(world, worldMovingBlock.getWorldCoordinate().getRegionNo());
            return;
        }
        if (!region.getScenes().containsKey(worldMovingBlock.getWorldCoordinate().getSceneCoordinate())) {
            worldService.expandScene(world, worldMovingBlock.getWorldCoordinate(), 1);
        }
        Scene scene = region.getScenes().get(worldMovingBlock.getWorldCoordinate().getSceneCoordinate());
        if (null != scene.getGrid() && null != scene.getGrid()[0]) {
            int code = sceneManager.getGridBlockCode(world, worldMovingBlock.getWorldCoordinate());
            worldMovingBlock.getMovementInfo().setFloorCode(code);
        }
    }

    @Override
    public void updateCreatureMaxSpeed(GameWorld world, String userCode) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        double maxSpeedCoef = 1D;
        switch (player.getMovementInfo().getFloorCode()) {
            case BlockConstants.BLOCK_CODE_SWAMP:
                maxSpeedCoef = 0.2D;
                break;
            case BlockConstants.BLOCK_CODE_SAND:
                maxSpeedCoef = 0.4D;
                break;
            case BlockConstants.BLOCK_CODE_SNOW:
            case BlockConstants.BLOCK_CODE_LAVA:
            case BlockConstants.BLOCK_CODE_WATER_MEDIUM:
                maxSpeedCoef = 0.6D;
                break;
            case BlockConstants.BLOCK_CODE_ROUGH:
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
            case BlockConstants.BLOCK_CODE_WATER_SHALLOW:
                maxSpeedCoef = 0.8D;
                break;
            case BlockConstants.BLOCK_CODE_BLACK:
            case BlockConstants.BLOCK_CODE_GRASS:
            case BlockConstants.BLOCK_CODE_DIRT:
            case BlockConstants.BLOCK_CODE_WATER_DEEP:
            default:
                break;
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_FRACTURED] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_OVERWEIGHTED] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] != 0) {
            maxSpeedCoef /= 2;
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_STUNNED] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
            maxSpeedCoef = 0D;
        }
        player.getMovementInfo().setMaxPlanarSpeed(MovementConstants.MAX_PLANAR_SPEED_DEFAULT
                .multiply(BigDecimal.valueOf(maxSpeedCoef)));
        player.getMovementInfo().setPlanarAcceleration(player.getMovementInfo().getMaxPlanarSpeed()
                .multiply(MovementConstants.MAX_PLANAR_ACCELERATION_SPEED_RATIO));

        player.getMovementInfo().setMaxVerticalSpeed(MovementConstants.MAX_VERTICAL_SPEED_DEFAULT);
        player.getMovementInfo().setVerticalAcceleration(MovementConstants.VERTICAL_ACCELERATION_DEFAULT);
    }

    @Override
    public boolean detectCollision(GameWorld world, Block block1, Block block2, boolean relocate) {
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        Structure structure1 = structureMap.getOrDefault(block1.getBlockInfo().getCode(), new Structure());
        Structure structure2 = structureMap.getOrDefault(block2.getBlockInfo().getCode(), new Structure());
        if (null == structure1 || null == structure2) {
            return false;
        }
        WorldCoordinate worldCoordinate1 = block1.getWorldCoordinate();
        WorldCoordinate worldCoordinate2 = block2.getWorldCoordinate();
        Region region = world.getRegionMap().get(worldCoordinate1.getRegionNo());
        return BlockUtil.checkMaterials(relocate, structure1.getMaterial(), structure2.getMaterial())
                && BlockUtil.detectPlanarCollision(region, worldCoordinate1, worldCoordinate2, structure1, structure2)
                && BlockUtil.detectVerticalCollision(worldCoordinate1, worldCoordinate2, structure1, structure2);
    }

    @Override
    public boolean detectLinearCollision(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                         boolean relocate) {
        Region region = world.getRegionMap().get(from.getRegionNo());
        if (block1.getWorldCoordinate().getRegionNo() != region.getRegionNo()
                || block2.getWorldCoordinate().getRegionNo() != region.getRegionNo()) {
            return false;
        }
//        if (BlockUtil.compareAnglesInDegrees(
//                BlockUtil.calculateAngle(region, from, block2.getWorldCoordinate()).doubleValue(),
//                BlockUtil.calculateAngle(region, from, block1.getWorldCoordinate()).doubleValue()) > 135D) {
//            return false;
//        }

        // ===== 1) 把 world 坐标转成“全局平面坐标”(double)：x + sceneX*height, y + sceneY*width（与你 BlockUtil.adjustCoordinate 保持一致）=====
        final int h = region.getHeight();
        final int w = region.getWidth();

        // from
        final double x0 = from.getCoordinate().getX().doubleValue() + from.getSceneCoordinate().getX() * (double) h;
        final double y0 = from.getCoordinate().getY().doubleValue() + from.getSceneCoordinate().getY() * (double) w;

        // block1
        final WorldCoordinate wc1 = block1.getWorldCoordinate();
        final double x1 = wc1.getCoordinate().getX().doubleValue() + wc1.getSceneCoordinate().getX() * (double) h;
        final double y1 = wc1.getCoordinate().getY().doubleValue() + wc1.getSceneCoordinate().getY() * (double) w;

        // block2
        final WorldCoordinate wc2 = block2.getWorldCoordinate();
        final double x2 = wc2.getCoordinate().getX().doubleValue() + wc2.getSceneCoordinate().getX() * (double) h;
        final double y2 = wc2.getCoordinate().getY().doubleValue() + wc2.getSceneCoordinate().getY() * (double) w;

        // ===== 2) 线段最近点投影（double）=====
        final double vx = x1 - x0;
        final double vy = y1 - y0;
        final double wx = x2 - x0;
        final double wy = y2 - y0;

        final double vv = vx * vx + vy * vy;
        if (vv <= 1e-12) {
            // from 和 block1 基本重合，退化：最近点就当 x0,y0
            return false;
        }

        double t = (wx * vx + wy * vy) / vv;  // 投影参数
        if (t < 0) t = 0;
        else if (t > 1) t = 1;

        final double px = x0 + t * vx;
        final double py = y0 + t * vy;

        // ===== 3) 把最近点 P 写回一个“临时 block3（block1 的复制）”，然后做 detectCollision =====
        Block block3 = new Block(block1);
        WorldCoordinate wc3 = block3.getWorldCoordinate();

        // 先以 block1 的 scene 为基准写入局部坐标，再 fixWorldCoordinate 纠正跨 scene 的情况（循环次数很少）
        int baseSceneX = wc1.getSceneCoordinate().getX();
        int baseSceneY = wc1.getSceneCoordinate().getY();

        double localX = px - baseSceneX * (double) h;
        double localY = py - baseSceneY * (double) w;

        wc3.setRegionNo(wc1.getRegionNo());
        wc3.getSceneCoordinate().setX(baseSceneX);
        wc3.getSceneCoordinate().setY(baseSceneY);

        wc3.getCoordinate().setX(java.math.BigDecimal.valueOf(localX));
        wc3.getCoordinate().setY(java.math.BigDecimal.valueOf(localY));
        // Z 不变（沿用 block1 的 Z）
        wc3.getCoordinate().setZ(wc1.getCoordinate().getZ());

        // 修正 sceneCoordinate/局部坐标到合法范围
        BlockUtil.fixWorldCoordinate(region, wc3);

        // 精确碰撞检测（这里会用到 structure / material / z 等逻辑）
        if (!detectCollision(world, block3, block2, relocate)) {
            return false;
        }

        // 是否把 block1 真正挪到最近点（保持你原语义）
        if (relocate) {
            BlockUtil.copyWorldCoordinate(wc3, block1.getWorldCoordinate());
        }
        return true;
    }

    /**
     * No material check, no structure detection
     * @param world
     * @param from
     * @param block1
     * @param block2
     * @param sectorAngle
     * @return
     */
    @Override
    public boolean detectSectorInfluence(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                         BigDecimal sectorAngle) {
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        Structure structure1 = structureMap.getOrDefault(block1.getBlockInfo().getCode(), new Structure());
        Structure structure2 = structureMap.getOrDefault(block2.getBlockInfo().getCode(), new Structure());
        if (null == structure1 || null == structure2) {
            return false;
        }
        WorldCoordinate worldCoordinate1 = block1.getWorldCoordinate();
        WorldCoordinate worldCoordinate2 = block2.getWorldCoordinate();
        return BlockUtil.checkMaterials(false, structure1.getMaterial(), structure2.getMaterial())
                && detectPlanarSectorInfluence(world, from, block1, block2, sectorAngle)
                && BlockUtil.detectVerticalCollision(worldCoordinate1, worldCoordinate2, structure1, structure2);
    }

    private boolean detectPlanarSectorInfluence(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                                BigDecimal sectorAngle) {
        Region region = world.getRegionMap().get(from.getRegionNo());
        if (region == null) return false;

        if (block2.getWorldCoordinate().getRegionNo() != region.getRegionNo()) {
            return false;
        }

        BigDecimal radius = BlockUtil.calculatePlanarDistance(region, from, block1.getWorldCoordinate());
        final double r = (radius == null) ? 0D : radius.doubleValue();
        if (r <= 0D) return false;

        final double angleDeg = (sectorAngle == null) ? 0D : sectorAngle.doubleValue();
        if (angleDeg <= 0D) return false;

        final int h = region.getHeight();
        final int w = region.getWidth();

        // 圆心改成 from（玩家位置），而不是 block1(eventBlock)
        final double cx = from.getCoordinate().getX().doubleValue()
                + from.getSceneCoordinate().getX() * (double) h;
        final double cy = from.getCoordinate().getY().doubleValue()
                + from.getSceneCoordinate().getY() * (double) w;

        // block2 center (global)
        final WorldCoordinate wc2 = block2.getWorldCoordinate();
        final double tx = wc2.getCoordinate().getX().doubleValue()
                + wc2.getSceneCoordinate().getX() * (double) h;
        final double ty = wc2.getCoordinate().getY().doubleValue()
                + wc2.getSceneCoordinate().getY() * (double) w;

        final double vx = tx - cx;
        final double vy = ty - cy;
        final double dist2 = vx * vx + vy * vy;

        // 半径判定（平方）
        final double rr = r * r;
        if (dist2 > rr) return false;
        if (dist2 <= 1e-12) return true;

        if (angleDeg >= 360D) return true;

        // faceDirection：0°向右；y 正向向南 => uy = -sin
        BigDecimal faceDirection = block1.getMovementInfo().getFaceDirection();
        final double fd = (faceDirection == null) ? 0D : faceDirection.doubleValue();
        final double rad = Math.toRadians(fd);
        final double ux = Math.cos(rad);
        final double uy = -Math.sin(rad);

        final double dot = ux * vx + uy * vy;
        if (dot < 0D) return false;

        final double halfAngleDeg = angleDeg / 2D;
        final double cosHalf = Math.cos(Math.toRadians(halfAngleDeg));
        final double cos2 = cosHalf * cosHalf;

        // 加一点 epsilon，避免贴边抖动
        return (dot * dot) + 1e-9 >= (dist2 * cos2);
    }

    /**
     * No material check, no structure detection
     * @param world
     * @param from
     * @param block1
     * @param block2
     * @param planarDistance
     * @param verticalDistance
     * @return
     */
    @Override
    public boolean detectCylinderInfluence(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                           BigDecimal planarDistance, BigDecimal verticalDistance) {
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        Structure structure1 = structureMap.getOrDefault(block1.getBlockInfo().getCode(), new Structure());
        Structure structure2 = structureMap.getOrDefault(block2.getBlockInfo().getCode(), new Structure());
        if (null == structure1 || null == structure2) {
            return false;
        }
        WorldCoordinate worldCoordinate1 = block1.getWorldCoordinate();
        WorldCoordinate worldCoordinate2 = block2.getWorldCoordinate();
        return BlockUtil.checkMaterials(false, structure1.getMaterial(), structure2.getMaterial())
                && detectPlanarCylinderInfluence(world, from, block1, block2, planarDistance, verticalDistance)
                && BlockUtil.detectVerticalCollision(worldCoordinate1, worldCoordinate2, structure1, structure2);
    }

    private boolean detectPlanarCylinderInfluence(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                                  BigDecimal planarDistance, BigDecimal verticalDistance) {
        Region region = world.getRegionMap().get(from.getRegionNo());
        if (region == null) return false;

        if (block1.getWorldCoordinate().getRegionNo() != region.getRegionNo()
                || block2.getWorldCoordinate().getRegionNo() != region.getRegionNo()) {
            return false;
        }

        double pd = (planarDistance == null) ? 0D : planarDistance.doubleValue();
        if (pd <= 0) return false;
        double vd = (verticalDistance == null) ? 0D : verticalDistance.doubleValue();
        if (vd <= 0) return false;

        final int h = region.getHeight();
        final int w = region.getWidth();

        final WorldCoordinate wc1 = block1.getWorldCoordinate();
        final WorldCoordinate wc2 = block2.getWorldCoordinate();

        final double x1 = wc1.getCoordinate().getX().doubleValue() + wc1.getSceneCoordinate().getX() * (double) h;
        final double y1 = wc1.getCoordinate().getY().doubleValue() + wc1.getSceneCoordinate().getY() * (double) w;
        final double z1 = wc1.getCoordinate().getZ().doubleValue();

        final double x2 = wc2.getCoordinate().getX().doubleValue() + wc2.getSceneCoordinate().getX() * (double) h;
        final double y2 = wc2.getCoordinate().getY().doubleValue() + wc2.getSceneCoordinate().getY() * (double) w;
        final double z2 = wc2.getCoordinate().getZ().doubleValue();

        final double dx = x2 - x1;
        final double dy = y2 - y1;
        final double dz = z2 - z1;

        return (dx * dx + dy * dy) <= pd * pd && Math.abs(dz) <= vd;
    }

    /**
     * 结合type(替代material), speed, 结算block碰撞block
     * @param world
     * @param block1
     * @param block2
     * @param relativeVelocity
     */
    private void settleBlockCollision(GameWorld world, Block block1, Block block2, BigDecimal relativeVelocity) {
        if (block1.getBlockInfo().getType() > block2.getBlockInfo().getType()
                && block2.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_NORMAL) {
            settleBlockCollision(world, block2, block1, relativeVelocity);
            return;
        }
        String fromId = world.getSourceMap().containsKey(block2.getBlockInfo().getId())
                ? world.getSourceMap().get(block2.getBlockInfo().getId())
                : block2.getBlockInfo().getId();
        switch (block1.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_NORMAL:
                // 普通类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_EFFECT:
                // 效果类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_PLAYER:
                switch (block2.getBlockInfo().getType()) {
                    case BlockConstants.BLOCK_TYPE_PLAYER:
                        collideCreature(world, block1, relativeVelocity.multiply(BigDecimal.valueOf(0.5D)));
                        collideCreature(world, block2, relativeVelocity.multiply(BigDecimal.valueOf(0.5D)));
                        break;
                    case BlockConstants.BLOCK_TYPE_DROP:
                        playerService.useDrop(block1.getBlockInfo().getId(), block2.getBlockInfo().getId());
                        break;
                    case BlockConstants.BLOCK_TYPE_TRAP:
                        switch (block2.getBlockInfo().getCode()) {
                            case BlockConstants.BLOCK_CODE_MINE:
                                if (playerService.validateActiveness(world, block1.getBlockInfo().getId())
                                        && !StringUtils.equals(fromId, block1.getBlockInfo().getId())) {
                                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_EXPLODE, fromId, block2.getWorldCoordinate());
                                    sceneManager.removeBlock(world, block2, true);
                                }
                                break;
                            case BlockConstants.BLOCK_CODE_WIRE_NETTING:
                            case BlockConstants.BLOCK_CODE_CACTUS_1:
                            case BlockConstants.BLOCK_CODE_CACTUS_2:
                            case BlockConstants.BLOCK_CODE_CACTUS_3:
                                if (playerService.validateActiveness(world, block1.getBlockInfo().getId())
                                        && random.nextDouble()
                                        < Math.sqrt(Math.pow(block1.getMovementInfo().getSpeed().getX().doubleValue(), 2)
                                        + Math.pow(block1.getMovementInfo().getSpeed().getY().doubleValue(), 2))
                                        / block1.getMovementInfo().getMaxPlanarSpeed().doubleValue()) {
                                    eventManager.affectBlock(world, block2, block1);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case BlockConstants.BLOCK_TYPE_EFFECT:
                    case BlockConstants.BLOCK_TYPE_TELEPORT:
                    case BlockConstants.BLOCK_TYPE_FARM:
                    case BlockConstants.BLOCK_TYPE_FLOOR_DECORATION:
                    case BlockConstants.BLOCK_TYPE_WALL_DECORATION:
                    case BlockConstants.BLOCK_TYPE_PLASMA:
                    case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
                    case BlockConstants.BLOCK_TYPE_MELEE:
                    case BlockConstants.BLOCK_TYPE_SHOOT:
                    case BlockConstants.BLOCK_TYPE_EXPLOSION:
                        // No effect
                        break;
                    default:
                        collideCreature(world, block1, relativeVelocity);
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                // 掉落物类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_TELEPORT:
                // 传送类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_BED:
                // 床类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_TOILET:
                // 厕所类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_DRESSER:
                // 梳妆台类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_GAME:
                // 游戏类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_STORAGE:
                // 存储类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_COOKER:
                // 烹饪类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_SINK:
                // 水槽类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_CONTAINER:
                // 容器类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_SPEAKER:
                // 音响类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_BUILDING:
                // 建筑类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_TREE:
                // 树木类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_ROCK:
                // 岩石类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                // 农场类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP:
                // 工坊类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
                // 工具工坊类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
                // 弹药工坊类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
                // 装备工坊类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
                // 化学工坊类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                // 回收工坊类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_TRAP:
                // 陷阱类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR:
                // 地板类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR_DECORATION:
                // 地板装饰类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WALL:
                // 墙壁类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_WALL_DECORATION:
                // 墙壁装饰类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_PLASMA:
                // 等离子体类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
                // 人类遗体容器类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                // 动物遗体容器类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
                // 文本显示类型方块的碰撞处理
                break;
            case BlockConstants.BLOCK_TYPE_MELEE:
                // 近战攻击类型方块的碰撞处理 TODO
                break;
            case BlockConstants.BLOCK_TYPE_SHOOT:
                // 射击类型方块的碰撞处理 TODO
                break;
            case BlockConstants.BLOCK_TYPE_EXPLOSION:
                // 爆炸类型方块的碰撞处理
                break;
            default:
                // 未知类型方块的默认处理
                break;
        }
    }

    /**
     * 结合type(替代material), speed, 结算block碰撞terrain
     * @param world
     * @param block1
     * @param worldCoordinate2
     * @param relativeVelocity
     */
    private void settleTerrainCollision(GameWorld world, Block block1, WorldCoordinate worldCoordinate2,
                                        BigDecimal relativeVelocity) {
        switch (block1.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_PLAYER:
                collideCreature(world, block1, relativeVelocity);
                break;
            case BlockConstants.BLOCK_TYPE_MELEE:
                // 近战攻击类型方块的碰撞处理 TODO
                break;
            case BlockConstants.BLOCK_TYPE_SHOOT:
                // 射击类型方块的碰撞处理 TODO
                break;
            default:
                // 未知类型方块的默认处理
                break;
        }
    }

    private void collideCreature(GameWorld world, Block creature, BigDecimal relativeVelocity) {
        switch (creature.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_PLAYER:
                if (relativeVelocity.compareTo(MovementConstants.MIN_CREATURE_LETHAL_SPEED_DEFAULT) >= 0) {
                    playerService.killPlayer(creature.getBlockInfo().getId());
                } else if (relativeVelocity.compareTo(MovementConstants.MIN_CREATURE_INJURIOUS_SPEED_DEFAULT) >= 0) {
                    int changedHp = (int) Math.round(creature.getBlockInfo().getHpMax().doubleValue()
                            * relativeVelocity.doubleValue()
                            / (MovementConstants.MIN_CREATURE_LETHAL_SPEED_DEFAULT.doubleValue()
                            - MovementConstants.MIN_CREATURE_INJURIOUS_SPEED_DEFAULT.doubleValue()));
                    eventManager.changeHp(world, creature, - changedHp, false);
                }
            default:
                break;
        }
    }

    /**
     * 对人类玩家应用摩擦力，当他们停止移动时减慢速度
     */
    public void applyFriction(Block block) {
        // 获取当前速度
        Coordinate currentSpeed = block.getMovementInfo().getSpeed();
            
        // 减少水平速度，使其更快地接近零
        BigDecimal newXSpeed = currentSpeed.getX().multiply(MovementConstants.FRICTION_FACTOR);
        BigDecimal newYSpeed = currentSpeed.getY().multiply(MovementConstants.FRICTION_FACTOR);

        // 如果速度已经非常接近0，则设为0
        if (newXSpeed.abs().compareTo(MovementConstants.MIN_BLOCK_SPEED) < 0) {
            newXSpeed = BigDecimal.ZERO;
        }
        if (newYSpeed.abs().compareTo(MovementConstants.MIN_BLOCK_SPEED) < 0) {
            newYSpeed = BigDecimal.ZERO;
        }

        block.getMovementInfo().setSpeed(new Coordinate(newXSpeed, newYSpeed, currentSpeed.getZ()));
    }
}
