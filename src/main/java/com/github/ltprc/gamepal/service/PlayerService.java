package com.github.ltprc.gamepal.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface PlayerService {

    ResponseEntity setRelation(HttpServletRequest request);
}
