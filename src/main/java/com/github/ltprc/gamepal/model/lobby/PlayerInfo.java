package com.github.ltprc.gamepal.model.lobby;

import com.github.ltprc.gamepal.model.map.UserCoordinate;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class PlayerInfo {
    private int playerType; // 0-human player 1-npc
    private String userCode;
    private int worldNo;
    private UserCoordinate userCoordinate = new UserCoordinate();
    private Set<String> decorations = new HashSet<>();
}
