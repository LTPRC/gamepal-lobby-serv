package com.github.ltprc.gamepal.service;

import javax.servlet.http.HttpServletRequest;

import com.github.ltprc.gamepal.model.Message;
import org.springframework.http.ResponseEntity;

public interface MessageService {

    ResponseEntity sendMessage(HttpServletRequest request);

    ResponseEntity sendMessage(String userCode, Message message);

    ResponseEntity sendMessageToAll(Message message);

    void communicate(String userCode);
}
