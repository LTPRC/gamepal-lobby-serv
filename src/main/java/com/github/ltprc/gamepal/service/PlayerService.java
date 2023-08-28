package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.world.Drop;
import com.github.ltprc.gamepal.model.world.PlayerInfo;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PlayerService {

    ResponseEntity setRelation(HttpServletRequest request);

    ResponseEntity getRelation(HttpServletRequest request);

    ResponseEntity setDrop(HttpServletRequest request);

    ResponseEntity getDrop(HttpServletRequest request);

    ResponseEntity setPlayerInfo(HttpServletRequest request);

    ResponseEntity setPlayerInfoByEntities(HttpServletRequest request);

    ResponseEntity getPlayerInfo(HttpServletRequest request);

    Map<String, PlayerInfo> getPlayerInfoMap();

    Map<String, Drop> getDropMap();

    Map<String, Integer> getRelationMapByUserCode(String userCode);
}
