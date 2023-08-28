package com.github.ltprc.gamepal.model.world;

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
}
