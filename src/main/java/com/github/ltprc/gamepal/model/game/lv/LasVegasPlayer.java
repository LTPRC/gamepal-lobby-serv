package com.github.ltprc.gamepal.model.game.lv;

import com.github.ltprc.gamepal.model.game.Dice;
import com.github.ltprc.gamepal.model.game.Player;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
public class LasVegasPlayer extends Player {
    private Queue<Dice> diceQueue = new LinkedList<>();
    private BigDecimal money;
}
