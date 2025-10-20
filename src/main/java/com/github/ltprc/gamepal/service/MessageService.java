package com.github.ltprc.gamepal.service;

import javax.servlet.http.HttpServletRequest;

import com.github.ltprc.gamepal.model.Message;
import org.springframework.http.ResponseEntity;

public interface MessageService {

    ResponseEntity<String> receiveMessage(HttpServletRequest request);

    ResponseEntity<String> receiveMessage(String userCode, Message message);
}
