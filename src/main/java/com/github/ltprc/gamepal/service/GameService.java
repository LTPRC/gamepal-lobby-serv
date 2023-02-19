package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.game.Game;

import java.util.Map;

public interface GameService {

    Map<Integer, Game> getGameMap();
}
