package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.Skill;
import com.github.ltprc.gamepal.model.item.Tool;
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

    @Deprecated
    public static void defineToolProps(Tool tool) {
        int toolType;
        int skillMode;
        int skillTime;
        switch (tool.getItemNo()) {
            case "t005":
            case "t006":
            case "t012":
            case "t013":
            case "t015":
            case "t113":
            case "t114":
            case "t115":
                toolType = SkillConstants.SKILL_CODE_MELEE_HIT;
                break;
            case "t010":
            case "t014":
            case "t102":
            case "t110":
            case "t112":
            case "t117":
            case "t204":
            case "t227":
                toolType = SkillConstants.SKILL_CODE_MELEE_CLEAVE;
                break;
            case "t004":
            case "t111":
                toolType = SkillConstants.SKILL_CODE_MELEE_SCRATCH;
                break;
            case "t001":
            case "t020":
            case "t101":
            case "t103":
            case "t109":
                toolType = SkillConstants.SKILL_CODE_MELEE_STAB;
                break;
            case "t007":
                toolType = SkillConstants.SKILL_CODE_SHOOT_HIT;
                break;
            case "t108":
            case "t203":
                toolType = SkillConstants.SKILL_CODE_SHOOT_ARROW;
                break;
            case "t000":
            case "t003":
            case "t016":
            case "t017":
            case "t018":
            case "t019":
            case "t100":
            case "t104":
            case "t105":
            case "t107":
            case "t116":
            case "t200":
            case "t206":
            case "t207":
            case "t208":
            case "t209":
            case "t210":
            case "t213":
            case "t214":
            case "t216":
            case "t217":
            case "t223":
            case "t224":
            case "t225":
            case "t228":
            case "t229":
            case "t230":
                toolType = SkillConstants.SKILL_CODE_SHOOT_GUN;
                break;
            case "t106":
            case "t201":
            case "t215":
            case "t220":
            case "t231":
                toolType = SkillConstants.SKILL_CODE_SHOOT_SHOTGUN;
                break;
            case "t002":
            case "t202":
            case "t205":
            case "t211":
            case "t212":
            case "t219":
            case "t222":
                toolType = SkillConstants.SKILL_CODE_SHOOT_MAGNUM;
                break;
            case "t221":
            case "t226":
                toolType = SkillConstants.SKILL_CODE_SHOOT_ROCKET;
                break;
            case "t218":
                toolType = SkillConstants.SKILL_CODE_SHOOT_FIRE;
                break;
            case "t008":
            case "t011":
                toolType = SkillConstants.SKILL_CODE_SHOOT_WATER;
                break;
            case "t009":
                toolType = SkillConstants.SKILL_CODE_CHEER;
                break;
            case "t021":
                toolType = SkillConstants.SKILL_CODE_LAY;
                break;
            case "t301":
            case "t302":
            case "t303":
            case "t304":
            case "t305":
            case "t306":
            case "t307":
            case "t308":
            case "t309":
            case "t310":
            case "t311":
            case "t312":
            case "t313":
            case "t314":
            case "t315":
                toolType = SkillConstants.SKILL_CODE_BUILD;
                break;
            default:
                toolType = SkillConstants.SKILL_CODE_MELEE_HIT;
                break;
        }
        switch (tool.getItemNo()) {
            case "t008":
            case "t009":
            case "t011":
            case "t100":
            case "t105":
            case "t111":
            case "t116":
            case "t200":
            case "t202":
            case "t205":
            case "t206":
            case "t207":
            case "t209":
            case "t210":
            case "t212":
            case "t213":
            case "t214":
            case "t218":
            case "t220":
            case "t222":
            case "t223":
            case "t224":
            case "t228":
            case "t229":
            case "t230":
            case "t231":
                skillMode = SkillConstants.SKILL_MODE_AUTO;
                break;
            default:
                skillMode = SkillConstants.SKILL_MODE_SEMI_AUTO;
                break;
        }
        switch (tool.getItemNo()) {
            case "t100":
            case "t105":
            case "t111":
            case "t116":
            case "t205":
            case "t206":
            case "t210":
            case "t222":
            case "t228":
                skillTime = 1;
                break;
            case "t224":
                skillTime = 2;
                break;
            case "t200":
            case "t207":
            case "t208":
            case "t209":
            case "t213":
            case "t214":
            case "t223":
            case "t229":
            case "t230":
                skillTime = 3;
                break;
            case "t000":
            case "t003":
            case "t016":
            case "t018":
            case "t212":
            case "t225":
            case "t231":
                skillTime = 5;
                break;
            case "t002":
            case "t019":
            case "t113":
            case "t114":
            case "t202":
                skillTime = 10;
                break;
            case "t017":
            case "t106":
            case "t201":
            case "t217":
            case "t220":
                skillTime = 20;
                break;
            case "t001":
            case "t004":
            case "t005":
            case "t009":
            case "t020":
            case "t104":
            case "t112":
            case "t115":
            case "t117":
            case "t203":
            case "t204":
            case "t216":
            case "t218":
                skillTime = 25;
                break;
            case "t006":
            case "t007":
            case "t008":
            case "t010":
            case "t011":
            case "t012":
            case "t211":
                skillTime = 50;
                break;
            case "t013":
            case "t014":
            case "t015":
            case "t101":
            case "t102":
            case "t215":
            case "t227":
                skillTime = 75;
                break;
            case "t021":
            case "t103":
            case "t109":
            case "t110":
            case "t221":
            case "t226":
                skillTime = 100;
                break;
            case "t108":
            case "t219":
                skillTime = 125;
                break;
            case "t107":
                skillTime = 250;
                break;
            default:
                skillTime = SkillConstants.SKILL_DEFAULT_FRAME;
                break;
        }
        BigDecimal range;
        switch (toolType) {
            case SkillConstants.SKILL_CODE_SHOOT_HIT:
            case SkillConstants.SKILL_CODE_SHOOT_ARROW:
            case SkillConstants.SKILL_CODE_SHOOT_GUN:
            case SkillConstants.SKILL_CODE_SHOOT_MAGNUM:
            case SkillConstants.SKILL_CODE_SHOOT_ROCKET:
                range = SkillConstants.SKILL_RANGE_SHOOT;
                break;
            case SkillConstants.SKILL_CODE_SHOOT_SHOTGUN:
                range = SkillConstants.SKILL_RANGE_SHOOT_SHOTGUN;
                break;
            case SkillConstants.SKILL_CODE_SHOOT_FIRE:
                range = SkillConstants.SKILL_RANGE_SHOOT_FIRE_MAX;
                break;
            case SkillConstants.SKILL_CODE_SHOOT_WATER:
                range = SkillConstants.SKILL_RANGE_SHOOT_WATER;
                break;
            case SkillConstants.SKILL_CODE_MELEE_HIT:
            case SkillConstants.SKILL_CODE_MELEE_KICK:
            case SkillConstants.SKILL_CODE_MELEE_SCRATCH:
            case SkillConstants.SKILL_CODE_MELEE_CLEAVE:
            case SkillConstants.SKILL_CODE_MELEE_STAB:
            case SkillConstants.SKILL_CODE_LAY:
            default:
                range = SkillConstants.SKILL_RANGE_MELEE;
                break;
        }
        int skillType;
        switch (toolType) {
            case SkillConstants.SKILL_CODE_SHOOT_HIT:
            case SkillConstants.SKILL_CODE_SHOOT_ARROW:
            case SkillConstants.SKILL_CODE_SHOOT_GUN:
            case SkillConstants.SKILL_CODE_SHOOT_MAGNUM:
            case SkillConstants.SKILL_CODE_SHOOT_ROCKET:
            case SkillConstants.SKILL_CODE_SHOOT_SHOTGUN:
            case SkillConstants.SKILL_CODE_SHOOT_FIRE:
            case SkillConstants.SKILL_CODE_SHOOT_WATER:
            case SkillConstants.SKILL_CODE_MELEE_HIT:
            case SkillConstants.SKILL_CODE_MELEE_KICK:
            case SkillConstants.SKILL_CODE_MELEE_SCRATCH:
            case SkillConstants.SKILL_CODE_MELEE_CLEAVE:
            case SkillConstants.SKILL_CODE_MELEE_STAB:
            case SkillConstants.SKILL_CODE_LAY:
                skillType = SkillConstants.SKILL_TYPE_ATTACK;
                break;
            default:
                skillType = SkillConstants.SKILL_TYPE_DEFAULT;
                break;
        }
        Skill skill0 = new Skill(toolType, skillMode, 0, skillTime, skillType, range, null);
        tool.getSkills().add(skill0);
    }

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
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_ATTACK,
                SkillConstants.SKILL_RANGE_MELEE, null));
        skills.add(new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
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
     * @return changed hp amount
     */
    public static int calculateChangedHp(final int eventCode) {
        Random random = new Random();
        switch (eventCode) {
            case GamePalConstants.EVENT_CODE_HEAL:
                return 100;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
                return -10 + random.nextInt(10);
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
                return -20 + random.nextInt(10);
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
                return -40 + random.nextInt(30);
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
                return -75 + random.nextInt(50);
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
                return -100 + random.nextInt(100);
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
                return -110 + random.nextInt(20);
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
                return -250 + random.nextInt(200);
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
                return -250 + random.nextInt(100);
            case GamePalConstants.EVENT_CODE_EXPLODE:
                return -600 + random.nextInt(400);
            case GamePalConstants.EVENT_CODE_FIRE:
                return -3;
            default:
                return 0;
        }
    }

    public static boolean blockCode2Build(int code) {
        switch (code) {
//            case BlockConstants.BLOCK_CODE_NOTHING:
            case BlockConstants.BLOCK_CODE_WATER:
                return false;
            default:
                return true;
        }
    }
}
