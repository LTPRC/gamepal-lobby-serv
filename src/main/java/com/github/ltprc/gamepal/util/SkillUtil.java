package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.Skill;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class SkillUtil {

    private SkillUtil() {}

    public static void updateHumanSkills(PlayerInfo playerInfo) {
        List<Skill> skills = new ArrayList<>(SkillConstants.SKILL_LENGTH);
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_ATTACK,
                SkillConstants.SKILL_RANGE_MELEE, null));
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_KICK, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME * 2, SkillConstants.SKILL_TYPE_ATTACK,
                SkillConstants.SKILL_RANGE_MELEE, null));
        skills.add(new Skill(SkillConstants.SKILL_CODE_CURSE, SkillConstants.SKILL_MODE_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_DEFAULT, BigDecimal.ZERO, null));
        skills.add(new Skill(SkillConstants.SKILL_CODE_CHEER, SkillConstants.SKILL_MODE_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_DEFAULT, BigDecimal.ZERO, null));
        playerInfo.setSkills(skills);
    }

    public static void updateAnimalSkills(PlayerInfo playerInfo) {
        List<Skill> skills = new ArrayList<>(SkillConstants.SKILL_LENGTH);
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_SCRATCH, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_ATTACK,
                SkillConstants.SKILL_RANGE_MELEE, null));
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_KICK, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_ATTACK,
                SkillConstants.SKILL_RANGE_MELEE, null));
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_ATTACK,
                SkillConstants.SKILL_RANGE_MELEE, null));
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_ATTACK,
                SkillConstants.SKILL_RANGE_MELEE, null));
        switch (playerInfo.getSkinColor()) {
            case CreatureConstants.SKIN_COLOR_PAOFU:
            case CreatureConstants.SKIN_COLOR_CAT:
            case CreatureConstants.SKIN_COLOR_TIGER:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_SCRATCH, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        10, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
            case CreatureConstants.SKIN_COLOR_FROG:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        20, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
            case CreatureConstants.SKIN_COLOR_MONKEY:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        15, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
            case CreatureConstants.SKIN_COLOR_RACOON:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_SCRATCH, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        20, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
            case CreatureConstants.SKIN_COLOR_CHICKEN:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_STAB, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        25, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
            case CreatureConstants.SKIN_COLOR_BUFFALO:
            case CreatureConstants.SKIN_COLOR_SHEEP:
            case CreatureConstants.SKIN_COLOR_HORSE:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        30, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
            case CreatureConstants.SKIN_COLOR_FOX:
            case CreatureConstants.SKIN_COLOR_DOG:
            case CreatureConstants.SKIN_COLOR_WOLF:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_SCRATCH, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        15, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
            case CreatureConstants.SKIN_COLOR_POLAR_BEAR:
            case CreatureConstants.SKIN_COLOR_BOAR:
                skills.set(0, new Skill(SkillConstants.SKILL_CODE_MELEE_SCRATCH, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                        15, SkillConstants.SKILL_TYPE_ATTACK, SkillConstants.SKILL_RANGE_MELEE, null));
                break;
        }
        playerInfo.setSkills(skills);
    }

    public static boolean isSceneDetected(final Block player, final WorldCoordinate worldCoordinate,
                                          final int sceneScanRadius) {
        return worldCoordinate.getRegionNo() == player.getWorldCoordinate().getRegionNo()
                && isSceneDetected(player, worldCoordinate.getSceneCoordinate(), sceneScanRadius);
    }

    public static boolean isSceneDetected(final Block player, final IntegerCoordinate sceneCoordinate,
                                          final int sceneScanRadius) {
        IntegerCoordinate integerCoordinate = BlockUtil.getCoordinateRelation(
                player.getWorldCoordinate().getSceneCoordinate(), sceneCoordinate);
        return Math.abs(integerCoordinate.getX()) <= sceneScanRadius
                && Math.abs(integerCoordinate.getY()) <= sceneScanRadius;
    }

    public static void updateExpMax(PlayerInfo playerInfo) {
        if (playerInfo.getLevel() <= 0 || playerInfo.getLevel() >= 100) {
            playerInfo.setExpMax(0);
        } else {
            playerInfo.setExpMax(playerInfo.getLevel() * playerInfo.getLevel() + playerInfo.getLevel() + 10);
        }
    }

    public static boolean checkSkillTypeAttack(final PlayerInfo playerInfo) {
        return playerInfo.getSkills().stream()
                .anyMatch(skill -> skill.getSkillType() == SkillConstants.SKILL_TYPE_ATTACK);
    }

    /**
     * At present, we do not determine hp amount based on equipped tool
     * @param eventCode
     * @param targetType
     * @return changed hp amount
     */
    public static int calculateChangedHp(final int eventCode, final int targetType) {
        int hp = 0;
        if (targetType == BlockConstants.BLOCK_TYPE_NORMAL
                || targetType == BlockConstants.BLOCK_TYPE_EFFECT
                || targetType == BlockConstants.BLOCK_TYPE_DROP
                || targetType == BlockConstants.BLOCK_TYPE_TELEPORT
                || targetType == BlockConstants.BLOCK_TYPE_PLASMA) {
            // No hp effect
            return hp;
        }
        Random random = new Random();
        switch (eventCode) {
            case BlockConstants.BLOCK_CODE_HEAL:
                if (targetType == BlockConstants.BLOCK_TYPE_PLAYER) {
                    hp = 100;
                }
                break;
            case BlockConstants.BLOCK_CODE_MELEE_HIT:
                hp = -10 - random.nextInt(10);
                break;
            case BlockConstants.BLOCK_CODE_MELEE_KICK:
                hp = -10 - random.nextInt(20);
                break;
            case BlockConstants.BLOCK_CODE_MELEE_SCRATCH:
            case BlockConstants.BLOCK_CODE_WIRE_NETTING:
                hp = -20 - random.nextInt(60);
                break;
            case BlockConstants.BLOCK_CODE_MELEE_SMASH:
                hp = -50 - random.nextInt(100);
                break;
            case BlockConstants.BLOCK_CODE_MELEE_CLEAVE:
                hp = -75 - random.nextInt(50);
                if (targetType == BlockConstants.BLOCK_TYPE_TREE) {
                    hp *= 2;
                }
                break;
            case BlockConstants.BLOCK_CODE_MELEE_CHOP:
                hp = -75 - random.nextInt(50);
                if (targetType == BlockConstants.BLOCK_TYPE_TREE) {
                    hp *= 10;
                }
                break;
            case BlockConstants.BLOCK_CODE_MELEE_PICK:
                hp = -75 - random.nextInt(50);
                if (targetType == BlockConstants.BLOCK_TYPE_ROCK) {
                    hp *= 10;
                }
                break;
            case BlockConstants.BLOCK_CODE_MELEE_STAB:
                hp = -100 - random.nextInt(100);
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_HIT:
                hp = -110 - random.nextInt(20);
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_ARROW:
                hp = -250 - random.nextInt(200);
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_SLUG:
            case BlockConstants.BLOCK_CODE_SHOOT_MAGNUM:
                hp = -250 - random.nextInt(100);
                break;
            case BlockConstants.BLOCK_CODE_EXPLODE:
                hp = -600 - random.nextInt(400);
                break;
            case BlockConstants.BLOCK_CODE_FIRE:
                hp = -3;
                break;
            default:
                break;
        }
        return hp;
    }

    public static boolean blockCode2Build(int code) {
        switch (code) {
            case BlockConstants.BLOCK_CODE_WATER_SHALLOW:
            case BlockConstants.BLOCK_CODE_WATER_MEDIUM:
            case BlockConstants.BLOCK_CODE_WATER_DEEP:
                return false;
            default:
                return true;
        }
    }
}
