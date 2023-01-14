package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.service.ContactService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class ContactServiceImpl implements ContactService {
    private Map<String, Set<String>> contactMap = new ConcurrentHashMap<>(); // userCode, message queue

    @Override
    public ResponseEntity AddContact(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        String nextUserCode = req.getString("nextUserCode");
        if (!contactMap.containsKey(userCode)) {
            contactMap.put(userCode, new ConcurrentSkipListSet<>());
        }
        contactMap.get(userCode).add(nextUserCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity RemoveContact(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        String nextUserCode = req.getString("nextUserCode");
        if (!contactMap.containsKey(userCode) || !contactMap.get(userCode).contains(nextUserCode)) {
            ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1011));
        }
        contactMap.get(userCode).remove(nextUserCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity GetAllContacts(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        JSONArray contacts = new JSONArray();
        if (contactMap.containsKey(userCode)) {
            contacts.addAll(contactMap.get(userCode));
        }
        rst.put("contacts", contacts);
        return ResponseEntity.ok().body(rst.toString());
    }
}
