package com.github.ltprc.gamepal.task;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.*;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
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
    private SceneManager sceneManager;

    @Autowired
    private FarmManager farmManager;

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

            // Update events
            world.getBlockMap().values().forEach(block -> eventManager.updateEvent(world, block));

            Map<String, Long> onlineMap = world.getOnlineMap();
            Map<String, Block> creatureMap = world.getCreatureMap();
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();

            onlineMap.keySet().stream()
//                    .filter(blockInfo -> blockInfo.getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                    .filter(creatureMap::containsKey)
                    .filter(id -> playerInfoMap.get(id).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(id -> {
                        buffManager.updateBuffTime(world, id);
                        buffManager.changeBuff(world, id);

                        PlayerInfo playerInfo = playerInfoMap.get(id);
                        if (playerService.validateActiveness(world, id)
                                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] == 0) {
                            Block player = creatureMap.get(id);
                            MovementInfo movementInfo = player.getMovementInfo();
                            double randomNumber;

                            // Change hp
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLEEDING] != 0) {
                                eventManager.changeHp(world, player, -1, false);
                            }
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_RECOVERING] != 0) {
                                eventManager.changeHp(world, player, 1, false);
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
                                        || movementInfo.getSpeed().getY().doubleValue() > 0) {
                                    newVp -= 15;
                                }
                            } else {
                                if (Math.pow(movementInfo.getSpeed().getX().doubleValue(), 2)
                                        + Math.pow(movementInfo.getSpeed().getY().doubleValue(), 2)
                                        > Math.pow(movementInfo.getMaxSpeed().doubleValue() / 2, 2)) {
                                    newVp -= 15;
                                }
                            }
                            playerService.changeVp(id, newVp, false);

                            // Change hunger
                            randomNumber = Math.random();
                            if (Math.abs(movementInfo.getSpeed().getX().doubleValue()) > 0
                                    || Math.abs(movementInfo.getSpeed().getY().doubleValue()) > 0) {
                                randomNumber *= 10;
                            }
                            if (randomNumber < 1000D / (7 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                                playerService.changeHunger(id, -1, false);
                                if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] != 0) {
                                    eventManager.changeHp(world, player, -1, false);
                                }
                            }

                            // Change thirst
                            randomNumber = Math.random();
                            if (Math.abs(movementInfo.getSpeed().getX().doubleValue()) > 0
                                    || Math.abs(movementInfo.getSpeed().getY().doubleValue()) > 0) {
                                randomNumber *= 10;
                            }
                            if (randomNumber < 1000D / (3 * 24 * 60 * GamePalConstants.FRAME_PER_SECOND)) {
                                playerService.changeThirst(id, -1, false);
                                if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] != 0) {
                                    eventManager.changeHp(world, player, -1, false);
                                }
                            }

                            // Change precision
                            playerService.changePrecision(id, 50 - 100
                                    * (int) (Math.sqrt(Math.pow(movementInfo.getSpeed().getX().doubleValue(), 2)
                                    + Math.pow(movementInfo.getSpeed().getY().doubleValue(), 2))
                                    / movementInfo.getMaxSpeed().doubleValue()), false);

                            // Change view radius
                            BlockUtil.updatePerceptionInfo(playerInfo.getPerceptionInfo(), world.getWorldTime());
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

                            // Check floorCode
                            BlockUtil.updateMaxSpeed(movementInfo);
                        }

                        buffManager.changeBuff(world, id);
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
