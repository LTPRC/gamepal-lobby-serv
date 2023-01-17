package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.lobby.BasicInfo;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PlayerService {

    ResponseEntity<String> initUserInfo(HttpServletRequest request);

    ResponseEntity setRelation(HttpServletRequest request);

    ResponseEntity getRelation(HttpServletRequest request);

    ResponseEntity setDrop(HttpServletRequest request);

    ResponseEntity getDrop(HttpServletRequest request);

    ResponseEntity setBasicInfo(HttpServletRequest request);

    ResponseEntity getBasicInfo(HttpServletRequest request);

    ResponseEntity setPlayerInfo(HttpServletRequest request);

    ResponseEntity getPlayerInfo(HttpServletRequest request);

    Map<String, BasicInfo> getBasicInfoMap();

    Map<String, PlayerInfo> getPlayerInfoMap();
}
