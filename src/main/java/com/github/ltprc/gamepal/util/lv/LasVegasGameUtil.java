package com.github.ltprc.gamepal.util.lv;

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
        lasVegasGame.setUserCode(UUID.randomUUID().toString());
        lasVegasGame.setGameStatus(0);
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
        lasVegasGame.setGameStatus(1);
        return lasVegasGame;
    }

    public static void initiateGame(LasVegasGame lasVegasGame, List<String> userCodes) {
        if (lasVegasGame.getGameStatus() != 1) {
            logger.warn(ErrorUtil.ERROR_1014);
        }
        Map<Integer, Player> playerMap = lasVegasGame.getPlayerMap();
        for (int i = 0; i < userCodes.size(); i++) {
            LasVegasPlayer lasVegasPlayer = new LasVegasPlayer();
            lasVegasPlayer.setUserCode(userCodes.get(i));
            lasVegasPlayer.setDiceNum(8);
            playerMap.put(i, lasVegasPlayer);
        }
        lasVegasGame.setGameStatus(2);
    }
}
