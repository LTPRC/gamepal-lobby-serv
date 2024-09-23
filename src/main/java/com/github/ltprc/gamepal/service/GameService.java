package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface GameService {

    int addGame(GameWorld world, int gameType);

    boolean joinGame(GameWorld world, int number, String userCode);

    boolean leaveGame(GameWorld world, int number, String userCode);

    boolean prepareGame(GameWorld world, int number, String userCode);

    boolean startGame(GameWorld world, int number);

    boolean terminateGame(GameWorld world, int number);
}
