package com.github.ltprc.gamepal.model.creature;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.map.world.WorldMovingBlock;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatureInfo extends WorldMovingBlock {
    private int creatureType;
    private int gender;
    private int skinColor;
    private int[] buff = new int[GamePalConstants.BUFF_CODE_LENGTH]; // buff code, remaining frame
    private Skill[] skill = new Skill[SkillConstants.SKILL_LENGTH];
    private PerceptionInfo perceptionInfo;
    private int hpMax;
    private int hp;
    private int vpMax;
    private int vp;
    private int hunger;
    private int hungerMax;
    private int thirst;
    private int thirstMax;
}
