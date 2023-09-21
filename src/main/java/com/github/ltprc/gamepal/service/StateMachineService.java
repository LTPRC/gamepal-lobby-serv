package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.terminal.Terminal;
import org.springframework.http.ResponseEntity;

public interface StateMachineService {

    ResponseEntity state(Terminal terminal);

    ResponseEntity input(Terminal terminal, String input);
}
