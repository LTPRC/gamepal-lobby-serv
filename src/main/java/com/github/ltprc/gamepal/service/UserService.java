package com.github.ltprc.gamepal.service;

import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<String> registerAccount(HttpServletRequest request);

    ResponseEntity<String> cancelAccount(HttpServletRequest request);

    ResponseEntity<String> login(HttpServletRequest request);

    ResponseEntity<String> logoff(HttpServletRequest request);

    void onOpen(Session session, String userCode);

    void onClose(String userCode);

    String getTokenByUserCode(String userCode);

    Long getOnlineTimestampByUserCode(String userCode);

    Session getSessionByUserCode(String userCode);

    Set<Entry<String, Session>> getSessionEntrySet();

    String updateTokenByUserCode(String userCode);
}
