package com.github.ltprc.gamepal.model.creature;

import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import lombok.Data;

import java.util.Deque;
import java.util.Queue;

@Data
public class NpcBrain {

    private int behavior;
    private int stance;
    private boolean[] exemption;
    private Queue<WorldCoordinate> greenQueue;
    private Deque<WorldCoordinate> yellowQueue;
    private Queue<PlayerInfo> redQueue;
}
