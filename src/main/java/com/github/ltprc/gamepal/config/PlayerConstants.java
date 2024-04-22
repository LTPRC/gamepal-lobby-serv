package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class PlayerConstants {

    private PlayerConstants() {}

    // Backend constants

    public static final int PLAYER_TYPE_HUMAN = 0;
    public static final int PLAYER_TYPE_AI = 1;
    public static final int PLAYER_TYPE_ANIMAL = 3;

    public static final int NPC_BRAIN_STATUS_UNAWARE = 0;
    public static final int NPC_BRAIN_STATUS_SUSPICIOUS = 1;
    public static final int NPC_BRAIN_STATUS_ALERT = 2;
    public static final int NPC_BRAIN_STATUS_SEARCHING = 3;
    public static final int NPC_BRAIN_STATUS_FOLLOWING = 4;
    public static final int NPC_BRAIN_STATUS_WANDERING = 5;

    public static final int NPC_BEHAVIOR_IDLE = 0;
    public static final int NPC_BEHAVIOR_MOVE = 1;
    public static final int NPC_BEHAVIOR_GUARD = 2;
    public static final int NPC_BEHAVIOR_PATROL = 3;
    public static final int NPC_BEHAVIOR_FOLLOW = 4;
    public static final int NPC_TASK_TYPE_IDLE = 0;
    public static final int NPC_TASK_TYPE_OBSERVE = 1;
    public static final int NPC_TASK_TYPE_MOVE = 2;
    public static final int NPC_TASK_TYPE_ATTACK = 3;
    public static final int STANCE_AGGRESSIVE = 1;
    public static final int STANCE_DEFENSIVE = 2;
    public static final int STANCE_STAND_GROUND = 3;
    public static final int STANCE_NO_ATTACK = 4;
    public static final BigDecimal NPC_ARRIVE_DISTANCE = BigDecimal.valueOf(0.1);
    public static final BigDecimal NPC_CHASE_DISTANCE = BigDecimal.ONE;
}
