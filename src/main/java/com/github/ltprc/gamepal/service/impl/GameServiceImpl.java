package com.github.ltprc.gamepal.service.impl;

import com.github.ltprc.gamepal.config.GameConstants;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.GameService;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;


@Service
public class GameServiceImpl implements GameService {

    private static final Log logger = LogFactory.getLog(GameServiceImpl.class);

    @Override
    public int addGame(GameWorld world, int gameType) {
        int gameNo = world.getGameMap().keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        Game game = new Game(gameNo, gameType, GameConstants.GAME_STATUS_WAITING, GameConstants.PUBG_PLAYER_COUNT_MIN,
                GameConstants.PUBG_PLAYER_COUNT_MAX);
        world.getGameMap().put(gameNo, game);
        return gameNo;
    }

    @Override
    public boolean joinGame(GameWorld world, int number, String userCode) {
        if (!world.getGameMap().containsKey(number)) {
            logger.error(ErrorUtil.ERROR_1101);
            return false;
        }
        Game game = world.getGameMap().get(number);
        if (game.getStatus() != GameConstants.GAME_STATUS_WAITING) {
            logger.error(ErrorUtil.ERROR_1103);
            return false;
        }
        if (game.getPlayerMap().size() > game.getPlayerCountMax()) {
            logger.error(ErrorUtil.ERROR_1102);
            return false;
        }
        game.getPlayerMap().put(userCode, GameConstants.GAME_STATUS_WAITING);
        return true;
    }

    @Override
    public boolean leaveGame(GameWorld world, int number, String userCode) {
        if (!world.getGameMap().containsKey(number)) {
            logger.error(ErrorUtil.ERROR_1101);
            return false;
        }
        Game game = world.getGameMap().get(number);
        if (game.getStatus() != GameConstants.GAME_STATUS_WAITING) {
            logger.error(ErrorUtil.ERROR_1103);
            return false;
        }
        game.getPlayerMap().remove(userCode, GameConstants.GAME_PLAYER_STATUS_STANDBY);
        return true;
    }

    @Override
    public boolean prepareGame(GameWorld world, int number, String userCode) {
        if (!world.getGameMap().containsKey(number)) {
            logger.error(ErrorUtil.ERROR_1101);
            return false;
        }
        Game game = world.getGameMap().get(number);
        if (game.getStatus() != GameConstants.GAME_STATUS_WAITING) {
            logger.error(ErrorUtil.ERROR_1103);
            return false;
        }
        int playerStatus = game.getPlayerMap().get(userCode);
        if (playerStatus == GameConstants.GAME_PLAYER_STATUS_STANDBY) {
            game.getPlayerMap().remove(userCode, GameConstants.GAME_PLAYER_STATUS_PREPARED);
        } else if (playerStatus == GameConstants.GAME_PLAYER_STATUS_PREPARED) {
            game.getPlayerMap().remove(userCode, GameConstants.GAME_PLAYER_STATUS_STANDBY);
        } else {
            logger.error(ErrorUtil.ERROR_1104);
            return false;
        }
        return true;
    }

    @Override
    public boolean startGame(GameWorld world, int number) {
        if (!world.getGameMap().containsKey(number)) {
            logger.error(ErrorUtil.ERROR_1101);
            return false;
        }
        Game game = world.getGameMap().get(number);
        if (game.getStatus() != GameConstants.GAME_STATUS_WAITING) {
            logger.error(ErrorUtil.ERROR_1103);
            return false;
        }
        if (game.getPlayerMap().size() < game.getPlayerCountMin()
                || game.getPlayerMap().size() > game.getPlayerCountMax()) {
            logger.error(ErrorUtil.ERROR_1102);
            return false;
        }
        game.setStatus(GameConstants.GAME_STATUS_RUNNING);
        game.getPlayerMap().entrySet().stream().forEach(entry -> entry.setValue(GameConstants.GAME_STATUS_RUNNING));
        return true;
    }

    @Override
    public boolean terminateGame(GameWorld world, int number) {
        if (!world.getGameMap().containsKey(number)) {
            logger.error(ErrorUtil.ERROR_1101);
            return false;
        }
        Game game = world.getGameMap().get(number);
        game.setStatus(GameConstants.GAME_STATUS_END);
        game.getPlayerMap().entrySet().stream().forEach(entry -> entry.setValue(GameConstants.GAME_PLAYER_STATUS_END));
        return true;
    }
}
