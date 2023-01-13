package com.github.ltprc.gamepal.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

public interface MessageService {
    void onMessage(String message);

    ResponseEntity sendMessage(HttpServletRequest request);
}
