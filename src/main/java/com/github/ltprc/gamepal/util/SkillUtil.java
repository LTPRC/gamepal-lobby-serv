package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.Skill;
import com.github.ltprc.gamepal.model.item.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SkillUtil {

    private SkillUtil() {}

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
            case "t008":
            case "t009":
            case "t011":
            case "t021":
            case "t218":
                // TODO
            default:
                toolType = SkillConstants.SKILL_CODE_MELEE_HIT;
                break;
        }
        switch (tool.getItemNo()) {
            case "t008":
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
            case "t218":
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
            case "t020":
            case "t104":
            case "t112":
            case "t115":
            case "t117":
            case "t203":
            case "t204":
            case "t216":
                skillTime = 25;
                break;
            case "t006":
            case "t007":
            case "t010":
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
            case "t008":
            case "t009":
            case "t011":
            default:
                skillTime = SkillConstants.SKILL_DEFAULT_FRAME;
                break;
        }
        tool.setItemType(toolType);
        tool.setItemMode(skillMode);
        tool.setItemTime(skillTime);
    }

    public static void updateSkills(PlayerInfo playerInfo) {
        Skill[] skills = new Skill[4];
        skills[0] = new Skill(SkillConstants.SKILL_CODE_MELEE_HIT, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_ATTACK,
                GamePalConstants.EVENT_MAX_DISTANCE_MELEE);
        skills[1] = new Skill(SkillConstants.SKILL_CODE_MELEE_KICK, SkillConstants.SKILL_MODE_SEMI_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME * 2, SkillConstants.SKILL_TYPE_ATTACK,
                GamePalConstants.EVENT_MAX_DISTANCE_MELEE);
        skills[2] = new Skill(SkillConstants.SKILL_CODE_CURSE, SkillConstants.SKILL_MODE_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_DEFAULT, BigDecimal.ZERO);
        skills[3] = new Skill(SkillConstants.SKILL_CODE_CHEER, SkillConstants.SKILL_MODE_AUTO, 0,
                SkillConstants.SKILL_DEFAULT_FRAME, SkillConstants.SKILL_TYPE_DEFAULT, BigDecimal.ZERO);
        playerInfo.getTools().forEach((String toolStr) -> {
            Tool tool = new Tool();
            tool.setItemNo(toolStr);
            defineToolProps(tool);
            skills[0] = new Skill(tool.getItemType(), tool.getItemMode(), 0,
                    tool.getItemTime(), SkillConstants.SKILL_TYPE_ATTACK,
                    GamePalConstants.EVENT_MAX_DISTANCE_MELEE);
        });
        playerInfo.setSkill(skills);
    }

    public static boolean validateDamage(PlayerInfo playerInfo) {
        return playerInfo.getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0;
    }
}
