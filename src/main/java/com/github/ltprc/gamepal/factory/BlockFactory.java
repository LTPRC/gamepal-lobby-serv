package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.model.map.Block;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

@Component
public class BlockFactory {

    public Queue<Block> createRankingQueue() {
        Queue<Block> rankingQueue = new PriorityQueue<>((o1, o2) -> {
            IntegerCoordinate level1 = PlayerUtil.ConvertBlockType2Level(o1.getType());
            IntegerCoordinate level2 = PlayerUtil.ConvertBlockType2Level(o2.getType());
            if (!Objects.equals(level1.getX(), level2.getX())) {
                return level1.getX() - level2.getX();
            }
            // Please use equals() instead of == 24/02/10
            if (!o1.getY().equals(o2.getY())) {
                return o1.getY().compareTo(o2.getY());
            }
            return level1.getY() - level2.getY();
        });
        return rankingQueue;
    }
}
