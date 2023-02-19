package com.github.ltprc.gamepal.model.game.lv;

import com.github.ltprc.gamepal.model.game.Cash;
import com.github.ltprc.gamepal.model.game.Game;
import lombok.Data;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class LasVegasGame extends Game {
    private Map<Integer, Casino> casinoMap = new ConcurrentHashMap<>();
    private Stack<Cash> cashStack = new Stack<>();
}
