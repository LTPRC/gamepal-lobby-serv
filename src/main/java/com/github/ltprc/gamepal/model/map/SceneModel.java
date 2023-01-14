package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneModel {
    private Integer center;
    private Integer east;
    private Integer northeast;
    private Integer north;
    private Integer northwest;
    private Integer west;
    private Integer southwest;
    private Integer south;
    private Integer southeast;
    public void setSceneNo(int index, Integer value) {
        switch (index) {
            case 0:
                northwest = value;
                break;
            case 1:
                north = value;
                break;
            case 2:
                northeast = value;
                break;
            case 3:
                west = value;
                break;
            case 4:
                center = value;
                break;
            case 5:
                east = value;
                break;
            case 6:
                southwest = value;
                break;
            case 7:
                south = value;
                break;
            case 8:
                southeast = value;
                break;
            default:
                center = value;
        }
    }
    public Integer getSceneNo(int index) {
        switch(index) {
            case 0:
                return northwest;
            case 1:
                return north;
            case 2:
                return northeast;
            case 3:
                return west;
            case 4:
                return center;
            case 5:
                return east;
            case 6:
                return southwest;
            case 7:
                return south;
            case 8:
                return southeast;
            default:
                return center;
        }
    }
}
