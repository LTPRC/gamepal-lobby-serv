package com.github.ltprc.gamepal.service;

import javax.websocket.Session;

public interface WebSocketService {

    void onMessage(String message);

    void onOpen(Session session, String userCode);

    void onClose(String userCode);

    void communicate(String userCode, int gameState);
}
