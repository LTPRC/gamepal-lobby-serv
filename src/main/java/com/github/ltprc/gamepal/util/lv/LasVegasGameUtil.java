package com.github.ltprc.gamepal.util.lv;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.game.Cash;
import com.github.ltprc.gamepal.model.game.Player;
import com.github.ltprc.gamepal.model.game.lv.Casino;
import com.github.ltprc.gamepal.model.game.lv.LasVegasGame;
import com.github.ltprc.gamepal.model.game.lv.LasVegasPlayer;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.*;

public class LasVegasGameUtil {
    private static final Log logger = LogFactory.getLog(LasVegasGameUtil.class);
    public static LasVegasGame getInstance() {
        LasVegasGame lasVegasGame = new LasVegasGame();
        lasVegasGame.setId(UUID.randomUUID().toString());
        lasVegasGame.setGameType(GamePalConstants.GAME_TYPE_LAS_VEGAS);
        lasVegasGame.setGameStatus(GamePalConstants.GAME_STATUS_WAITING);
        lasVegasGame.setGameNumber(0);
        lasVegasGame.setRoundNumber(0);
        lasVegasGame.setPlayerNumber(0);
        lasVegasGame.setMinPlayerNum(3);
        lasVegasGame.setMaxPlayerNum(5);
        Map<Integer, Casino> casinoMap = lasVegasGame.getCasinoMap();
        for (int i = 0; i < 6; i++) {
            casinoMap.put(i, new Casino());
        }
        Stack<Cash> cashStack = lasVegasGame.getCashStack();
        for (int i = 0; i < 5; i++) {
            cashStack.push(new Cash(new BigDecimal(60000)));
            cashStack.push(new Cash(new BigDecimal(70000)));
            cashStack.push(new Cash(new BigDecimal(80000)));
            cashStack.push(new Cash(new BigDecimal(90000)));
        }
        for (int i = 0; i < 6; i++) {
            cashStack.push(new Cash(new BigDecimal(10000)));
            cashStack.push(new Cash(new BigDecimal(40000)));
            cashStack.push(new Cash(new BigDecimal(50000)));
        }
        for (int i = 0; i < 8; i++) {
            cashStack.push(new Cash(new BigDecimal(20000)));
            cashStack.push(new Cash(new BigDecimal(30000)));
        }
        Collections.shuffle(cashStack);
        return lasVegasGame;
    }

    public static void initiateGame(LasVegasGame lasVegasGame) {
        if (lasVegasGame.getGameStatus() != GamePalConstants.GAME_STATUS_WAITING) {
            logger.warn(ErrorUtil.ERROR_1014);
        }
        Map<Integer, Player> playerMap = lasVegasGame.getPlayerMap();
        playerMap.entrySet().stream().forEach(entry -> {
            ((LasVegasPlayer) entry.getValue()).setDiceNum(8);
        });
        lasVegasGame.setGameStatus(GamePalConstants.GAME_STATUS_RUNNING);
    }
}
