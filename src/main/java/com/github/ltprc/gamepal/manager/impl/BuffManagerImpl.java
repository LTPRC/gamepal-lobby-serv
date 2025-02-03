package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Region;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;


@Component
public class BuffManagerImpl implements BuffManager {

    private static final Log logger = LogFactory.getLog(BuffManagerImpl.class);

    @Autowired
    private PlayerService playerService;

    @Autowired
    private EventManager eventManager;

    @Override
    public void updateBuffTime(GameWorld world, String userCode) {
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
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(player.getWorldCoordinate().getRegionNo());
        for (int i = 0; i < BuffConstants.BUFF_CODE_LENGTH; i++) {
            if (playerInfo.getBuff()[i] <= 0) {
                continue;
            }
            playerInfo.getBuff()[i] = playerInfo.getBuff()[i] - 1;
            if (i == BuffConstants.BUFF_CODE_DEAD) {
                if (playerInfo.getBuff()[i] == 0) {
                    playerService.generateNotificationMessage(userCode, "复活成功。");
                    playerService.revivePlayer(userCode);
                } else if (playerInfo.getBuff()[i] % GamePalConstants.FRAME_PER_SECOND == 0) {
                    playerService.generateNotificationMessage(userCode, "距离复活还有"
                            + playerInfo.getBuff()[i] / GamePalConstants.FRAME_PER_SECOND + "秒。");
                }
            } else if (i == BuffConstants.BUFF_CODE_KNOCKED) {
                if (playerInfo.getBuff()[i] == 0) {
                    playerService.generateNotificationMessage(userCode, "濒死结束。");
                } else {
                    WorldCoordinate bleedSevereWc = new WorldCoordinate(player.getWorldCoordinate());
                    bleedSevereWc.getCoordinate().setY(bleedSevereWc.getCoordinate().getY().add(BigDecimal.valueOf(0.7)));
                    BlockUtil.fixWorldCoordinate(region, bleedSevereWc);
                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_BLEED_SEVERE, userCode, bleedSevereWc);
                    if (playerInfo.getBuff()[i] % GamePalConstants.FRAME_PER_SECOND == 0) {
                        eventManager.addEvent(world, BlockConstants.BLOCK_CODE_BLEED, userCode, player.getWorldCoordinate());
                        playerService.generateNotificationMessage(userCode, "距离濒死结束还有"
                                + playerInfo.getBuff()[i] / GamePalConstants.FRAME_PER_SECOND + "秒。");
                    }
                }
            }
        }
    }

    /**
     * After activating buff
     * @param world
     * @param userCode
     */
    @Override
    public void changeBuff(GameWorld world, String userCode) {
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

        if (player.getBlockInfo().getHp().get() <= 0
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] == 0
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] == 0) {
            playerService.killPlayer(userCode);
        }

        if (playerInfo.getHunger() < playerInfo.getHungerMax() / 10
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] == 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] = -1;
        } else if (playerInfo.getHunger() >= playerInfo.getHungerMax() / 10
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] != 0){
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] = 0;
        }

        if (playerInfo.getThirst() < playerInfo.getThirstMax() / 10
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] == 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] = -1;
        } else if (playerInfo.getThirst() >= playerInfo.getThirstMax() / 10
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] != 0){
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] = 0;
        }

        if (playerInfo.getVp() == 0
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] == 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] = -1;
        } else if (playerInfo.getVp() > 0
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] != 0){
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] = 0;
        }

        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        if (bagInfo.getCapacity().compareTo(bagInfo.getCapacityMax()) > 0
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_OVERWEIGHTED] == 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_OVERWEIGHTED] = -1;
        } else if (bagInfo.getCapacity().compareTo(bagInfo.getCapacityMax()) <= 0
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_OVERWEIGHTED] != 0){
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_OVERWEIGHTED] = 0;
        }
    }

    @Override
    public void resetBuff(PlayerInfo playerInfo) {
        validateBuffArray(playerInfo);
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_STUNNED] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLEEDING] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_SICK] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_FRACTURED] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLIND] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLOCKED] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_HAPPY] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_SAD] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_RECOVERING] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_OVERWEIGHTED] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] = 0;
    }

    @Override
    public void initializeBuff(PlayerInfo playerInfo) {
        validateBuffArray(playerInfo);
        resetBuff(playerInfo);
        if (playerInfo.getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_ONE_HIT] = -1;
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_TROPHY] = -1;
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_REALISTIC] = -1;
        } else {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_REVIVED] = -1; // Game testing 25/01/21
        }
    }

    private void validateBuffArray(PlayerInfo playerInfo) {
        if (null == playerInfo.getBuff()) {
            playerInfo.setBuff(new int[BuffConstants.BUFF_CODE_LENGTH]);
        }
        if (playerInfo.getBuff().length < BuffConstants.BUFF_CODE_LENGTH) {
            int[] newBuff = new int[BuffConstants.BUFF_CODE_LENGTH];
            System.arraycopy(playerInfo.getBuff(), 0, newBuff, 0, playerInfo.getBuff().length);
            playerInfo.setBuff(newBuff);
        }
    }
}
