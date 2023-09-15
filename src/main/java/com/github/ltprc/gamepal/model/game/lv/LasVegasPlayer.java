package com.github.ltprc.gamepal.model.game.lv;

import com.github.ltprc.gamepal.model.game.Cash;
import com.github.ltprc.gamepal.model.game.Player;
import lombok.Data;

import java.util.Stack;

@Data
public class LasVegasPlayer extends Player {
    private int diceNum;
    private Stack<Cash> cashStack = new Stack<>();
}
