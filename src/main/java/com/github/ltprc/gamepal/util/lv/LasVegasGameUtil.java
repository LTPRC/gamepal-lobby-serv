package com.github.ltprc.gamepal.util.lv;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.game.Cash;
import com.github.ltprc.gamepal.model.game.Dice;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.game.Player;
import com.github.ltprc.gamepal.model.game.lv.Casino;
import com.github.ltprc.gamepal.model.game.lv.LasVegasGame;
import com.github.ltprc.gamepal.model.game.lv.LasVegasPlayer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.*;

public class LasVegasGameUtil {
    private LasVegasGameUtil() {}
    private static final Log logger = LogFactory.getLog(LasVegasGameUtil.class);
    public static LasVegasGame getInstance() {
        LasVegasGame lasVegasGame = new LasVegasGame();
        lasVegasGame.setId(UUID.randomUUID().toString());
        lasVegasGame.setGameType(GamePalConstants.GAME_TYPE_LAS_VEGAS);
        lasVegasGame.setGameStatus(GamePalConstants.GAME_STATUS_WAITING);
        lasVegasGame.setGameNumber(1);
        lasVegasGame.setRoundNumber(1);
        lasVegasGame.setPlayerNumber(1);
        lasVegasGame.setMinPlayerNum(1);
        lasVegasGame.setMaxPlayerNum(5);
        Map<Integer, Casino> casinoMap = lasVegasGame.getCasinoMap();
        for (int i = 1; i <= 6; i++) {
            Casino casino = new Casino();
            casino.setCasinoNo(i);
            casino.setCashQueue(new LinkedList<>());
            casino.setDiceMap(new HashMap<>());
            casinoMap.put(i, casino);
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

    public static BigDecimal getTotalMoney(Casino casino) {
        if (null == casino.getCashQueue() || casino.getCashQueue().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return casino.getCashQueue().stream().map(cash -> cash.getValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static void addPlayer2Map(Map<Integer, Player> playerMap, Player player) {
        int playerNo = playerMap.entrySet().size() + 1;
        player.setPlayerNo(playerNo);
        playerMap.put(playerNo, player);
    }

    public static void removePlayerFromMap(Map<Integer, Player> playerMap, int playerNo) {
        playerMap.get(playerNo).setPlayerNo(0);
        for (int i = playerNo + 1; i <= playerMap.entrySet().size(); i++) {
            playerMap.put(i - 1, playerMap.get(i));
        }
        playerMap.remove(playerMap.entrySet().size());
    }

    public static void rollDiceQueue(LasVegasPlayer player) {
        player.getDiceQueue().stream().forEach(dice -> {
            dice.setPoint(Double.valueOf(1 + Math.random() * 6).intValue());
        });
    }

    public static String printDiceQueue(LasVegasPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("你的骰子结果为：[");
        player.getDiceQueue().stream().forEach(dice -> {
            sb.append(dice.getPoint());
            sb.append("][");
        });
        sb.append("]");
        return sb.toString();
    }

    public static void resetDiceQueue(LasVegasPlayer player, int diceNumber) {
        player.getDiceQueue().clear();
        for (int i = 0; i < diceNumber; i++) {
            player.getDiceQueue().add(new Dice());
        }
    }

    public static void updatePlayerNo(Game game) {
        if (game.getPlayerNumber() == game.getPlayerMap().size()) {
            game.setGameNumber(1);
            game.setRoundNumber(game.getRoundNumber() + 1);
        } else {
            game.setGameNumber(game.getPlayerNumber() + 1);
        }
    }

    public static int getDiceNumber(LasVegasPlayer player, int point) {
        if (null == player.getDiceQueue()) {
            return 0;
        }
        return Long.valueOf(player.getDiceQueue().stream().filter(dice -> dice.getPoint() == point).count()).intValue();
    }

    public static List<String> calculateWinners(LasVegasGame game) {
        List<String> winners = new ArrayList<>();
        BigDecimal maxMoney = BigDecimal.valueOf(-1);
        game.getPlayerMap().entrySet().stream().forEach(entry -> {
            BigDecimal total = ((LasVegasPlayer) entry.getValue()).getMoney();
            if (total.equals(maxMoney)) {
                winners.add(entry.getValue().getId());
            } else if (total.compareTo(maxMoney) > 0) {
                winners.clear();
                winners.add(entry.getValue().getId());
            }
        });
        return winners;
    }
}
