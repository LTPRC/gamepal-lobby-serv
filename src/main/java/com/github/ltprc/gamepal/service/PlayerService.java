package com.github.ltprc.gamepal.service;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.map.world.PlayerInfo;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PlayerService {

    ResponseEntity setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute);

    ResponseEntity updateplayerinfobyentities(String userCode, JSONObject req);

    ResponseEntity getPlayerInfo(HttpServletRequest request);

    Map<String, PlayerInfo> getPlayerInfoMap();

    Map<String, Integer> getRelationMapByUserCode(String userCode);
}
