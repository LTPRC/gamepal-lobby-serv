package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.model.creature.PerceptionInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.util.NameUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Random;

@Component
public class CreatureFactory {

    public static PlayerInfo createCreatureInstance(final int playerType) {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerType(playerType);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        playerInfo.setHpMax(1000);
        playerInfo.setHp(playerInfo.getHpMax() / 2);
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
        playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
        playerInfo.setPerceptionInfo(new PerceptionInfo());
        if (CreatureConstants.CREATURE_TYPE_ANIMAL != playerInfo.getCreatureType()) {
            SkillUtil.updateHumanSkills(playerInfo);
            randomlyPersonalizePlayerInfo(playerInfo, NameUtil.generateGender());
        } else {
            SkillUtil.updateAnimalSkills(playerInfo);
            randomlyPersonalizeAnimalInfo(playerInfo, NameUtil.generateGender());
        }
        playerInfo.setRespawnPoint(GamePalConstants.DEFAULT_BIRTHPLACE);
        return playerInfo;
    }

    public static void randomlyPersonalizePlayerInfo(PlayerInfo playerInfo, int gender) {
        Random random = new Random();
        String origin = NameUtil.generateOrigin();
        String[] names = NameUtil.generateNames(origin, gender);
        playerInfo.setAvatar(String.valueOf(random.nextInt(CreatureConstants.AVATARS_LENGTH)));
        playerInfo.setGender(gender);
        playerInfo.setFirstName(names[0]);
        playerInfo.setLastName(names[1]);
        playerInfo.setNickname(names[2]);
        playerInfo.setNameColor("#990000");
        playerInfo.setSkinColor(NameUtil.generateSkinColorByOrigin(origin));
        playerInfo.setHairstyle(NameUtil.generateHairStyleByGender(gender));
        playerInfo.setHairColor(random.nextInt(3) + 1);
        playerInfo.setEyes(random.nextInt(CreatureConstants.EYES_LENGTH) + 1);
        playerInfo.setFaceCoefs(Arrays.stream(new int[CreatureConstants.FACE_COEFS_LENGTH])
                .map(faceCoef -> random.nextInt(100)).toArray());
    }

    public static void randomlyPersonalizeAnimalInfo(PlayerInfo animalInfo, int gender) {
        animalInfo.setGender(gender);
        animalInfo.setSkinColor(generateAnimalSkinColor());
    }

    private static int generateAnimalSkinColor() {
        Random random = new Random();
        return random.nextInt(14) + 1;
    }

//    public static Block createCreatureInstance(final int playerType) {
//        WorldCoordinate worldCoordinate = new WorldCoordinate();
//        BlockUtil.copyWorldCoordinate(GamePalConstants.DEFAULT_BIRTHPLACE, worldCoordinate);
//
//        BlockInfo blockInfo = BlockUtil.generateBlockInfo(BlockConstants.BLOCK_TYPE_PLAYER);
//
//        MovementInfo movementInfo = new MovementInfo();
//        movementInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
//        movementInfo.setFaceDirection(BlockConstants.FACE_DIRECTION_DEFAULT);
//        BlockUtil.calculateMaxSpeed(movementInfo);
//        movementInfo.setAcceleration(BlockConstants.ACCELERATION_DEFAULT);
//
//        PlayerInfo playerInfo = new PlayerInfo();
//        playerInfo.setPlayerType(playerType);
//        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
//        playerInfo.setHpMax(1000);
//        playerInfo.setHp(playerInfo.getHpMax() / 2);
//        playerInfo.setVpMax(1000);
//        playerInfo.setVp(playerInfo.getVpMax() / 2);
//        playerInfo.setHungerMax(1000);
//        playerInfo.setHunger(playerInfo.getHungerMax() / 2);
//        playerInfo.setThirstMax(1000);
//        playerInfo.setThirst(playerInfo.getThirstMax() / 2);
//        playerInfo.setPrecisionMax(1000);
//        playerInfo.setPrecision(playerInfo.getPrecisionMax());
//        playerInfo.setLevel(1);
//        playerInfo.setExp(0);
//        SkillUtil.updateExpMax(playerInfo);
//        playerInfo.setMoney(1);
//        playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
//        playerInfo.setPerceptionInfo(new PerceptionInfo());
//        if (CreatureConstants.CREATURE_TYPE_ANIMAL != playerInfo.getCreatureType()) {
//            SkillUtil.updateHumanSkills(playerInfo);
//            randomlyPersonalizePlayerInfo(playerInfo, NameUtil.generateGender());
//        } else {
//            SkillUtil.updateAnimalSkills(playerInfo);
//            randomlyPersonalizeAnimalInfo(playerInfo, NameUtil.generateGender());
//        }
//        playerInfo.setRespawnPoint(GamePalConstants.DEFAULT_BIRTHPLACE);
//
//        Block player = new Block(worldCoordinate, blockInfo, movementInfo, playerInfo, null);
//        return player;
//    }
}
