package com.github.ltprc.gamepal.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface ContactService {

    ResponseEntity AddContact(HttpServletRequest request);

    ResponseEntity RemoveContact(HttpServletRequest request);

    ResponseEntity GetAllContacts(HttpServletRequest request);
}
