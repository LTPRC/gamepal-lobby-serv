package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.terminal.GameTerminal;
import org.springframework.http.ResponseEntity;

public interface GameService {

    ResponseEntity checkStatus(GameTerminal gameTerminal);

    ResponseEntity input(GameTerminal gameTerminal, String input);
}
