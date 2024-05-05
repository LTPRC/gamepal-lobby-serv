package com.github.ltprc.gamepal.model.creature;

import com.github.ltprc.gamepal.model.map.world.WorldMovingBlock;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatureInfo extends WorldMovingBlock {
    private int creatureType;
    private int gender;
    private int skinColor;
    private int[] buff; // buff code, remaining frame
    private Skill[] skill;
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
