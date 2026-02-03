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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
            limitSpeed(world, block, BigDecimal.ZERO, BigDecimal.ZERO, null);
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
            limitSpeed(world, block, null, null, BigDecimal.ZERO);
        }
    }

    @Override
    public void settleAcceleration(GameWorld world, Block block, Coordinate acceleration, BigDecimal maxPlanarSpeed,
                                   BigDecimal maxVerticalSpeed) {
        block.getMovementInfo().getSpeed().setX(block.getMovementInfo().getSpeed().getX().add(acceleration.getX()));
        block.getMovementInfo().getSpeed().setY(block.getMovementInfo().getSpeed().getY().add(acceleration.getY()));
        block.getMovementInfo().getSpeed().setZ(block.getMovementInfo().getSpeed().getZ().add(acceleration.getZ()));
        block.getMovementInfo().setFaceDirection(BlockUtil.calculateAngle(new PlanarCoordinate(),
                new PlanarCoordinate(block.getMovementInfo().getSpeed().getX(),
                        block.getMovementInfo().getSpeed().getY())));
        BigDecimal speedXAbs = null == maxPlanarSpeed ? null : maxPlanarSpeed.multiply(BigDecimal.valueOf(
                Math.cos(block.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI))).abs();
        BigDecimal speedYAbs = null == maxPlanarSpeed ? null : maxPlanarSpeed.multiply(BigDecimal.valueOf(
                Math.sin(block.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI)).negate()).abs();
        limitSpeed(world, block, speedXAbs, speedYAbs, maxVerticalSpeed);
        settlePlanarSpeed(world, block);
        settleVerticalSpeed(world, block);
    }

    @Override
    public void limitSpeed(GameWorld world, Block block, BigDecimal speedXAbs, BigDecimal speedYAbs,
                           BigDecimal speedZAbs) {
        if (null != speedXAbs) {
            BigDecimal currentX = block.getMovementInfo().getSpeed().getX();
            BigDecimal limitedX = currentX.abs().compareTo(speedXAbs) > 0 ? 
                (currentX.compareTo(BigDecimal.ZERO) >= 0 ? speedXAbs : speedXAbs.negate()) : currentX;
            block.getMovementInfo().getSpeed().setX(limitedX);
        }
        if (null != speedYAbs) {
            BigDecimal currentY = block.getMovementInfo().getSpeed().getY();
            BigDecimal limitedY = currentY.abs().compareTo(speedYAbs) > 0 ? 
                (currentY.compareTo(BigDecimal.ZERO) >= 0 ? speedYAbs : speedYAbs.negate()) : currentY;
            block.getMovementInfo().getSpeed().setY(limitedY);
        }
        if (null != speedZAbs) {
            BigDecimal currentZ = block.getMovementInfo().getSpeed().getZ();
            BigDecimal limitedZ = currentZ.abs().compareTo(speedZAbs) > 0 ? 
                (currentZ.compareTo(BigDecimal.ZERO) >= 0 ? speedZAbs : speedZAbs.negate()) : currentZ;
            block.getMovementInfo().getSpeed().setZ(limitedZ);
        }
    }

    @Override
    public void settlePlanarSpeed(GameWorld world, Block worldMovingBlock) {
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        WorldCoordinate teleportWc = null;
        Block expectedNewBlockX = new Block(worldMovingBlock);
        Block expectedNewBlockY = new Block(worldMovingBlock);
        Block expectedNewBlockXY = new Block(worldMovingBlock);
        expectedNewBlockX.getWorldCoordinate().setCoordinate(new Coordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY(),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getZ())));
        expectedNewBlockY.getWorldCoordinate().setCoordinate(new Coordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX(),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getY()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getZ())));
        expectedNewBlockXY.getWorldCoordinate().setCoordinate(new Coordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getY()),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()
                        .add(worldMovingBlock.getMovementInfo().getSpeed().getZ())));
        BlockUtil.fixWorldCoordinate(region, expectedNewBlockX.getWorldCoordinate());
        BlockUtil.fixWorldCoordinate(region, expectedNewBlockY.getWorldCoordinate());
        BlockUtil.fixWorldCoordinate(region, expectedNewBlockXY.getWorldCoordinate());
        boolean xCollision = false;
        boolean yCollision = false;
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

        // Linear selection on pre-selected blocks
        Set<Block> preSelectedBlocks = new HashSet<>();
        preSelectedBlocks.addAll(sceneManager.collectBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockXY));
        preSelectedBlocks.addAll(sceneManager.collectBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockX));
        preSelectedBlocks.addAll(sceneManager.collectBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockY));

        for (Block block : preSelectedBlocks) {
            if (detectCollision(world, worldMovingBlock, block, false)) {
                continue;
            }
            if (xCollision && yCollision) {
                break;
            }
            if (!xCollision) {
                if (detectCollision(world, expectedNewBlockX, block, false)) {
                    xCollision = true;
                    teleportWc = checkCollisionByType(world, expectedNewBlockX, block);
                    if (null != teleportWc) {
                        yCollision = true;
                        break;
                    }
                }
            }
            if (!yCollision) {
                if (detectCollision(world, expectedNewBlockY, block, false)) {
                    yCollision = true;
                    teleportWc = checkCollisionByType(world, expectedNewBlockY, block);
                    if (null != teleportWc) {
                        xCollision = true;
                        break;
                    }
                }
            }
            if (!xCollision && !yCollision) {
                if (detectCollision(world, expectedNewBlockXY, block, false)) {
                    xCollision = true;
                    yCollision = true;
                    teleportWc = checkCollisionByType(world, expectedNewBlockXY, block);
                    if (null != teleportWc) {
                        break;
                    }
                }
            }
        }
        if (xCollision) {
            limitSpeed(world, worldMovingBlock, BigDecimal.ZERO, null, null);
        }
        if (yCollision) {
            limitSpeed(world, worldMovingBlock, null, BigDecimal.ZERO, null);
        }
        // Settle worldMovingBlock position
        if (null == teleportWc) {
            teleportWc = new WorldCoordinate(
                    worldMovingBlock.getWorldCoordinate().getRegionNo(),
                    worldMovingBlock.getWorldCoordinate().getSceneCoordinate(),
                    new Coordinate(
                            worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                                    .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                            worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                                    .add(worldMovingBlock.getMovementInfo().getSpeed().getY()),
                            worldMovingBlock.getWorldCoordinate().getCoordinate().getZ()
                                    .add(worldMovingBlock.getMovementInfo().getSpeed().getZ())));
            BlockUtil.fixWorldCoordinate(region, teleportWc);
            settleCoordinate(world, worldMovingBlock, teleportWc, false);
        } else {
//            limitSpeed(world, worldMovingBlock, BigDecimal.ZERO, BigDecimal.ZERO, null);
            settleCoordinate(world, worldMovingBlock, teleportWc, true);
        }
    }

    @Override
    public void settleVerticalSpeed(GameWorld world, Block block) {
        BigDecimal altitude = sceneManager.getAltitude(world, block.getWorldCoordinate());
        BigDecimal newAltitude = block.getWorldCoordinate().getCoordinate().getZ()
                .add(block.getMovementInfo().getSpeed().getZ());
        if (altitude.compareTo(newAltitude) > 0) {
            sceneManager.updateBlockAltitude(world, block);
        } else {
            block.getWorldCoordinate().getCoordinate().setZ(newAltitude);
        }
    }

    private WorldCoordinate checkCollisionByType(GameWorld world, Block block1, Block block2) {
        String fromId = world.getSourceMap().containsKey(block2.getBlockInfo().getId())
                ? world.getSourceMap().get(block2.getBlockInfo().getId())
                : block2.getBlockInfo().getId();
        switch (block2.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_DROP:
                playerService.useDrop(block1.getBlockInfo().getId(), block2.getBlockInfo().getId());
                break;
            case BlockConstants.BLOCK_TYPE_TELEPORT:
                return world.getTeleportMap().get(block2.getBlockInfo().getId());
            case BlockConstants.BLOCK_TYPE_TRAP:
                switch (block2.getBlockInfo().getCode()) {
                    case BlockConstants.BLOCK_CODE_MINE:
                        if (block1.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                && playerService.validateActiveness(world, block1.getBlockInfo().getId())
                                && !StringUtils.equals(fromId, block1.getBlockInfo().getId())) {
                            eventManager.addEvent(world, BlockConstants.BLOCK_CODE_EXPLODE, fromId, block2.getWorldCoordinate());
                            sceneManager.removeBlock(world, block2, true);
                        }
                        break;
                    case BlockConstants.BLOCK_CODE_WIRE_NETTING:
                    case BlockConstants.BLOCK_CODE_CACTUS_1:
                    case BlockConstants.BLOCK_CODE_CACTUS_2:
                    case BlockConstants.BLOCK_CODE_CACTUS_3:
                        if (block1.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                && playerService.validateActiveness(world, block1.getBlockInfo().getId())
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
            default:
                break;
        }
        return null;
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
//        Queue<StructuredBlock> rankingQueue = sceneManager.collectSurroundingBlocks(world, worldMovingBlock, 1);
//        rankingQueue.forEach(nearbyBlock -> checkBlockInteraction(world, worldMovingBlock, nearbyBlock.getBlock()));
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

//    private void checkBlockInteraction(GameWorld world, Block block, Block nearbyBlock) {
//        Region region = world.getRegionMap().get(block.getWorldCoordinate().getRegionNo());
//        BigDecimal distance = BlockUtil.calculateDistance(region, block.getWorldCoordinate(),
//                nearbyBlock.getWorldCoordinate());
//        if (null == distance) {
//            return;
//        }
//        String fromId = world.getSourceMap().containsKey(nearbyBlock.getBlockInfo().getId())
//                ? world.getSourceMap().get(nearbyBlock.getBlockInfo().getId())
//                : nearbyBlock.getBlockInfo().getId();
//        Random random = new Random();
//        switch (nearbyBlock.getBlockInfo().getType()) {
//            case BlockConstants.BLOCK_TYPE_DROP:
//                if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
//                        && distance.compareTo(BlockConstants.MIN_DROP_INTERACTION_DISTANCE) < 0) {
//                    playerService.useDrop(block.getBlockInfo().getId(), nearbyBlock.getBlockInfo().getId());
//                }
//                break;
//            case BlockConstants.BLOCK_TYPE_TRAP:
//                switch (nearbyBlock.getBlockInfo().getCode()) {
//                    case BlockConstants.BLOCK_CODE_MINE:
//                        if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
//                                && playerService.validateActiveness(world, block.getBlockInfo().getId())
//                                && !StringUtils.equals(fromId, block.getBlockInfo().getId())
//                                && distance.compareTo(BlockConstants.MINE_RADIUS) < 0) {
//                            eventManager.addEvent(world, BlockConstants.BLOCK_CODE_EXPLODE, fromId, nearbyBlock.getWorldCoordinate());
//                            sceneManager.removeBlock(world, nearbyBlock, true);
//                        }
//                        break;
//                    case BlockConstants.BLOCK_CODE_WIRE_NETTING:
//                    case BlockConstants.BLOCK_CODE_CACTUS_1:
//                    case BlockConstants.BLOCK_CODE_CACTUS_2:
//                    case BlockConstants.BLOCK_CODE_CACTUS_3:
//                        if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
//                                && playerService.validateActiveness(world, block.getBlockInfo().getId())
//                                && distance.compareTo(BlockConstants.WIRE_NETTING_RADIUS) < 0
//                                && random.nextDouble()
//                                < Math.sqrt(Math.pow(block.getMovementInfo().getSpeed().getX().doubleValue(), 2)
//                                + Math.pow(block.getMovementInfo().getSpeed().getY().doubleValue(), 2))
//                                / block.getMovementInfo().getMaxPlanarSpeed().doubleValue()) {
//                            eventManager.affectBlock(world, nearbyBlock, block);
//                        }
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            default:
//                break;
//        }
//    }

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
                && detectPlanarCollision(region, worldCoordinate1, worldCoordinate2, structure1, structure2)
                && detectZCollision(worldCoordinate1, worldCoordinate2, structure1, structure2);
    }

    private boolean detectPlanarCollision(RegionInfo regionInfo, WorldCoordinate worldCoordinate1,
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

    private boolean detectPlanarCollision(Coordinate coordinate1, Coordinate coordinate2, Shape shape1, Shape shape2) {
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

    private boolean detectZCollision(WorldCoordinate worldCoordinate1, WorldCoordinate worldCoordinate2,
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
    public boolean detectSectorCollision(GameWorld world, WorldCoordinate from, Block block1, Block block2,
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
}
