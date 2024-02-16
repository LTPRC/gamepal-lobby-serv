package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.game.Cash;
import com.github.ltprc.gamepal.model.game.Dice;
import com.github.ltprc.gamepal.model.game.lv.LasVegasGame;
import com.github.ltprc.gamepal.model.game.lv.LasVegasPlayer;
import com.github.ltprc.gamepal.service.StateMachineService;
import com.github.ltprc.gamepal.terminal.GameTerminal;
import com.github.ltprc.gamepal.terminal.Terminal;
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

@Service
public class StateMachineServiceImpl implements StateMachineService {

    private static final Log logger = LogFactory.getLog(StateMachineServiceImpl.class);

    @Autowired
    private PlayerService playerService;

    @Override
    public ResponseEntity gameTerminalState(GameTerminal gameTerminal) {
        JSONObject rst = ContentUtil.generateRst();
        if (null == gameTerminal.getWorld()) {
            gameTerminal.addOutput("用户信息错误，终端机载入失败。");
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1021));
        }
        switch (gameTerminal.getStatus()) {
            case GamePalConstants.GAME_PLAYER_STATUS_END:
                gameTerminal.addOutput("");
                gameTerminal.addOutput("终端机已经中止运行。");
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_START:
                gameTerminal.addOutput("");
                gameTerminal.addOutput("终端机启动，载入当前正在进行的桌游列表中。");
                gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_SEEKING);
                return gameTerminalState(gameTerminal);
            case GamePalConstants.GAME_PLAYER_STATUS_SEEKING:
                gameTerminal.addOutput("");
                gameTerminal.addOutput("以下是当前正在进行的游戏列表：");
                gameTerminal.getWorld().getGameMap().entrySet().stream().forEach(entry -> {
                    gameTerminal.addOutput("[" + entry.getKey() + "] "
                            + convertGameType2Name(entry.getValue().getGameType()) + ": "
                            + entry.getValue().getPlayerMap().size() + " / "
                            + entry.getValue().getMinPlayerNum() + "~" + entry.getValue().getMaxPlayerNum());
                });
                gameTerminal.addOutput("输入【0】退出终端机");
                gameTerminal.addOutput("输入编号进入对应桌游局");
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_STANDBY:
                if (GamePalConstants.GAME_STATUS_WAITING == gameTerminal.getGame().getGameStatus()) {
                    notifyAllTerminals(gameTerminal, "");
                    notifyAllTerminals(gameTerminal, "桌游局信息发生更新。");
                    notifyAllTerminals(gameTerminal, "玩家如下：");
                    gameTerminal.getGame().getPlayerMap().entrySet().stream().forEach(entry -> {
                        Terminal terminal = playerService.getTerminalMap().get(entry.getValue().getId());
                        notifyAllTerminals(gameTerminal, "[" + entry.getKey() + "] " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
                    });
                    notifyAllTerminals(gameTerminal, "游客如下：");
                    gameTerminal.getGame().getStandbySet().stream().forEach(standBy -> {
                        Terminal terminal = playerService.getTerminalMap().get(standBy);
                        notifyAllTerminals(gameTerminal, "- " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
                    });
                    notifyAllTerminals(gameTerminal, "输入【0】退出桌游局");
                    notifyAllTerminals(gameTerminal, "输入【1】进入准备状态");
                } else if (GamePalConstants.GAME_PLAYER_STATUS_PLAYING == gameTerminal.getGame().getGameStatus()) {
                    lasVegasState(gameTerminal);
                }
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_PREPARED:
                notifyAllTerminals(gameTerminal, "");
                notifyAllTerminals(gameTerminal, "桌游局信息发生更新。");
                notifyAllTerminals(gameTerminal, "玩家如下：");
                gameTerminal.getGame().getPlayerMap().entrySet().stream().forEach(entry -> {
                    Terminal terminal = playerService.getTerminalMap().get(entry.getValue().getId());
                    notifyAllTerminals(gameTerminal, "[" + entry.getKey() + "] " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
                });
                notifyAllTerminals(gameTerminal, "游客如下：");
                gameTerminal.getGame().getStandbySet().stream().forEach(standBy -> {
                    Terminal terminal = playerService.getTerminalMap().get(standBy);
                    notifyAllTerminals(gameTerminal, "- " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
                });
                notifyAllTerminals(gameTerminal, "输入【0】退出桌游局");
                notifyAllTerminals(gameTerminal, "输入【1】取消准备状态");
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_PLAYING:
                if (null == gameTerminal.getGame()) {
                    gameTerminal.addOutput("用户信息错误，桌游局载入失败。");
                    return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1022));
                }
                switch (gameTerminal.getGame().getGameType()) {
                    case GamePalConstants.GAME_TYPE_LAS_VEGAS:
                        lasVegasState(gameTerminal);
                    default:
                        gameTerminal.addOutput("目前不支持这种游戏类型。");
                        return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1025));
                }
            default:
                return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1026));
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity gameTerminalInput(GameTerminal gameTerminal, String input) {
        if (null == gameTerminal.getWorld()) {
            gameTerminal.addOutput("用户信息错误，终端机载入失败。");
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1021));
        }
        int inputInt;
        switch (gameTerminal.getStatus()) {
            case GamePalConstants.GAME_PLAYER_STATUS_START:
                gameTerminal.addOutput("终端机初始化中。");
                gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_SEEKING);
                break;
            case GamePalConstants.GAME_PLAYER_STATUS_SEEKING:
                if (!ContentUtil.isInteger(input)) {
                    gameTerminal.addOutput("你的指令无法被识别。");
                    break;
                }
                inputInt = Integer.valueOf(input);
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
                if (!ContentUtil.isInteger(input)) {
                    gameTerminal.addOutput("你的指令无法被识别。");
                    break;
                }
                inputInt = Integer.valueOf(input);
                if (0 == inputInt) {
                    gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_SEEKING);
                    gameTerminal.getGame().getStandbySet().remove(gameTerminal.getId());
                    gameTerminal.setGame(null);
                    gameTerminal.addOutput("你已经离开上一局游戏。");
                } else if (1 == inputInt) {
                    gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_PREPARED);
                    LasVegasPlayer lasVegasPlayer = new LasVegasPlayer();
                    lasVegasPlayer.setId(gameTerminal.getId());
                    lasVegasPlayer.setName(playerService.getPlayerInfoMap().get(gameTerminal.getUserCode()).getNickname());
                    for (int i = 0; i < 8; i++) {
                        lasVegasPlayer.getDiceQueue().add(new Dice());
                    }
                    LasVegasGameUtil.addPlayer2Map(gameTerminal.getGame().getPlayerMap(), lasVegasPlayer);
                    gameTerminal.getGame().getStandbySet().remove(gameTerminal.getId());
                    gameTerminal.addOutput("你已经准备好下一局游戏。");
                    // Check whether the game can start now
                    gameTerminal.getGame().setGameStatus(GamePalConstants.GAME_STATUS_RUNNING);
                    gameTerminal.getGame().getPlayerMap().entrySet().stream().forEach(entry -> {
                        GameTerminal gameTerminal2 = (GameTerminal) playerService.getTerminalMap().get(entry.getValue().getId());
                        gameTerminal2.setStatus(GamePalConstants.GAME_PLAYER_STATUS_PLAYING);
                    });
                    notifyAllTerminals(gameTerminal, "");
                    notifyAllTerminals(gameTerminal, "人员已满，游戏自动开始。");
                } else {
                    gameTerminal.addOutput("没有意义的指令。");
                }
            case GamePalConstants.GAME_PLAYER_STATUS_PREPARED:
                if (!ContentUtil.isInteger(input)) {
                    gameTerminal.addOutput("你的指令无法被识别。");
                    break;
                }
                inputInt = Integer.valueOf(input);
                if (0 == inputInt) {
                    gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_SEEKING);
                    gameTerminal.getGame().getStandbySet().remove(gameTerminal.getId());
                    gameTerminal.setGame(null);
                    gameTerminal.addOutput("你已经离开上一局游戏。");
                } else if (1 == inputInt) {
                    gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_STANDBY);
                    gameTerminal.getGame().getStandbySet().add(gameTerminal.getId());
                    LasVegasGameUtil.removePlayerFromMap(gameTerminal.getGame().getPlayerMap(), gameTerminal.getPlayer().getPlayerNo());
                    gameTerminal.addOutput("你已经取消准备下一局游戏。");
                } else {
                    gameTerminal.addOutput("没有意义的指令。");
                }
            case GamePalConstants.GAME_PLAYER_STATUS_PLAYING:
                if (null == gameTerminal.getGame()) {
                    gameTerminal.addOutput("用户信息错误，桌游局载入失败。");
                    return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1022));
                }
                switch (gameTerminal.getGame().getGameType()) {
                    case GamePalConstants.GAME_TYPE_LAS_VEGAS:
                        lasVegasInput(gameTerminal, input);
                    default:
                        gameTerminal.addOutput("目前不支持这种游戏类型。");
                        return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1025));
                }
        }
        return gameTerminalState(gameTerminal);
    }

    @Override
    public ResponseEntity lasVegasState(GameTerminal gameTerminal) {
        JSONObject rst = ContentUtil.generateRst();
        LasVegasGame game = (LasVegasGame) gameTerminal.getGame();
//        if (game.getPlayerMap().entrySet().stream().filter(entry -> entry.getValue().getId().equals(gameTerminal.getId())).count() == 0) {
//            gameTerminal.addOutput("游戏已开始，请寻找其他房间。");
//            gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_SEEKING);
//        }
        if (game.getCashStack().empty()) {
            notifyAllTerminals(gameTerminal, "货币全部入场，游戏结束！获胜者：");
            List<String> winners = LasVegasGameUtil.calculateWinners(game);
            winners.stream().forEach(winner -> {
                Terminal terminal = playerService.getTerminalMap().get(winner);
                gameTerminal.addOutput("- " + playerService.getPlayerInfoMap().get(terminal.getUserCode()).getNickname());
            });
            game.setGameStatus(GamePalConstants.GAME_STATUS_END);
        }
        if (game.getRoundNumber() > 4) {
            notifyAllTerminals(gameTerminal, "新的一轮开始，补牌中。");
            int casinoIndex = 1;
            while (!game.getCashStack().empty()) {
                while (casinoIndex <= game.getCasinoMap().size() && LasVegasGameUtil.getTotalMoney(game.getCasinoMap().get(casinoIndex)).compareTo(BigDecimal.valueOf(50000)) >= 0) {
                    casinoIndex++;
                }
                if (casinoIndex > game.getCasinoMap().size()) {
                    break;
                }
                Cash cash = game.getCashStack().pop();
                game.getCasinoMap().get(casinoIndex).getCashQueue().add(cash);
            }
            game.setRoundNumber(1);
        }
        // Play
        notifyAllTerminals(gameTerminal, "局数[" + game.getGameNumber() + "] 轮数[" + game.getRoundNumber() + "] 玩家["
                + game.getPlayerNumber() + "]"
                + playerService.getPlayerInfoMap().get(gameTerminal.getUserCode()).getNickname());
        if (GamePalConstants.GAME_PLAYER_STATUS_PLAYING == gameTerminal.getStatus()
                && game.getPlayerNumber() == gameTerminal.getPlayer().getPlayerNo()) {
            LasVegasPlayer player = (LasVegasPlayer) gameTerminal.getPlayer();
            if (player.getDiceQueue().isEmpty()) {
                notifyAllTerminals(gameTerminal, "玩家[" + game.getPlayerNumber() + "]"
                        + game.getPlayerMap().get(game.getPlayerNumber()).getName() + "没有骰子，跳过。");
                LasVegasGameUtil.updatePlayerNo(game);
            } else {
                LasVegasGameUtil.rollDiceQueue(player);
                gameTerminal.addOutput(LasVegasGameUtil.printDiceQueue(player));
                gameTerminal.addOutput("输入编号(1-6)进行投资：");
            }
        } else {
            notifyAllTerminals(gameTerminal, "等待玩家[" + game.getPlayerNumber() + "]"
                    + game.getPlayerMap().get(game.getPlayerNumber()).getName() + "操作。");
        }

        // Communicate
        JSONObject gameOutput = new JSONObject();
        gameOutput.put("terminalType", GamePalConstants.TERMINAL_TYPE_GAME);
        gameOutput.put("gameType", GamePalConstants.GAME_TYPE_LAS_VEGAS);
        JSONObject players = new JSONObject();
        game.getPlayerMap().entrySet().stream().forEach(entry -> {
            players.put(String.valueOf(entry.getKey()), entry.getValue());
        });
        gameOutput.put("players", players);
        JSONObject casinos = new JSONObject();
        game.getCasinoMap().entrySet().stream().forEach(entry -> {
            casinos.put(String.valueOf(entry.getKey()), entry.getValue());
        });
        gameOutput.put("casinos", casinos);
        gameTerminal.setGameOutput(gameOutput);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity lasVegasInput(GameTerminal gameTerminal, String input) {
        JSONObject rst = ContentUtil.generateRst();
        int inputInt;
        LasVegasGame game = (LasVegasGame) gameTerminal.getGame();
        // Operate
        if (game.getPlayerNumber() != gameTerminal.getPlayer().getPlayerNo()) {
            gameTerminal.addOutput("没有轮到你操作。");
            return ResponseEntity.ok().body(rst.toString());
        }
        if (!ContentUtil.isInteger(input)) {
            gameTerminal.addOutput("你的指令无法被识别。");
            return ResponseEntity.ok().body(rst.toString());
        }
        inputInt = Integer.valueOf(input);
        LasVegasPlayer player = (LasVegasPlayer) gameTerminal.getPlayer();
        int diceNumber = LasVegasGameUtil.getDiceNumber(player, inputInt);
        if (diceNumber == 0) {
            gameTerminal.addOutput("编号[" + inputInt + "]无法进行投资，相同点数的骰子不够。");
        } else {
            int casinoDiceNumber = game.getCasinoMap().get(inputInt).getDiceMap().getOrDefault(player.getPlayerNo(), 0);
            game.getCasinoMap().get(inputInt).getDiceMap().put(player.getPlayerNo(), casinoDiceNumber + diceNumber);
            LasVegasGameUtil.resetDiceQueue(player, player.getDiceQueue().size() - diceNumber);
            LasVegasGameUtil.updatePlayerNo(game);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    private String convertGameType2Name(int gameType) {
        switch (gameType) {
            case GamePalConstants.GAME_TYPE_LAS_VEGAS:
                return "拉斯维加斯";
            default:
                return "不详";
        }
    }

    private void notifyAllTerminals(GameTerminal gameTerminal, String content) {
        gameTerminal.getGame().getPlayerMap().entrySet().stream().forEach(entry -> {
            GameTerminal gameTerminal2 = (GameTerminal) playerService.getTerminalMap().get(entry.getValue().getId());
            gameTerminal2.addOutput(content);
        });
    }
}
