package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PerceptionInfo;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.NameUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

@Component
public class PlayerInfoFactory {

    public PlayerInfo createPlayerInfoInstance() {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setStructure(new Structure(GamePalConstants.STRUCTURE_MATERIAL_FLESH,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Coordinate(GamePalConstants.PLAYER_RADIUS, GamePalConstants.PLAYER_RADIUS))));
        playerInfo.setType(GamePalConstants.BLOCK_TYPE_PLAYER);
        playerInfo.setPlayerType(GamePalConstants.PLAYER_TYPE_HUMAN);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        playerInfo.setRegionNo(1);
        playerInfo.setSceneCoordinate(new IntegerCoordinate(0, 0));
        playerInfo.setCoordinate(new Coordinate(new BigDecimal(5), new BigDecimal(5)));
        playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        playerInfo.setFaceDirection(BigDecimal.ZERO);
        randomlyPersonalizePlayerInfo(playerInfo);
        playerInfo.setMaxSpeed(BigDecimal.valueOf(0.1));
        playerInfo.setAcceleration(BigDecimal.valueOf(0.01));
        playerInfo.setHpMax(1000);
        playerInfo.setHp(playerInfo.getHpMax() / 2);
        playerInfo.setVpMax(1000);
        playerInfo.setVp(playerInfo.getVpMax() / 2);
        playerInfo.setHungerMax(1000);
        playerInfo.setHunger(playerInfo.getHungerMax() / 2);
        playerInfo.setThirstMax(1000);
        playerInfo.setThirst(playerInfo.getThirstMax() / 2);
        playerInfo.setLevel(1);
        playerInfo.setExp(0);
        playerInfo.setExpMax(100);
        playerInfo.setMoney(1);
        playerInfo.setCapacity(new BigDecimal(0));
        playerInfo.setCapacityMax(new BigDecimal(500));
        playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
        SkillUtil.updateSkills(playerInfo);
        playerInfo.setPerceptionInfo(new PerceptionInfo());
        BlockUtil.updateVisionRadius(playerInfo.getPerceptionInfo(), 0);
        return playerInfo;
    }

    public void randomlyPersonalizePlayerInfo(PlayerInfo playerInfo) {
        Random random = new Random();
        String origin = NameUtil.generateOrigin();
        String gender = NameUtil.generateGender();
        String[] names = NameUtil.generateNames(origin, gender);
        playerInfo.setAvatar(String.valueOf(random.nextInt(GamePalConstants.AVATARS_LENGTH)));
        playerInfo.setGender(gender);
        playerInfo.setFirstName(names[0]);
        playerInfo.setLastName(names[1]);
        playerInfo.setNickname(names[2]);
        playerInfo.setNameColor("#990000");
        playerInfo.setCreature("1");
        playerInfo.setSkinColor(NameUtil.generateSkinColorByOrigin(origin));
        playerInfo.setHairstyle(NameUtil.generateHairStyleByGender(gender));
        playerInfo.setHairColor(String.valueOf(random.nextInt(3) + 1));
        playerInfo.setEyes(String.valueOf(random.nextInt(GamePalConstants.EYES_LENGTH) + 1));
        playerInfo.setFaceCoefs(Arrays.stream(new int[GamePalConstants.FACE_COEFS_LENGTH])
                .map(faceCoef -> random.nextInt(100)).toArray());
    }
}
