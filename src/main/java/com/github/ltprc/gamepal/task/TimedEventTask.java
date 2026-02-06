package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.FarmManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.PlayerInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

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
    private SceneManager sceneManager;

    @Scheduled(fixedRate = 40)
    public void executeByFrame() {
        long timestamp = System.currentTimeMillis();
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();

            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, Block> creatureMap = world.getCreatureMap();
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();

            onlineMap.keySet().stream()
                    .filter(creatureMap::containsKey)
                    .filter(id -> playerInfoMap.get(id).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(id -> buffManager.updateBuffTime(world, id));

            world.getBlockMap().values()
                    .forEach(block -> {
                        eventManager.updateEvent(world, block, timestamp);
                        movementManager.settleGravityAcceleration(world, block);
                        movementManager.applyFriction(block);
                        movementManager.settleSpeed(world, block);
                    });
            world.getCreatureMap().values()
                    .forEach(player -> {
                        movementManager.settleGravityAcceleration(world, player);
                        movementManager.applyFriction(player);
                        movementManager.settleSpeed(world, player);
                    });

            onlineMap.keySet().stream()
                    .filter(id -> playerService.validateActiveness(world, id))
                    .forEach(id -> {
                        buffManager.checkBuff(world, id);

                        // Change maxPlanarSpeed
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
                            int newVp = 1;
                            if (!movementInfo.getSpeed().getX().equals(BigDecimal.ZERO)
                                    || !movementInfo.getSpeed().getY().equals(BigDecimal.ZERO)) {
                                newVp = -1;
                            }
                            playerService.changeVp(id, newVp, false);

                            // Change hunger
                            if (playerInfo.getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN) {
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
                            if (playerInfo.getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN) {
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
                            playerService.changePrecision(id, 50
                                    - 100 * (int) (Math.sqrt(Math.pow(movementInfo.getSpeed().getX().doubleValue(), 2)
                                    + Math.pow(movementInfo.getSpeed().getY().doubleValue(), 2))
                                    / movementInfo.getMaxPlanarSpeed().doubleValue())
                                    - 100 * (int) (Math.abs(movementInfo.getSpeed().getZ().doubleValue())
                                    / movementInfo.getMaxVerticalSpeed().doubleValue()), false);

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
                                // TODO Better deep-water experience
                                for (int i = 0; i < playerInfo.getSkills().size(); i++) {
                                    playerInfo.getSkills().get(i).setFrame(playerInfo.getSkills().get(i).getFrameMax());
                                }
                            }

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

                            // Check relation with boss
                            if (playerInfo.getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN
                                    && StringUtils.isNotBlank(playerInfo.getBossId())
                                    && playerService.getRelationMapByUserCode(id)
                                    .get(playerInfo.getBossId()) <= CreatureConstants.RELATION_MIN) {
                                playerService.setMember(playerInfo.getBossId(), id, "", true);
                            }
                        }

                        if (creatureMap.containsKey(id)) {
                            buffManager.checkBuff(world, id);
                        }
                    });

            // Update farms
            farmManager.updateFarmStatus(world);
        }

        // Update NPC
        for (Map.Entry<String, GameWorld> entry : worldService.getWorldMap().entrySet()) {
            GameWorld world = entry.getValue();
            npcManager.updateNpcBrains(world);
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
        long timestamp = System.currentTimeMillis();
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
                                > Math.pow(player.getMovementInfo().getMaxPlanarSpeed().doubleValue() / 2, 2)) {
                            eventManager.addEvent(world, BlockConstants.BLOCK_CODE_NOISE, id,
                                    player.getWorldCoordinate());
                        }
                    });
            // Check timeout
//            onlineMap.forEach((blockInfo, oldTimestamp) -> {
//                long timeThreshold = BlockConstants.BLOCK_TYPE_TIMEOUT_MAP.getOrDefault(blockInfo.getType(), 0L);
//                if (timestamp - oldTimestamp > timeThreshold) {
//                    if (blockInfo.getType() == BlockConstants.BLOCK_TYPE_PLAYER
//                            && creatureMap.containsKey(blockInfo.getId())
//                            && playerInfoMap.get(blockInfo.getId()).getPlayerType() != GamePalConstants.PLAYER_TYPE_HUMAN) {
//                        // NPC is exempted 24/10/20
//                        userService.logoff(blockInfo.getId(), "", false);
//                    }
//                    if (world.getBlockMap().containsKey(blockInfo.getId())) {
//                        sceneManager.removeBlock(world, world.getBlockMap().get(blockInfo.getId()), false);
//                    }
//                }
//            });
            onlineMap.forEach((id, oldTimestamp) -> {
                if (timestamp - oldTimestamp > GamePalConstants.PLAYER_LOGOFF_THRESHOLD_IN_MILLISECOND
                        && (playerInfoMap.get(id).getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN)) {
                    // NPC is exempted 25/02/01
                    userService.logoff(id, "", false);
                }
            });
        }
    }
}
