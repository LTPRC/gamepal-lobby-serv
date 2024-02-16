package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.terminal.GameTerminal;
import org.springframework.http.ResponseEntity;

public interface StateMachineService {

    ResponseEntity gameTerminalState(GameTerminal gameTerminal);

    ResponseEntity gameTerminalInput(GameTerminal gameTerminal, String input);

    ResponseEntity lasVegasState(GameTerminal gameTerminal);

    ResponseEntity lasVegasInput(GameTerminal gameTerminal, String input);
}
