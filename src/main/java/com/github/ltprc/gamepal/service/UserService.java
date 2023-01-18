package com.github.ltprc.gamepal.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<String> registerAccount(HttpServletRequest request);

    ResponseEntity<String> cancelAccount(HttpServletRequest request);

    ResponseEntity<String> login(HttpServletRequest request);

    ResponseEntity<String> logoff(HttpServletRequest request);

    String getTokenByUserCode(String userCode);

    Long getOnlineTimestampByUserCode(String userCode);

    Session getSessionByUserCode(String userCode);

    Map<String, Session> getSessionMap();

    String updateTokenByUserCode(String userCode);
}
