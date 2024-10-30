package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class BuffManagerImpl implements BuffManager {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private NpcManager npcManager;

    @Override
    public void updateBuffTime(GameWorld world, String userCode) {
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        for (int i = 0; i < GamePalConstants.BUFF_CODE_LENGTH; i++) {
            if (playerInfo.getBuff()[i] <= 0) {
                continue;
            }
            playerInfo.getBuff()[i] = playerInfo.getBuff()[i] - 1;
            if (i == GamePalConstants.BUFF_CODE_DEAD) {
                if (playerInfo.getBuff()[i] == 0) {
                    playerService.generateNotificationMessage(userCode, "复活成功。");
                    playerService.revivePlayer(userCode);
                } else if (playerInfo.getBuff()[i] % GamePalConstants.FRAME_PER_SECOND == 0) {
                    playerService.generateNotificationMessage(userCode, "距离复活还有"
                            + playerInfo.getBuff()[i] / GamePalConstants.FRAME_PER_SECOND + "秒。");
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
        Block player = world.getCreatureMap().get(userCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);

//        if (player.getBlockInfo().getHp().get() <= 0 && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0) {
//            playerService.killPlayer(userCode);
//        }

        if (playerInfo.getHunger() < playerInfo.getHungerMax() / 10
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] == 0) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] = -1;
        } else if (playerInfo.getHunger() >= playerInfo.getHungerMax() / 10
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] != 0){
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] = 0;
        }

        if (playerInfo.getThirst() < playerInfo.getThirstMax() / 10
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] == 0) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] = -1;
        } else if (playerInfo.getThirst() >= playerInfo.getThirstMax() / 10
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] != 0){
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] = 0;
        }

        if (playerInfo.getVp() == 0
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] == 0) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] = -1;
        } else if (playerInfo.getVp() > 0
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] != 0){
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] = 0;
        }

        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        if (bagInfo.getCapacity().compareTo(bagInfo.getCapacityMax()) > 0
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_OVERWEIGHTED] == 0) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_OVERWEIGHTED] = -1;
        } else if (bagInfo.getCapacity().compareTo(bagInfo.getCapacityMax()) <= 0
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_OVERWEIGHTED] != 0){
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_OVERWEIGHTED] = 0;
        }
    }

    @Override
    public void resetBuff(PlayerInfo playerInfo) {
        validateBuffArray(playerInfo);
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_STUNNED] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLEEDING] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SICK] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLIND] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HAPPY] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_RECOVERING] = 0;
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_OVERWEIGHTED] = 0;
    }

    @Override
    public void initializeBuff(PlayerInfo playerInfo) {
        validateBuffArray(playerInfo);
        resetBuff(playerInfo);
        if (playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_REVIVED] = -1;
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_ANTI_TROPHY] = -1;
        } else if (playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_NPC) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_REALISTIC] = -1;
        } else {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_REALISTIC] = -1;
        }
    }

    private void validateBuffArray(PlayerInfo playerInfo) {
        if (null == playerInfo.getBuff()) {
            playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
        }
        if (playerInfo.getBuff().length < GamePalConstants.BUFF_CODE_LENGTH) {
            int[] newBuff = new int[GamePalConstants.BUFF_CODE_LENGTH];
            for (int i = 0; i < playerInfo.getBuff().length; i++) {
                newBuff[i] = playerInfo.getBuff()[i];
            }
            playerInfo.setBuff(newBuff);
        }
    }
}
