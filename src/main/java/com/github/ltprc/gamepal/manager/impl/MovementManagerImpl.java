package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MovementManagerImpl implements MovementManager {

    private static final Log logger = LogFactory.getLog(MovementManagerImpl.class);

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
    public void speedUpBlock(GameWorld world, Block block, Coordinate deltaSpeed) {
        MovementInfo movementInfo = block.getMovementInfo();
        movementInfo.getSpeed().setX(movementInfo.getSpeed().getX().add(deltaSpeed.getX()));
        movementInfo.getSpeed().setY(movementInfo.getSpeed().getY().add(deltaSpeed.getY()));
        movementInfo.getSpeed().setZ(movementInfo.getSpeed().getZ().add(deltaSpeed.getZ()));
        movementInfo.setFaceDirection(BlockUtil.calculateAngle(new Coordinate(), movementInfo.getSpeed()));
        settleSpeedAndCoordinate(world, block, 0);
    }

    @Override
    public void settleAcceleration(GameWorld world, Block block, Coordinate accelerationCoordinate, int movementMode) {
        if (accelerationCoordinate.getX().equals(BigDecimal.ZERO)
                && accelerationCoordinate.getY().equals(BigDecimal.ZERO)) {
            block.getMovementInfo().setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        } else {
            double newSpeed = Math.sqrt(Math.pow(block.getMovementInfo().getSpeed().getX().doubleValue(), 2)
                    + Math.pow(block.getMovementInfo().getSpeed().getY().doubleValue(), 2))
                    + block.getMovementInfo().getAcceleration().doubleValue()
                    * Math.sqrt(accelerationCoordinate.getX().pow(2)
                    .add(accelerationCoordinate.getY().pow(2)).doubleValue());
            double maxSpeed = block.getMovementInfo().getMaxSpeed().doubleValue()
                    * Math.sqrt(accelerationCoordinate.getX().pow(2)
                    .add(accelerationCoordinate.getY().pow(2)).doubleValue());
            newSpeed = Math.min(newSpeed, maxSpeed);
            switch (movementMode) {
                case BlockConstants.MOVEMENT_MODE_STAND_GROUND:
                    newSpeed = 0D;
                    break;
                case BlockConstants.MOVEMENT_MODE_WALK:
                    newSpeed = Math.min(newSpeed, block.getMovementInfo().getMaxSpeed().doubleValue() / 2);
                    break;
                case BlockConstants.MOVEMENT_MODE_DEFAULT:
                default:
                    break;
            }
            block.getMovementInfo().setSpeed(new Coordinate(
                    BigDecimal.valueOf(newSpeed * accelerationCoordinate.getX().doubleValue()
                            / Math.sqrt(accelerationCoordinate.getX().pow(2)
                            .add(accelerationCoordinate.getY().pow(2)).doubleValue())),
                    BigDecimal.valueOf(newSpeed * accelerationCoordinate.getY().doubleValue()
                            / Math.sqrt(accelerationCoordinate.getX().pow(2)
                            .add(accelerationCoordinate.getY().pow(2)).doubleValue())),
                    block.getMovementInfo().getSpeed().getZ().add(accelerationCoordinate.getZ())));
            block.getMovementInfo().setFaceDirection(BlockUtil.calculateAngle(new Coordinate(), accelerationCoordinate));
        }
        settleSpeedAndCoordinate(world, block, 1);
    }

    @Override
    public void settleSpeedAndCoordinate(GameWorld world, Block worldMovingBlock, int sceneScanDepth) {
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
        String fromId = world.getSourceMap().containsKey(worldMovingBlock.getBlockInfo().getId())
                ? world.getSourceMap().get(worldMovingBlock.getBlockInfo().getId())
                : worldMovingBlock.getBlockInfo().getId();
        boolean xCollision = false;
        boolean yCollision = false;
        if (sceneManager.getAltitude(world, expectedNewBlockX.getWorldCoordinate())
                .subtract(expectedNewBlockX.getWorldCoordinate().getCoordinate().getZ())
                .compareTo(BlockConstants.MAX_Z_STEP_DEFAULT) > 0) {
            xCollision = true;
        }
        if (sceneManager.getAltitude(world, expectedNewBlockY.getWorldCoordinate())
                .subtract(expectedNewBlockY.getWorldCoordinate().getCoordinate().getZ())
                .compareTo(BlockConstants.MAX_Z_STEP_DEFAULT) > 0) {
            yCollision = true;
        }
        if (sceneManager.getAltitude(world, expectedNewBlockXY.getWorldCoordinate())
                .subtract(expectedNewBlockXY.getWorldCoordinate().getCoordinate().getZ())
                .compareTo(BlockConstants.MAX_Z_STEP_DEFAULT) > 0) {
            xCollision = true;
            yCollision = true;
        }

        // Linear selection on pre-selected blocks
        Set<Block> preSelectedBlocks = new HashSet<>();
        preSelectedBlocks.addAll(sceneManager.collectLinearBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockXY, fromId));
        preSelectedBlocks.addAll(sceneManager.collectLinearBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockX, fromId));
        preSelectedBlocks.addAll(sceneManager.collectLinearBlocks(world, worldMovingBlock.getWorldCoordinate(),
                expectedNewBlockY, fromId));
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        preSelectedBlocks.stream()
                .filter(blocker -> region.getRegionNo() == blocker.getWorldCoordinate().getRegionNo())
                .filter(blocker -> structureMap.containsKey(worldMovingBlock.getBlockInfo().getCode())
                        && structureMap.containsKey(blocker.getBlockInfo().getCode())
                        && BlockUtil.checkMaterialCollision(
                                structureMap.get(worldMovingBlock.getBlockInfo().getCode()).getMaterial(),
                        structureMap.get(blocker.getBlockInfo().getCode()).getMaterial()))
                .filter(blocker -> detectLineCollision(world, worldMovingBlock.getWorldCoordinate(),
                        expectedNewBlockXY, blocker, false))
                .collect(Collectors.toList());

        for (Block block : preSelectedBlocks) {
            if (detectCollision(world, worldMovingBlock, block)) {
                continue;
            }
            if (xCollision && yCollision) {
                break;
            }
            if (!xCollision) {
                if (detectCollision(world, expectedNewBlockX, block)) {
                    if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_TELEPORT) {
                        teleportWc = world.getTeleportMap().get(block.getBlockInfo().getId());
                        break;
                    } else {
                        xCollision = true;
                    }
                }
            }
            if (!yCollision) {
                if (detectCollision(world, expectedNewBlockY, block)) {
                    if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_TELEPORT) {
                        teleportWc = world.getTeleportMap().get(block.getBlockInfo().getId());
                        break;
                    } else {
                        yCollision = true;
                    }
                }
            }
            if (!xCollision && !yCollision) {
                if (detectCollision(world, expectedNewBlockXY, block)) {
                    if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_TELEPORT) {
                        teleportWc = world.getTeleportMap().get(block.getBlockInfo().getId());
                        break;
                    } else {
                        xCollision = true;
                        yCollision = true;
                    }
                }
            }
        }
        if (xCollision) {
            worldMovingBlock.getMovementInfo().getSpeed().setX(BigDecimal.ZERO);
        }
        if (yCollision) {
            worldMovingBlock.getMovementInfo().getSpeed().setY(BigDecimal.ZERO);
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
            worldMovingBlock.getMovementInfo().setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            worldService.expandByCoordinate(world, worldMovingBlock.getWorldCoordinate(), teleportWc, sceneScanDepth);
            settleCoordinate(world, worldMovingBlock, teleportWc, true);
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
        worldMovingBlock.getWorldCoordinate().getCoordinate().setZ(sceneManager.getAltitude(world, worldMovingBlock.getWorldCoordinate()));
        BlockUtil.fixWorldCoordinate(region, worldMovingBlock.getWorldCoordinate());
        syncFloorCode(world, worldMovingBlock);
        if (worldMovingBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER) {
            if (world.getPlayerInfoMap().get(worldMovingBlock.getBlockInfo().getId()).getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN) {
                if (isTeleport) {
                    world.getFlagMap().get(worldMovingBlock.getBlockInfo().getId())[FlagConstants.FLAG_UPDATE_MOVEMENT] = true;
                }
                // Check location change
                if (isSceneChanged) {
                    webSocketService.resetPlayerBlockMap(worldMovingBlock.getBlockInfo().getId());
                    world.getFlagMap().get(worldMovingBlock.getBlockInfo().getId())[FlagConstants.FLAG_UPDATE_REGION] = true;
                    Scene scene = region.getScenes().get(worldMovingBlock.getWorldCoordinate().getSceneCoordinate());
                    playerService.generateNotificationMessage(worldMovingBlock.getBlockInfo().getId(),
                            "来到【" + region.getName() + "-" + scene.getName() + "】");
                }
            }
//            region.getScenes().values().stream()
//                    .filter(scene -> SkillUtil.isSceneDetected(worldMovingBlock, scene.getSceneCoordinate(), 1))
//                    .forEach(scene -> scene.getBlocks().values().forEach(block -> {
//                BigDecimal distance = BlockUtil.calculateDistance(region, worldMovingBlock.getWorldCoordinate(),
//                        block.getWorldCoordinate());
//                if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_DROP
//                        && null != distance && distance.compareTo(BlockConstants.MIN_DROP_INTERACTION_DISTANCE) < 0) {
//                    playerService.useDrop(worldMovingBlock.getBlockInfo().getId(), block.getBlockInfo().getId());
//                }
//            }));
        } else if (isSceneChanged) {
            region.getScenes().get(newWorldCoordinate.getSceneCoordinate()).getBlocks()
                    .put(worldMovingBlock.getBlockInfo().getId(), worldMovingBlock);
            region.getScenes().get(oldWorldCoordinate.getSceneCoordinate()).getBlocks()
                    .remove(worldMovingBlock.getBlockInfo().getId());
        }
        Queue<Block> rankingQueue = sceneManager.collectSurroundingBlocks(world, worldMovingBlock, 1);
        rankingQueue.forEach(nearbyBlock -> checkBlockInteraction(world, worldMovingBlock, nearbyBlock));
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

    private void checkBlockInteraction(GameWorld world, Block block, Block nearbyBlock) {
        Region region = world.getRegionMap().get(block.getWorldCoordinate().getRegionNo());
        BigDecimal distance = BlockUtil.calculateDistance(region, block.getWorldCoordinate(),
                nearbyBlock.getWorldCoordinate());
        if (null == distance) {
            return;
        }
        String fromId = world.getSourceMap().containsKey(nearbyBlock.getBlockInfo().getId())
                ? world.getSourceMap().get(nearbyBlock.getBlockInfo().getId())
                : nearbyBlock.getBlockInfo().getId();
        Random random = new Random();
        switch (nearbyBlock.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_DROP:
                if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                        && distance.compareTo(BlockConstants.MIN_DROP_INTERACTION_DISTANCE) < 0) {
                    playerService.useDrop(block.getBlockInfo().getId(), nearbyBlock.getBlockInfo().getId());
                }
                break;
            case BlockConstants.BLOCK_TYPE_TRAP:
                switch (nearbyBlock.getBlockInfo().getCode()) {
                    case BlockConstants.BLOCK_CODE_MINE:
                        if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                && playerService.validateActiveness(world, block.getBlockInfo().getId())
                                && !StringUtils.equals(fromId, block.getBlockInfo().getId())
                                && distance.compareTo(BlockConstants.MINE_RADIUS) < 0) {
                            eventManager.addEvent(world, BlockConstants.BLOCK_CODE_EXPLODE, fromId, nearbyBlock.getWorldCoordinate());
                            sceneManager.removeBlock(world, nearbyBlock, true);
                        }
                        break;
                    case BlockConstants.BLOCK_CODE_WIRE_NETTING:
                        if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                && playerService.validateActiveness(world, block.getBlockInfo().getId())
                                && distance.compareTo(BlockConstants.WIRE_NETTING_RADIUS) < 0
                                && random.nextDouble()
                                < Math.sqrt(Math.pow(block.getMovementInfo().getSpeed().getX().doubleValue(), 2)
                                + Math.pow(block.getMovementInfo().getSpeed().getY().doubleValue(), 2))
                                / block.getMovementInfo().getMaxSpeed().doubleValue()) {
                            eventManager.affectBlock(world, nearbyBlock, block);
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
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
        player.getMovementInfo().setMaxSpeed(BlockConstants.MAX_SPEED_DEFAULT.multiply(BigDecimal.valueOf(maxSpeedCoef)));
        player.getMovementInfo().setAcceleration(player.getMovementInfo().getMaxSpeed().multiply(BlockConstants.ACCELERATION_MAX_SPEED_RATIO));
    }

    @Override
    public boolean detectCollision(GameWorld world, Block block1, Block block2) {
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        if (structureMap.containsKey(block1.getBlockInfo().getCode())
                && structureMap.containsKey(block2.getBlockInfo().getCode())
                && !BlockUtil.checkMaterialStopMovement(
                structureMap.get(block1.getBlockInfo().getCode()).getMaterial(),
                structureMap.get(block2.getBlockInfo().getCode()).getMaterial())) {
            return false;
        }
        Region region = world.getRegionMap().get(block1.getWorldCoordinate().getRegionNo());
        return detectPlanarCollision(region, block1, block2) && detectZCollision(block1, block2);
    }

    private boolean detectPlanarCollision(RegionInfo regionInfo, Block block1, Block block2) {
        if (block1.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()
                || block2.getWorldCoordinate().getRegionNo() != regionInfo.getRegionNo()) {
            return false;
        }
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        if (!structureMap.containsKey(block1.getBlockInfo().getCode())
                || !structureMap.containsKey(block2.getBlockInfo().getCode())) {
            return false;
        }
        Coordinate coordinate1 = BlockUtil.convertWorldCoordinate2Coordinate(regionInfo, block1.getWorldCoordinate());
        Coordinate coordinate2 = BlockUtil.convertWorldCoordinate2Coordinate(regionInfo, block2.getWorldCoordinate());
        Shape shape1 = structureMap.get(block1.getBlockInfo().getCode()).getShape();
        Shape shape2 = structureMap.get(block2.getBlockInfo().getCode()).getShape();
        return BlockUtil.detectPlanarCollision(coordinate1, coordinate2, shape1, shape2);
    }

    private boolean detectZCollision(Block block1, Block block2) {
        if (block1.getWorldCoordinate().getRegionNo() != block2.getWorldCoordinate().getRegionNo()) {
            return false;
        }
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        if (!structureMap.containsKey(block1.getBlockInfo().getCode())
                || !structureMap.containsKey(block2.getBlockInfo().getCode())) {
            return false;
        }
        return block1.getWorldCoordinate().getCoordinate().getZ()
                .add(structureMap.get(block1.getBlockInfo().getCode()).getShape().getRadius().getZ())
                .compareTo(block2.getWorldCoordinate().getCoordinate().getZ()) > 0
                && block2.getWorldCoordinate().getCoordinate().getZ()
                .add(structureMap.get(block2.getBlockInfo().getCode()).getShape().getRadius().getZ())
                .compareTo(block1.getWorldCoordinate().getCoordinate().getZ()) > 0;
    }

    @Override
    public boolean detectLineCollision(GameWorld world, WorldCoordinate from, Block block1, Block block2, boolean correctBlock1) {
        Region region = world.getRegionMap().get(from.getRegionNo());
        if (block1.getWorldCoordinate().getRegionNo() != region.getRegionNo()
                || block2.getWorldCoordinate().getRegionNo() != region.getRegionNo()) {
            return false;
        }
        Coordinate coordinate0 = BlockUtil.convertWorldCoordinate2Coordinate(region, from);
        Coordinate coordinate1 = BlockUtil.convertWorldCoordinate2Coordinate(region, block1.getWorldCoordinate());
        Coordinate coordinate2 = BlockUtil.convertWorldCoordinate2Coordinate(region, block2.getWorldCoordinate());
        Coordinate coordinate3 = BlockUtil.findClosestPoint(coordinate0, coordinate1, coordinate2);
        Block block3 = new Block(block1);
        WorldCoordinate worldCoordinate3 = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
                block1.getWorldCoordinate(),
                BlockUtil.calculateAngle(coordinate1, coordinate3), BlockUtil.calculateDistance(coordinate1, coordinate3));
        BlockUtil.copyWorldCoordinate(worldCoordinate3, block3.getWorldCoordinate());
        if (!detectCollision(world, block3, block2)) {
            return false;
        }
        if (correctBlock1) {
            BlockUtil.copyWorldCoordinate(worldCoordinate3, block1.getWorldCoordinate());
        }
        return true;
    }
}
