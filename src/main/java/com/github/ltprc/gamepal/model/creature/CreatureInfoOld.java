package com.github.ltprc.gamepal.model.creature;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class CreatureInfoOld extends BlockInfo {

    private int playerStatus;
    private int creatureType;
    private int gender;
    private int skinColor;
    private int[] buff = new int[GamePalConstants.BUFF_CODE_LENGTH]; // buff code, remaining frame
    private List<Skill> skills = new ArrayList<>(SkillConstants.SKILL_LENGTH);
    private PerceptionInfo perceptionInfo;
    private int hpMax;
    private int hp;
    private int vpMax;
    private int vp;
    private int hunger;
    private int hungerMax;
    private int thirst;
    private int thirstMax;
    private int precision;
    private int precisionMax;
}
