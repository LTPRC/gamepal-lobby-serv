package com.github.ltprc.gamepal.model.creature;

import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Data
@NoArgsConstructor
public class PlayerInfo {

    private int playerStatus;
    private int creatureType;
    private int gender;
    private int skinColor;
    private int[] buff = new int[BuffConstants.BUFF_CODE_LENGTH]; // buff code, remaining frame
    private List<Skill> skills = new ArrayList<>(SkillConstants.SKILL_LENGTH);
    private PerceptionInfo perceptionInfo;
//    private int hpMax;
//    private int hp;
    private int vpMax;
    private int vp;
    private int hunger;
    private int hungerMax;
    private int thirst;
    private int thirstMax;
    private int precision;
    private int precisionMax;

    // Basic properties
    private int playerType; // 0-human 1-AI

    // Character properties
    private String avatar;
    private String firstName;
    private String lastName;
    private String nickname;
    private String nameColor;
    private int hairstyle;
    private String hairColor;
    private int eyes;
    private int[] faceCoefs = new int[CreatureConstants.FACE_COEFS_LENGTH];

    // Dynamic properties
    private int level;
    private int exp;
    private int expMax;
    private int money;
    private Set<String> tools = new ConcurrentSkipListSet<>();
    private Set<String> outfits = new ConcurrentSkipListSet<>();
    private String bossId;
    private String topBossId;
    private WorldCoordinate respawnPoint;
}
