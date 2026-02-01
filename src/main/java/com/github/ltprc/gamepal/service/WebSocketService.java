package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.map.world.GameWorld;

import javax.websocket.Session;

public interface WebSocketService {

    void onMessage(String message);

    void onOpen(Session session, String userCode);

    void onClose(String userCode);

    void resetPlayerBlockMapByUser(String userCode);

    void resetPlayerBlockMapByBlock(GameWorld world, String id);

    void resetPlayerBlockMapByUserAndBlock(GameWorld world, String userCode, String id);
}
