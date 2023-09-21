package com.github.ltprc.gamepal.terminal;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.game.Player;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameTerminal implements Terminal {

    private String id;
    private String userCode;
    private int status;
    private List<String> outputs;
    private JSONObject gameOutput;
    private GameWorld world;
    private Game game;
    private Player player;

    @Autowired
    private PlayerService playerService;

    public GameTerminal(GameWorld world) {
        this.world = world;
    }

    @Override
    public int getType() {
        return GamePalConstants.TERMINAL_TYPE_GAME;
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
    public void addOutput(String output) {
        outputs.add(output);
    }

    @Override
    public List<String> flushOutput() {
        List<String> newOutputs = new ArrayList<>();
        newOutputs.addAll(outputs);
        outputs.clear();
        return newOutputs;
    }
}
