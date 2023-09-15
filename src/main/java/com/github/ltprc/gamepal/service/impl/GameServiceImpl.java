package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.game.Cash;
import com.github.ltprc.gamepal.model.game.Player;
import com.github.ltprc.gamepal.model.game.lv.LasVegasGame;
import com.github.ltprc.gamepal.model.game.lv.LasVegasPlayer;
import com.github.ltprc.gamepal.model.terminal.GameTerminal;
import com.github.ltprc.gamepal.model.terminal.Terminal;
import com.github.ltprc.gamepal.service.GameService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.lv.LasVegasGameUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private static final Log logger = LogFactory.getLog(GameServiceImpl.class);

    @Autowired
    private PlayerService playerService;

    @Override
    public ResponseEntity checkStatus(GameTerminal gameTerminal) {
        JSONObject rst = ContentUtil.generateRst();
        if (null == gameTerminal.getWorld()) {
            gameTerminal.addOutput("用户信息错误，桌游载入失败。");
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1021));
        }
        switch (gameTerminal.getStatus()) {
            case GamePalConstants.GAME_PLAYER_STATUS_START:
                gameTerminal.addOutput("欢迎开启桌游。");
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_SEEKING:
                gameTerminal.addOutput("以下是当前正在进行的游戏列表：");
                gameTerminal.getWorld().getGameMap().entrySet().stream().forEach(entry -> {
                    gameTerminal.addOutput("[" + entry.getKey() + "] "
                            + convertGameType2Name(entry.getValue().getGameType()) + ": "
                            + entry.getValue().getPlayerMap().size() + " / "
                            + entry.getValue().getMinPlayerNum() + "~" + entry.getValue().getMaxPlayerNum());
                });
                gameTerminal.addOutput("输入编号进入对应桌游局：");
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_STANDBY:
            case GamePalConstants.GAME_PLAYER_STATUS_PREPARED:
            case GamePalConstants.GAME_PLAYER_STATUS_PLAYING:
                if (null == gameTerminal.getGame()) {
                    gameTerminal.addOutput("用户信息错误，桌游局载入失败。");
                    return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1022));
                }
                return checkStatusLasVegas(gameTerminal);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity input(GameTerminal gameTerminal, String input) {
        switch (gameTerminal.getStatus()) {
            case GamePalConstants.GAME_PLAYER_STATUS_START:
                if ("start".equals(input)) {
                    gameTerminal.addOutput("连接中。");
                    gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_SEEKING);
                    break;
                }
            case GamePalConstants.GAME_PLAYER_STATUS_SEEKING:
                if (!ContentUtil.isInteger(input)) {
                    gameTerminal.addOutput("你的指令无法被识别。");
                    break;
                }
                int inputInt = Integer.valueOf(input);
                if (gameTerminal.getWorld().getGameMap().containsKey(inputInt)) {
                    gameTerminal.setGame(gameTerminal.getWorld().getGameMap().get(inputInt));
                    gameTerminal.addOutput("已加入编号" + inputInt + "的桌游局。");
                    gameTerminal.addOutput("桌游的类型为" + convertGameType2Name(gameTerminal.getGame().getGameType()) + "。");
                    switch (gameTerminal.getGame().getGameType()) {
                        case GamePalConstants.GAME_TYPE_LAS_VEGAS:
                            gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_STANDBY);
                            gameTerminal.getGame().getStandbySet().add(gameTerminal.getId());
                            break;
                        default:
                            gameTerminal.addOutput("目前不支持这种游戏类型。");
                            break;
                    }
                } else {
                    gameTerminal.addOutput("无法加入编号" + inputInt + "的桌游局，桌游局不存在。");
                }
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_STANDBY:
            case GamePalConstants.GAME_PLAYER_STATUS_PREPARED:
            case GamePalConstants.GAME_PLAYER_STATUS_PLAYING:
                inputLasVegas(gameTerminal, input);
                break;
        }
        return checkStatus(gameTerminal);
    }

    private String convertGameType2Name(int gameType) {
        switch (gameType) {
            case GamePalConstants.GAME_TYPE_LAS_VEGAS:
                return "拉斯维加斯";
            default:
                return "不详";
        }
    }

    public ResponseEntity checkStatusLasVegas(GameTerminal gameTerminal) {
        JSONObject rst = ContentUtil.generateRst();
        switch (gameTerminal.getGame().getGameStatus()) {
            case GamePalConstants.GAME_STATUS_START:
                gameTerminal.addOutput("本局桌游尚未初始化。");
                break;
            case GamePalConstants.GAME_STATUS_WAITING:
                gameTerminal.addOutput("玩家如下：");
                gameTerminal.getGame().getPlayerMap().entrySet().stream().forEach(entry -> {
                    Terminal terminal = playerService.getTerminalMap().get(entry.getValue().getId());
                    gameTerminal.addOutput("[" + entry.getKey() + "] " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
                });
                gameTerminal.addOutput("游客如下：");
                gameTerminal.getGame().getStandbySet().stream().forEach(standBy -> {
                    Terminal terminal = playerService.getTerminalMap().get(standBy);
                    gameTerminal.addOutput("- " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
                });
                if (gameTerminal.getGame().getStandbySet().contains(gameTerminal.getId())) {
                    gameTerminal.addOutput("输入：0-退出房间 1-准备游戏");
                } else {
                    gameTerminal.addOutput("输入：0-取消准备 1-启动游戏");
                }
                break;
            case GamePalConstants.GAME_STATUS_RUNNING:
                // TODO display and return obj
                LasVegasGame game = (LasVegasGame) gameTerminal.getGame();
                // Init
                game.getPlayerMap().entrySet().stream().forEach(entry -> {
                    ((LasVegasPlayer) entry.getValue()).setDiceNum(8);
                });
                int casinoIndex = 0;
                while (!game.getCashStack().empty()) {
                    while (casinoIndex < game.getCasinoMap().size() && LasVegasGameUtil.getTotalMoney(game.getCasinoMap().get(casinoIndex)).compareTo(BigDecimal.valueOf(50000)) >= 0) {
                        casinoIndex++;
                    }
                    if (casinoIndex >= game.getCasinoMap().size()) {
                        break;
                    }
                    Cash cash = game.getCashStack().pop();
                    game.getCasinoMap().get(casinoIndex).getCashQueue().add(cash);
                }
                if (game.getCashStack().empty()) {
                    gameTerminal.addOutput("货币全部入场，游戏结束！获胜者：");
                    List<String> winners = calculateWinners(game);
                    winners.stream().forEach(winner -> {
                        Terminal terminal = playerService.getTerminalMap().get(winner);
                        gameTerminal.addOutput("- " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
                    });
                    game.setGameStatus(GamePalConstants.GAME_STATUS_END);
                }
                // Play
                gameTerminal.addOutput("局数[" + game.getGameNumber() + "] 轮数[" + game.getRoundNumber() + "] 玩家["
                        + game.getPlayerNumber() + "]"
                        + playerService.getPlayerInfoMap().get(gameTerminal.getUserCode()).getNickname());
                // Communicate
                JSONObject gameOutput = new JSONObject();
                gameOutput.put("players", game.getPlayerMap());
                gameOutput.put("casinos", game.getCasinoMap());
                gameTerminal.setGameOutput(gameOutput);


                break;
            case GamePalConstants.GAME_STATUS_END:
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    public ResponseEntity inputLasVegas(GameTerminal gameTerminal, String input) {
        JSONObject rst = ContentUtil.generateRst();
        switch (gameTerminal.getGame().getGameStatus()) {
            case GamePalConstants.GAME_STATUS_START:
            case GamePalConstants.GAME_STATUS_WAITING:
                if (!ContentUtil.isInteger(input)) {
                    gameTerminal.addOutput("你的指令无法被识别。");
                    break;
                }
                int inputInt = Integer.valueOf(input);
                if (GamePalConstants.GAME_PLAYER_STATUS_STANDBY == gameTerminal.getStatus()) {
                    if (0 == inputInt) {
                        gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_SEEKING);
                        gameTerminal.getGame().getStandbySet().remove(gameTerminal.getId());
                        gameTerminal.setGame(null);
                        gameTerminal.addOutput("你已经离开上一局游戏。");
                    } else if (1 == inputInt) {
                        gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_PREPARED);
                        LasVegasPlayer lasVegasPlayer = new LasVegasPlayer();
                        lasVegasPlayer.setId(gameTerminal.getId());
                        lasVegasPlayer.setDiceNum(8);
                        for (int i = 1; i < Integer.MAX_VALUE; i++) {
                            if (!gameTerminal.getGame().getPlayerMap().containsKey(i)) {
                                gameTerminal.setPlayerNo(i);
                                gameTerminal.getGame().getPlayerMap().put(i, lasVegasPlayer);
                                break;
                            }
                        }
                        gameTerminal.getGame().getStandbySet().remove(gameTerminal.getId());
                        gameTerminal.addOutput("你已经准备好下一局游戏。");
                    }
                } else if (GamePalConstants.GAME_PLAYER_STATUS_PREPARED == gameTerminal.getStatus()) {
                    if (0 == inputInt) {
                        gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_STANDBY);
                        gameTerminal.getGame().getStandbySet().add(gameTerminal.getId());
                        gameTerminal.getGame().getPlayerMap().remove(gameTerminal.getPlayerNo());
                        gameTerminal.addOutput("你已经取消准备下一局游戏。");
                    } else if (1 == inputInt) {
                        if (gameTerminal.getGame().getPlayerMap().size() < gameTerminal.getGame().getMinPlayerNum()
                                || gameTerminal.getGame().getPlayerMap().size() > gameTerminal.getGame().getMaxPlayerNum()) {
                            gameTerminal.addOutput("玩家人数不对，不能启动游戏。");
                            break;
                        }
                        gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_PLAYING);
                        gameTerminal.getGame().setGameStatus(GamePalConstants.GAME_STATUS_RUNNING);
                        playerService.getTerminalMap().entrySet().stream().filter(entry ->
                                gameTerminal.getGame().getPlayerMap().values().contains(entry.getValue().getId())
                        ).forEach(entry -> {
                            entry.getValue().setStatus(GamePalConstants.GAME_STATUS_RUNNING);
                        });
                        gameTerminal.addOutput("你已经主动开启了下一局游戏。");
                    }
                }
                break;
            case GamePalConstants.GAME_STATUS_RUNNING:
                LasVegasGame game = (LasVegasGame) gameTerminal.getGame();
                // Operate
                if (game.getGameNumber() != gameTerminal.getPlayerNo()) {
                    gameTerminal.addOutput("没有轮到你操作。");
                    break;
                }
                if (!ContentUtil.isInteger(input)) {
                    gameTerminal.addOutput("你的指令无法被识别。");
                    break;
                }

                // Update numbers
                Queue<Integer> playerNos = new PriorityQueue<>();
                game.getPlayerMap().entrySet().stream().forEach(entry -> playerNos.add(entry.getKey()));
                int min = playerNos.peek();
                int next = game.getGameNumber();
                while (!playerNos.isEmpty()) {
                    int num = playerNos.poll();
                    if (num > next) {
                        next = num;
                    }
                }
                if (next == game.getGameNumber()) {
                    game.setGameNumber(min);
                    game.setRoundNumber(game.getRoundNumber() + 1);
                } else {
                    game.setGameNumber(next);
                }
                break;
            case GamePalConstants.GAME_STATUS_END:
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    private List<String> calculateWinners(LasVegasGame game) {
        List<String> winners = new ArrayList<>();
        BigDecimal maxMoney = BigDecimal.valueOf(-1);
        game.getPlayerMap().entrySet().stream().forEach(entry -> {
            BigDecimal total = BigDecimal.valueOf(0);
            ((LasVegasPlayer) entry.getValue()).getCashStack().stream().forEach(cash -> {
                total.add(cash.getValue());
            });
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
