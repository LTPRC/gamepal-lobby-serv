package com.github.ltprc.gamepal.service;

import javax.servlet.http.HttpServletRequest;

import com.github.ltprc.gamepal.model.map.world.GameWorld;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<String> registerAccount(HttpServletRequest request);

    ResponseEntity<String> cancelAccount(HttpServletRequest request);

    ResponseEntity<String> login(HttpServletRequest request);

    ResponseEntity<String> logoff(HttpServletRequest request);

    ResponseEntity<String> logoff(String userCode, String token, boolean needToken);

    GameWorld getWorldByUserCode(String userCode);

    void addUserIntoMap(GameWorld world, String userCode);
}
