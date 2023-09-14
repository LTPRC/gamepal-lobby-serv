package com.github.ltprc.gamepal.model.terminal;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.ContentUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameTerminal implements Terminal {

    private String id;
    private String userCode;
    private int status; // 0-start 1-show game list 2-in game
    private List<String> outputs;
    private GameWorld world;
    private Game game;

    @Autowired
    private WorldService worldService;

    @Autowired
    private PlayerService playerService;

    public GameTerminal(GameWorld world) {
        this.world = world;
    }

    @Override
    public void checkStatus() {
        if (null == world) {
            outputs.add("用户信息错误，桌游载入失败。");
            return;
        }
        switch (status) {
            case GamePalConstants.GAME_STATUS_START:
                outputs.add("欢迎开启桌游。");
                break;
            case GamePalConstants.GAME_STATUS_SEEKING_GAME:
                outputs.add("以下是当前正在进行的游戏列表：");
                world.getGameMap().entrySet().stream().forEach(entry -> {
                    outputs.add("[" + entry.getKey() + "] "
                            + convertGameType2Name(entry.getValue().getGameType()) + ": "
                            + entry.getValue().getPlayerMap().size() + " / "
                            + entry.getValue().getMinPlayerNum() + "~" + entry.getValue().getMaxPlayerNum());
                });
                outputs.add("输入编号进入对应桌游局：");
                break;
            case GamePalConstants.GAME_STATUS_WAITING_FOR_PLAYERS:
            case GamePalConstants.GAME_STATUS_RUNNING:
                if (null == game) {
                    outputs.add("用户信息错误，桌游局载入失败。");
                    return;
                }
                checkStatusLasVegas();
                break;
        }
    }

    @Override
    public void input(String input) {
        switch (status) {
            case GamePalConstants.GAME_STATUS_START:
                status = GamePalConstants.GAME_STATUS_SEEKING_GAME;
                break;
            case GamePalConstants.GAME_STATUS_SEEKING_GAME:
                if (!ContentUtil.isInteger(input)) {
                    outputs.add("你的指令无法被识别。");
                    break;
                }
                int inputInt = Integer.valueOf(input);
                if (world.getGameMap().containsKey(inputInt)) {
                    game = world.getGameMap().get(inputInt);
                    outputs.add("已加入编号" + inputInt + "的桌游局。");
                    outputs.add("桌游的类型为" + convertGameType2Name(game.getGameType()) + "。");
                    switch (game.getGameType()) {
                        case GamePalConstants.GAME_TYPE_LAS_VEGAS:
                            status = GamePalConstants.GAME_STATUS_WAITING_FOR_PLAYERS;
                            game.getStandbySet().add(id);
                            break;
                        default:
                            outputs.add("目前不支持这种游戏类型。");
                            break;
                    }
                } else {
                    outputs.add("无法加入编号" + inputInt + "的桌游局，桌游局不存在。");
                }
                break;
            case GamePalConstants.GAME_STATUS_WAITING_FOR_PLAYERS:
            case GamePalConstants.GAME_STATUS_RUNNING:
                inputLasVegas(input);
                break;
        }
        checkStatus();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUserCode() {
        return userCode;
    }

    @Override
    public List<String> flushOutput() {
        List<String> newOutputs = new ArrayList<>();
        newOutputs.addAll(outputs);
        outputs.clear();
        return newOutputs;
    }

    @Override
    public JSONObject returnObject() {
        JSONObject rst = new JSONObject();
        rst.put("game", game);
        return rst;
    }

    private String convertGameType2Name(int gameType) {
        switch (gameType) {
            case GamePalConstants.GAME_TYPE_LAS_VEGAS:
                return "拉斯维加斯";
            default:
                return "不详";
        }
    }

    public void checkStatusLasVegas() {
        switch (game.getGameStatus()) {
            case GamePalConstants.GAME_STATUS_WAITING_FOR_PLAYERS:
                outputs.add("玩家如下：");
                game.getPlayerMap().entrySet().stream().forEach(entry -> {
                    outputs.add("[" + entry.getKey() + "] " + playerService.getPlayerInfoMap().get(entry.getValue().getId()).getNickname());
                });
                outputs.add("游客如下：");
                game.getStandbySet().stream().forEach(standBy -> {
                    outputs.add("- " + playerService.getPlayerInfoMap().get(standBy).getNickname());
                });
                if (game.getStandbySet().contains(id)) {
                    outputs.add("输入：0-退出房间 1-准备游戏");
                } else {
                    outputs.add("输入：0-取消准备 1-启动游戏");
                }
            case GamePalConstants.GAME_STATUS_RUNNING:
        }
    }

    public void inputLasVegas(String input) {
    }
}
