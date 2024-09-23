package com.github.ltprc.gamepal.model.item;

import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.creature.Skill;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Tool extends Item {
    // 0 - 默认共用
    private int itemIndex;
    private String ammoCode;
    private List<Skill> skills = new ArrayList<>(SkillConstants.SKILL_LENGTH);
}
