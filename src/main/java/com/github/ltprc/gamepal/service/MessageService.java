package com.github.ltprc.gamepal.service;

import javax.servlet.http.HttpServletRequest;

import com.github.ltprc.gamepal.model.Message;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Queue;

public interface MessageService {

    ResponseEntity sendMessage(HttpServletRequest request);

    ResponseEntity sendMessage(String userCode, Message message);
}
