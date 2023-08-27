package com.github.ltprc.gamepal.model;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.Data;

import javax.websocket.Session;
import java.util.Map;
import java.util.Queue;


@Data
public class GameWorld {
    private Map<String, Session> sessionMap; // userCode, session
    private Map<String, String> tokenMap; // userCode, token
    private Map<String, Long> onlineMap; // userCode, timestamp
    private Queue<String> onlineQueue; // userCode
    private Map<Integer, Map<Coordinate, Map<Coordinate, String>>> phaseMaps; // phaseNo, sceneCoordinate, blockCoordinate, blockCode
}
