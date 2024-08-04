package com.github.ltprc.gamepal.manager;

import org.springframework.http.ResponseEntity;

public interface CommandManager {

    ResponseEntity<String> useCommand(String userCode, String commandContent);
}
