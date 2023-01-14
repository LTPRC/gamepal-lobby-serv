package com.github.ltprc.gamepal.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface PlayerService {

    ResponseEntity setRelation(HttpServletRequest request);

    ResponseEntity getRelation(HttpServletRequest request);

    ResponseEntity setDrop(HttpServletRequest request);

    ResponseEntity getDrop(HttpServletRequest request);

    ResponseEntity setBasicInfo(HttpServletRequest request);

    ResponseEntity getBasicInfo(HttpServletRequest request);

    ResponseEntity setPlayerInfo(HttpServletRequest request);

    ResponseEntity getPlayerInfo(HttpServletRequest request);
}
