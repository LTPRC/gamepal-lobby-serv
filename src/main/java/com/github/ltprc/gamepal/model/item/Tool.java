package com.github.ltprc.gamepal.model.item;

import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.creature.Skill;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Tool extends Item {
    // 0 - 默认共用
    private int itemIndex;
    private String ammoCode;
    private Skill[] skills = new Skill[SkillConstants.SKILL_LENGTH];
}
