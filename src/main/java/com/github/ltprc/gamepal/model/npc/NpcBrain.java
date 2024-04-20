package com.github.ltprc.gamepal.model.npc;

import lombok.Data;

import java.util.Queue;

@Data
public class NpcBrain {

    private int status;
    private boolean attackTeammate; // teammate / same creature
    private boolean attackStranger; // stranger / different creature
    private Queue<INpcTask> observeTaskQueue;
    private Queue<INpcTask> moveTaskQueue;
    private Queue<INpcTask> attackTaskQueue;
}
