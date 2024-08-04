package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldEvent;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;


@Component
public class BuffManagerImpl implements BuffManager {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private NpcManager npcManager;

    @Override
    public void updateBuffTime(GameWorld world, String userCode) {
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        for (int i = 0; i < GamePalConstants.BUFF_CODE_LENGTH; i++) {
            if (playerInfo.getBuff()[i] <= 0) {
                continue;
            }
            playerInfo.getBuff()[i] = playerInfo.getBuff()[i] - 1;
            if (i == GamePalConstants.BUFF_CODE_DEAD) {
                if (playerInfo.getBuff()[i] == 0) {
                    playerService.generateNotificationMessage(userCode, "复活成功。");
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
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);

        // TODO revise
        if (playerInfo.getHp() <= 0 && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] = GamePalConstants.BUFF_DEFAULT_FRAME_DEAD;
            playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            playerService.changeVp(userCode, 0, true);
            playerService.changeHunger(userCode, 0, true);
            playerService.changeThirst(userCode, 0, true);
            // Wipe all other buff and skill remaining time
            playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] = GamePalConstants.BUFF_DEFAULT_FRAME_DEAD;
            for (int i = 0; i < playerInfo.getSkill().length; i++) {
                playerInfo.getSkill()[i].setFrame(playerInfo.getSkill()[i].getFrameMax());
            }
            if (playerInfo.getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN) {
                world.getOnlineMap().remove(userCode);
                npcManager.resetNpcBrainQueues(userCode);
            }
            WorldEvent worldEvent = BlockUtil.createWorldEvent(userCode, GamePalConstants.EVENT_CODE_DISTURB,
                    playerInfo);
            world.getEventQueue().add(worldEvent);
        } else if (playerInfo.getHp() > 0 && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] != 0) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] = 0;
            playerService.changeHp(userCode, playerInfo.getHpMax(), true);
            playerService.changeVp(userCode, playerInfo.getVpMax(), true);
            playerService.changeHunger(userCode, playerInfo.getHungerMax(), true);
            playerService.changeThirst(userCode, playerInfo.getThirstMax(), true);
            WorldEvent worldEvent = BlockUtil.createWorldEvent(playerInfo.getId(),
                    GamePalConstants.EVENT_CODE_SACRIFICE, playerInfo);
            world.getEventQueue().add(worldEvent);
        }

        if (playerInfoMap.get(userCode).getHunger() < playerInfoMap.get(userCode).getHungerMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] == 0) {
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] = -1;
        } else if (playerInfoMap.get(userCode).getHunger() >= playerInfoMap.get(userCode).getHungerMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] != 0){
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] = 0;
        }

        if (playerInfoMap.get(userCode).getThirst() < playerInfoMap.get(userCode).getThirstMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] == 0) {
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] = -1;
        } else if (playerInfoMap.get(userCode).getThirst() >= playerInfoMap.get(userCode).getThirstMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] != 0){
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] = 0;
        }

        if (playerInfoMap.get(userCode).getVp() < playerInfoMap.get(userCode).getVpMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] == 0) {
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] = -1;
        } else if (playerInfoMap.get(userCode).getVp() >= playerInfoMap.get(userCode).getVpMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] != 0){
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] = 0;
        }
    }
}
