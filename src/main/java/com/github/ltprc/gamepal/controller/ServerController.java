package com.github.ltprc.gamepal.controller;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import com.github.ltprc.gamepal.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class ServerController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PlayerService playerService;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<String> registerAccount(HttpServletRequest request) {
        return userService.registerAccount(request);
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public ResponseEntity<String> cancelAccount(HttpServletRequest request) {
        return userService.cancelAccount(request);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<String> login(HttpServletRequest request) {
        return userService.login(request);
    }

    @RequestMapping(value = "/logoff", method = RequestMethod.POST)
    public ResponseEntity<String> logoff(HttpServletRequest request) {
        return userService.logoff(request);
    }

    @RequestMapping(value = "/setplayerinfobyentities", method = RequestMethod.POST)
    public ResponseEntity<String> initUser(HttpServletRequest request) {
        return playerService.setPlayerInfoByEntities(request);
    }
    @RequestMapping(value = "/sendmsg", method = RequestMethod.POST)
    public ResponseEntity<String> sendMessage(HttpServletRequest request) {
        return messageService.sendMessage(request);
    }

    @RequestMapping(value = "/setrelation", method = RequestMethod.POST)
    public ResponseEntity<String> setRelation(HttpServletRequest request) {
        return playerService.setRelation(request);
    }

    @RequestMapping(value = "/getrelation", method = RequestMethod.POST)
    public ResponseEntity<String> getRelation(HttpServletRequest request) {
        return playerService.getRelation(request);
    }

    @RequestMapping(value = "/setdrop", method = RequestMethod.POST)
    public ResponseEntity<String> setDrop(HttpServletRequest request) {
        return playerService.setDrop(request);
    }

    @RequestMapping(value = "/getdrop", method = RequestMethod.POST)
    public ResponseEntity<String> getDrop(HttpServletRequest request) {
        return playerService.getDrop(request);
    }
}
