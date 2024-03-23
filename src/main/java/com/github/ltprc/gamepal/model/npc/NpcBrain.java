package com.github.ltprc.gamepal.model.npc;

import lombok.Data;

import java.util.Queue;

@Data
public class NpcBrain {

    private Queue<INpcTask> observeTaskQueue;
    private Queue<INpcTask> moveTaskQueue;
    private Queue<INpcTask> attackTaskQueue;
}
