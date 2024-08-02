package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.model.creature.CreatureInfo;
import com.github.ltprc.gamepal.model.creature.PerceptionInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.NameUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

@Component
public class CreatureFactory {

    public PlayerInfo createPlayerInfoInstance() {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setStructure(new Structure(GamePalConstants.STRUCTURE_MATERIAL_FLESH,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Coordinate(GamePalConstants.PLAYER_RADIUS, GamePalConstants.PLAYER_RADIUS))));
        playerInfo.setType(GamePalConstants.BLOCK_TYPE_PLAYER);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        playerInfo.setRegionNo(1);
        playerInfo.setSceneCoordinate(new IntegerCoordinate(0, 0));
        playerInfo.setCoordinate(new Coordinate(new BigDecimal(5), new BigDecimal(5)));
        playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        playerInfo.setFaceDirection(BigDecimal.ZERO);
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
//        playerInfo.setCapacity(new BigDecimal(0));
//        playerInfo.setCapacityMax(new BigDecimal(500));
        playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
        SkillUtil.updateHumanSkills(playerInfo);
        playerInfo.setPerceptionInfo(new PerceptionInfo());
        BlockUtil.updatePerceptionInfo(playerInfo.getPerceptionInfo(), 0);
        randomlyPersonalizePlayerInfo(playerInfo);
        return playerInfo;
    }

    public void randomlyPersonalizePlayerInfo(PlayerInfo playerInfo) {
        Random random = new Random();
        String origin = NameUtil.generateOrigin();
        int gender = NameUtil.generateGender();
        String[] names = NameUtil.generateNames(origin, gender);
        playerInfo.setAvatar(String.valueOf(random.nextInt(GamePalConstants.AVATARS_LENGTH)));
        playerInfo.setGender(gender);
        playerInfo.setFirstName(names[0]);
        playerInfo.setLastName(names[1]);
        playerInfo.setNickname(names[2]);
        playerInfo.setNameColor("#990000");
        playerInfo.setCreatureType(CreatureConstants.CREATURE_TYPE_HUMAN);
        playerInfo.setSkinColor(NameUtil.generateSkinColorByOrigin(origin));
        playerInfo.setHairstyle(NameUtil.generateHairStyleByGender(gender));
        playerInfo.setHairColor(random.nextInt(3) + 1);
        playerInfo.setEyes(random.nextInt(GamePalConstants.EYES_LENGTH) + 1);
        playerInfo.setFaceCoefs(Arrays.stream(new int[GamePalConstants.FACE_COEFS_LENGTH])
                .map(faceCoef -> random.nextInt(100)).toArray());
    }

    public PlayerInfo createAnimalInfoInstance() {
        PlayerInfo animalInfo = new PlayerInfo();
        animalInfo.setStructure(new Structure(GamePalConstants.STRUCTURE_MATERIAL_FLESH,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Coordinate(GamePalConstants.PLAYER_RADIUS, GamePalConstants.PLAYER_RADIUS))));
        animalInfo.setType(13);
        animalInfo.setRegionNo(1);
        animalInfo.setSceneCoordinate(new IntegerCoordinate(0, 0));
        animalInfo.setCoordinate(new Coordinate(new BigDecimal(5), new BigDecimal(5)));
        animalInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        animalInfo.setFaceDirection(BigDecimal.ZERO);
        animalInfo.setMaxSpeed(BigDecimal.valueOf(0.1));
        animalInfo.setAcceleration(BigDecimal.valueOf(0.01));
        animalInfo.setHpMax(1000);
        animalInfo.setHp(animalInfo.getHpMax() / 2);
        animalInfo.setVpMax(1000);
        animalInfo.setVp(animalInfo.getVpMax() / 2);
        animalInfo.setHungerMax(1000);
        animalInfo.setHunger(animalInfo.getHungerMax() / 2);
        animalInfo.setThirstMax(1000);
        animalInfo.setThirst(animalInfo.getThirstMax() / 2);
        SkillUtil.updateAnimalSkills(animalInfo);
        animalInfo.setPerceptionInfo(new PerceptionInfo());
        BlockUtil.updatePerceptionInfo(animalInfo.getPerceptionInfo(), 0);
        randomlyPersonalizeAnimalInfo(animalInfo);
        return animalInfo;
    }

    public void randomlyPersonalizeAnimalInfo(CreatureInfo animalInfo) {
        int gender = NameUtil.generateGender();
        animalInfo.setGender(gender);
        animalInfo.setCreatureType(CreatureConstants.CREATURE_TYPE_ANIMAL);
        animalInfo.setSkinColor(generateAnimalSkinColor());
    }

    private int generateAnimalSkinColor() {
        Random random = new Random();
        return random.nextInt(14) + 1;
    }

    public PlayerInfo createCreatureInstance(final int playerType) {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setStructure(new Structure(GamePalConstants.STRUCTURE_MATERIAL_FLESH,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Coordinate(GamePalConstants.PLAYER_RADIUS, GamePalConstants.PLAYER_RADIUS))));
        playerInfo.setType(GamePalConstants.BLOCK_TYPE_PLAYER);
        playerInfo.setPlayerType(playerType);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        playerInfo.setRegionNo(1);
        playerInfo.setSceneCoordinate(new IntegerCoordinate(0, 0));
        playerInfo.setCoordinate(new Coordinate(new BigDecimal(5), new BigDecimal(5)));
        playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        playerInfo.setFaceDirection(BigDecimal.ZERO);
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
//        playerInfo.setCapacity(new BigDecimal(0));
//        playerInfo.setCapacityMax(new BigDecimal(500));
        playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
        playerInfo.setPerceptionInfo(new PerceptionInfo());
        BlockUtil.updatePerceptionInfo(playerInfo.getPerceptionInfo(), 0);
        if (CreatureConstants.PLAYER_TYPE_ANIMAL != playerType) {
            SkillUtil.updateHumanSkills(playerInfo);
            randomlyPersonalizePlayerInfo(playerInfo);
        } else {
            SkillUtil.updateAnimalSkills(playerInfo);
            randomlyPersonalizeAnimalInfo(playerInfo);
        }
        return playerInfo;
    }
}
