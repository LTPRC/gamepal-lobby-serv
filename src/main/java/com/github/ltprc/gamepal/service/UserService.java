package com.github.ltprc.gamepal.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<String> registerAccount(HttpServletRequest request);

    ResponseEntity<String> cancelAccount(HttpServletRequest request);

    ResponseEntity<String> login(HttpServletRequest request);

    ResponseEntity<String> logoff(String userCode, String token);

    String getTokenByUserCode(String userCode);

    Long getOnlineTimestampByUserCode(String userCode);

    Session getSessionByUserCode(String userCode);

    Map<String, Session> getSessionMap();

    String updateTokenByUserCode(String userCode);

    Map<String, String> getTokenMap();

    Map<String, Long> getOnlineMap();

    Queue<String> getOnlineQueue();
}
