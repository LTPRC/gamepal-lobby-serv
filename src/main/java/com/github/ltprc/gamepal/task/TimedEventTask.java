package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.manager.*;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;
import com.github.ltprc.gamepal.model.map.Scene;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.PlayerInfoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class TimedEventTask {

    @Autowired
    private WorldService worldService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private BuffManager buffManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private FarmManager farmManager;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private InteractionManager interactionManager;

    @Autowired
    private SceneManager sceneManager;

    @Scheduled(fixedRate = 20)
    public void executeByHalfFrame() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            // NPC movements
            npcManager.updateNpcBrains(world);
        }
    }

    @Scheduled(fixedRate = 40)
    public void executeByFrame() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();

            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, Block> creatureMap = world.getCreatureMap();
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();

            Map<Integer, Set<IntegerCoordinate>> preSelectedSceneCoordinates = new HashMap<>();
            onlineMap.keySet().stream()
                    .filter(creatureMap::containsKey)
                    .filter(id -> playerInfoMap.get(id).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(id -> {
                        Block player = creatureMap.get(id);
                        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
                        // Pre-select scenes for updating events
                        BlockUtil.preSelectSceneCoordinates(
                                region, player.getWorldCoordinate(), player.getWorldCoordinate())
                                .forEach(sceneCoordinate -> {
                                    if (!region.getScenes().containsKey(sceneCoordinate)) {
                                        sceneManager.fillScene(world, region, sceneCoordinate);
                                    }
                                    preSelectedSceneCoordinates.putIfAbsent(region.getRegionNo(), new HashSet<>());
                                    preSelectedSceneCoordinates.get(region.getRegionNo()).add(sceneCoordinate);
                                });
                        // Update buff time
                        buffManager.updateBuffTime(world, id);
                    });

            preSelectedSceneCoordinates.forEach((regionNo, sceneCoordinates) -> {
                Region region = world.getRegionMap().get(regionNo);
                sceneCoordinates.forEach(sceneCoordinate -> {
                    Scene scene = region.getScenes().get(sceneCoordinate);
                    scene.getBlocks().values().forEach(block -> {
                        // Update events
                        eventManager.updateEvent(world, block);
                    });
                });
            });

            onlineMap.keySet().stream()
                    .filter(id -> playerService.validateActiveness(world, id))
                    .forEach(id -> {
                        buffManager.changeBuff(world, id);

                        // Change maxSpeed
                        movementManager.updateCreatureMaxSpeed(world, id);

                        PlayerInfo playerInfo = playerInfoMap.get(id);
                        if (playerService.validateActiveness(world, id)
                                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] == 0) {
                            Block player = creatureMap.get(id);
                            MovementInfo movementInfo = player.getMovementInfo();
                            double randomNumber = Math.random();;

                            // Change hp
                            int changedHp = 0;
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLEEDING] != 0) {
                                changedHp--;
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_RECOVERING] != 0) {
                                changedHp++;
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] != 0) {
                                changedHp--;
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] != 0) {
                                changedHp--;
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DROWNING] != 0) {
                                changedHp -= 10;
                            }
                            if (changedHp != 0) {
                                eventManager.changeHp(world, player, changedHp, false);
                            }

                            // Change vp
                            int newVp = 10;
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_SICK] != 0) {
                                newVp -= 5;
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_FRACTURED] != 0) {
                                newVp -= 5;
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] != 0) {
                                if (movementInfo.getSpeed().getX().doubleValue() > 0
                                        || movementInfo.getSpeed().getY().doubleValue() > 0
                                        || movementInfo.getSpeed().getZ().doubleValue() > 0) {
                                    newVp -= 15;
                                }
                            } else {
                                if (Math.pow(movementInfo.getSpeed().getX().doubleValue(), 2)
                                        + Math.pow(movementInfo.getSpeed().getY().doubleValue(), 2)
                                        + Math.pow(movementInfo.getSpeed().getZ().doubleValue(), 2)
                                        > Math.pow(movementInfo.getMaxSpeed().doubleValue() / 2, 2)) {
                                    newVp -= 15;
                                }
                            }
                            playerService.changeVp(id, newVp, false);

                            // Change hunger
                            if (playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN) {
                                randomNumber = Math.random();
                                if (Math.abs(movementInfo.getSpeed().getX().doubleValue()) > 0
                                        || Math.abs(movementInfo.getSpeed().getY().doubleValue()) > 0
                                        || Math.abs(movementInfo.getSpeed().getZ().doubleValue()) > 0) {
                                    randomNumber *= 10;
                                }
                                if (randomNumber < 1000D / (7 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                                    playerService.changeHunger(id, -1, false);
                                }
                            }

                            // Change thirst
                            if (playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN) {
                                randomNumber = Math.random();
                                if (Math.abs(movementInfo.getSpeed().getX().doubleValue()) > 0
                                        || Math.abs(movementInfo.getSpeed().getY().doubleValue()) > 0
                                        || Math.abs(movementInfo.getSpeed().getZ().doubleValue()) > 0) {
                                    randomNumber *= 10;
                                }
                                if (randomNumber < 1000D / (3 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                                    playerService.changeThirst(id, -1, false);
                                }
                            }

                            // Change precision
                            playerService.changePrecision(id, 50 - 100
                                    * (int) (Math.sqrt(Math.pow(movementInfo.getSpeed().getX().doubleValue(), 2)
                                    + Math.pow(movementInfo.getSpeed().getY().doubleValue(), 2)
                                    + Math.pow(movementInfo.getSpeed().getZ().doubleValue(), 2))
                                    / movementInfo.getMaxSpeed().doubleValue()), false);

                            // Change view radius
                            PlayerInfoUtil.updatePerceptionInfo(playerInfo.getPerceptionInfo(), world.getWorldTime());
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLIND] != 0) {
                                playerInfo.getPerceptionInfo().setDistinctVisionRadius(
                                        playerInfo.getPerceptionInfo().getDistinctVisionRadius()
                                                .divide(BigDecimal.TEN, RoundingMode.HALF_UP));
                                playerInfo.getPerceptionInfo().setIndistinctVisionRadius(
                                        playerInfo.getPerceptionInfo().getIndistinctVisionRadius()
                                                .divide(BigDecimal.TEN, RoundingMode.HALF_UP));
                            }

                            // Change skill remaining time
                            for (int i = 0; i < playerInfo.getSkills().size(); i++) {
                                if (null != playerInfo.getSkills().get(i) && playerInfo.getSkills().get(i).getFrame() > 0) {
                                    playerInfo.getSkills().get(i).setFrame(playerInfo.getSkills().get(i).getFrame() - 1);
                                }
                            }
                            if (movementInfo.getFloorCode() == BlockConstants.BLOCK_CODE_WATER_DEEP) {
                                playerInfo.getSkills().get(0).setFrame(playerInfo.getSkills().get(0).getFrameMax());
                            }

                            // Update possible interacted blocks
                            interactionManager.searchInteraction(world, id);

                            // Add decorating effects
                            Map<Integer, Region> regionMap = world.getRegionMap();
                            Region region = regionMap.get(player.getWorldCoordinate().getRegionNo());
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DIVING] != 0) {
                                if (Math.random() < 0.01D) {
                                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_BUBBLE, id,
                                            BlockUtil.locateCoordinateWithDirectionAndDistance(region,
                                                    player.getWorldCoordinate(), BigDecimal.valueOf(Math.random() * 360),
                                                    BigDecimal.valueOf(Math.random()
                                                            * BlockConstants.BUBBLE_THROW_RADIUS.doubleValue())));
                                }
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DROWNING] != 0) {
                                if (Math.random() < 0.5D) {
                                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_BUBBLE, id,
                                            BlockUtil.locateCoordinateWithDirectionAndDistance(region,
                                                    player.getWorldCoordinate(), BigDecimal.valueOf(Math.random() * 360),
                                                    BigDecimal.valueOf(Math.random()
                                                            * BlockConstants.BUBBLE_THROW_RADIUS.doubleValue())));
                                }
                            }
                        }

                        if (creatureMap.containsKey(id)) {
                            buffManager.changeBuff(world, id);
                        }
                    });

            // Update farms
            farmManager.updateFarmStatus(world);
        }
    }

    @Scheduled(fixedRate = 100)
    public void executeBy100ms() {
        // Update worldTime
        worldService.getWorldMap().forEach((key, value) -> worldService.updateWorldTime(value,
                GamePalConstants.UPDATED_WORLD_TIME_PER_SECOND / 10));
    }

    @Scheduled(fixedRate = 1000)
    public void executeBy1s() {
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, Block> creatureMap = world.getCreatureMap();
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
            onlineMap.keySet().stream()
                    .filter(id -> playerService.validateActiveness(world, id))
                    .forEach(id -> {
                        Block player = creatureMap.get(id);
                        // Add footstep
                        if (Math.pow(player.getMovementInfo().getSpeed().getX().doubleValue(), 2)
                                + Math.pow(player.getMovementInfo().getSpeed().getY().doubleValue(), 2)
                                + Math.pow(player.getMovementInfo().getSpeed().getZ().doubleValue(), 2)
                                > Math.pow(player.getMovementInfo().getMaxSpeed().doubleValue() / 2, 2)) {
                            eventManager.addEvent(world, BlockConstants.BLOCK_CODE_NOISE, id,
                                    player.getWorldCoordinate());
                        }
                    });
            // Check timeout
            long timestamp = Instant.now().getEpochSecond();
//            onlineMap.forEach((blockInfo, oldTimestamp) -> {
//                long timeThreshold = BlockConstants.BLOCK_TYPE_TIMEOUT_MAP.getOrDefault(blockInfo.getType(), 0L);
//                if (timestamp - oldTimestamp > timeThreshold) {
//                    if (blockInfo.getType() == BlockConstants.BLOCK_TYPE_PLAYER
//                            && creatureMap.containsKey(blockInfo.getId())
//                            && playerInfoMap.get(blockInfo.getId()).getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN) {
//                        // NPC is exempted 24/10/20
//                        userService.logoff(blockInfo.getId(), "", false);
//                    }
//                    if (world.getBlockMap().containsKey(blockInfo.getId())) {
//                        sceneManager.removeBlock(world, world.getBlockMap().get(blockInfo.getId()), false);
//                    }
//                }
//            });
            onlineMap.forEach((id, oldTimestamp) -> {
                if (timestamp - oldTimestamp > GamePalConstants.PLAYER_LOGOFF_THRESHOLD_IN_SECOND
                        && (playerInfoMap.get(id).getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN)) {
                    // NPC is exempted 25/02/01
                    userService.logoff(id, "", false);
                }
            });
        }
    }
}
