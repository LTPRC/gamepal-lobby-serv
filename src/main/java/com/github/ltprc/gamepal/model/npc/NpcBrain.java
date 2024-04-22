package com.github.ltprc.gamepal.model.npc;

import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import lombok.Data;

import java.util.Deque;
import java.util.Queue;

@Data
public class NpcBrain {

    private int behavior;
    private int stance;
    private boolean peaceWithTeammate;
    private boolean peaceWithSameCreature;
    private Queue<WorldCoordinate> greenQueue;
    private Deque<WorldCoordinate> yellowQueue;
    private Queue<PlayerInfo> redQueue;
}
