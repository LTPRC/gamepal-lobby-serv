package com.github.ltprc.gamepal.service.impl;

import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.service.GameService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Service
public class GameServiceImpl implements GameService {

    private static final Log logger = LogFactory.getLog(GameServiceImpl.class);
    private Map<Integer, Game> gameMap = new LinkedHashMap<>(); // gameNo, game

    @Override
    public Map<Integer, Game> getGameMap() {
        return gameMap;
    }
}
