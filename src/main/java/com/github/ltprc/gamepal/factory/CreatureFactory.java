package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.model.creature.PerceptionInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.util.PlayerInfoUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Random;

@Component
public class CreatureFactory {

    private CreatureFactory() {}

    private static final Random random = new Random();

    public static PlayerInfo createCreatureInstance(final int playerType) {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerType(playerType);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
//        playerInfo.setHpMax(1000);
//        playerInfo.setHp(playerInfo.getHpMax() / 2);
        playerInfo.setVpMax(1000);
        playerInfo.setVp(playerInfo.getVpMax() / 2);
        playerInfo.setHungerMax(1000);
        playerInfo.setHunger(playerInfo.getHungerMax() / 2);
        playerInfo.setThirstMax(1000);
        playerInfo.setThirst(playerInfo.getThirstMax() / 2);
        playerInfo.setPrecisionMax(1000);
        playerInfo.setPrecision(playerInfo.getPrecisionMax());
        playerInfo.setLevel(1);
        playerInfo.setExp(0);
        SkillUtil.updateExpMax(playerInfo);
        playerInfo.setMoney(1);
        playerInfo.setBuff(new int[BuffConstants.BUFF_CODE_LENGTH]);
        playerInfo.setPerceptionInfo(new PerceptionInfo());
        if (CreatureConstants.CREATURE_TYPE_ANIMAL != playerInfo.getCreatureType()) {
            SkillUtil.updateHumanSkills(playerInfo);
            randomlyPersonalizePlayerInfo(playerInfo, PlayerInfoUtil.generateGender());
        } else {
            SkillUtil.updateAnimalSkills(playerInfo);
            randomlyPersonalizeAnimalInfo(playerInfo, PlayerInfoUtil.generateGender());
        }
        playerInfo.setRespawnPoint(GamePalConstants.DEFAULT_BIRTHPLACE);
        return playerInfo;
    }

    public static void randomlyPersonalizePlayerInfo(PlayerInfo playerInfo, int gender) {
        String origin = PlayerInfoUtil.generateOrigin();
        String[] names = PlayerInfoUtil.generateNames(origin, gender);
        playerInfo.setAvatar(String.valueOf(random.nextInt(CreatureConstants.AVATARS_LENGTH)));
        playerInfo.setGender(gender);
        playerInfo.setFirstName(names[0]);
        playerInfo.setLastName(names[1]);
        playerInfo.setNickname(names[2]);
        playerInfo.setNameColor(PlayerInfoUtil.generateNameColor());
        playerInfo.setSkinColor(PlayerInfoUtil.generateSkinColorByOrigin(origin));
        playerInfo.setBreastType(PlayerInfoUtil.generateBreastTypeByGender(gender));
        playerInfo.setAccessories(PlayerInfoUtil.generateAccessoriesByGender(gender));
        playerInfo.setHairstyle(PlayerInfoUtil.generateHairStyleByGender(gender));
        playerInfo.setHairColor(PlayerInfoUtil.generateHairColorByOrigin(origin));
        playerInfo.setEyes(random.nextInt(CreatureConstants.EYES_LENGTH));
        playerInfo.setNose(random.nextInt(CreatureConstants.NOSE_LENGTH));
        playerInfo.setMouth(random.nextInt(CreatureConstants.MOUTH_LENGTH));
        playerInfo.setTongue(random.nextInt(CreatureConstants.TONGUE_LENGTH));
        playerInfo.setFaceCoefs(Arrays.stream(new int[CreatureConstants.FACE_COEFS_LENGTH])
                .map(faceCoef -> random.nextInt(100)).toArray());
    }

    public static void randomlyPersonalizeAnimalInfo(PlayerInfo animalInfo, int gender) {
        animalInfo.setGender(gender);
        animalInfo.setSkinColor(generateAnimalSkinColor());
    }

    private static int generateAnimalSkinColor() {
        return random.nextInt(14) + 1;
    }
}
