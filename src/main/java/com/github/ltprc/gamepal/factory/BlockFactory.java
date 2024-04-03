package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Block;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.RegionInfo;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

    /**
     *
     * @param regionInfo regionInfo
     * @param playerInfo playerInfo
     * @param eventType eventType
     * @param eventLocationType eventLocationType
     * @return WorldBlock
     */
    public WorldBlock createEventBlock(RegionInfo regionInfo, PlayerInfo playerInfo, int eventType,
                                        int eventLocationType) {
        WorldBlock eventBlock = new WorldBlock();
        eventBlock.setType(eventType);
        eventBlock.setCode(playerInfo.getId());
        eventBlock.setRegionNo(playerInfo.getRegionNo());
        IntegerCoordinate newSceneCoordinate = new IntegerCoordinate();
        newSceneCoordinate.setX(playerInfo.getSceneCoordinate().getX());
        newSceneCoordinate.setY(playerInfo.getSceneCoordinate().getY());
        eventBlock.setSceneCoordinate(newSceneCoordinate);
        // BigDecimal is immutable, no need to copy a new BigDecimal instance 24/04/04
        eventBlock.setCoordinate(new Coordinate(playerInfo.getCoordinate()));
        switch (eventLocationType) {
            case GamePalConstants.EVENT_LOCATION_TYPE_ADJACENT:
                break;
            case GamePalConstants.EVENT_LOCATION_TYPE_MELEE:
                eventBlock.getCoordinate().setX(eventBlock.getCoordinate().getX()
                        .add(BigDecimal.valueOf((Math.random())
                                * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                eventBlock.getCoordinate().setY(eventBlock.getCoordinate().getY()
                        .subtract(BigDecimal.valueOf((Math.random())
                                * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                break;
            case GamePalConstants.EVENT_LOCATION_TYPE_SHOOT:
                eventBlock.getCoordinate().setX(eventBlock.getCoordinate().getX()
                        .add(BigDecimal.valueOf((Math.random()
                                + GamePalConstants.EVENT_MAX_DISTANCE_SHOOT.intValue())
                                * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                eventBlock.getCoordinate().setY(eventBlock.getCoordinate().getY()
                        .subtract(BigDecimal.valueOf((Math.random()
                                + GamePalConstants.EVENT_MAX_DISTANCE_SHOOT.intValue())
                                * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
                break;
        }
        PlayerUtil.fixWorldCoordinate(regionInfo, eventBlock);
        return eventBlock;
    }
}
