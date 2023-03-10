package com.github.ltprc.gamepal.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.SceneModel;
import com.github.ltprc.gamepal.service.PlayerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.repository.entity.UserInfo;
import com.github.ltprc.gamepal.repository.UserInfoRepository;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;

@Transactional
@Service
public class UserServiceImpl implements UserService {

    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);

    private Map<String, Session> sessionMap = new ConcurrentHashMap<>(); // userId, session
    private Map<String, String> tokenMap = new ConcurrentHashMap<>(); // userId, token
    private Map<String, Long> onlineMap = new ConcurrentHashMap<>(); // userId, timestamp
    private Queue<String> onlineQueue = new PriorityQueue<>((s1, s2) -> {
        long l1 = onlineMap.get(s1);
        long l2 = onlineMap.get(s2);
        if (l1 - l2 < 0) {
            return -1;
        } else if (l1 - l2 > 0) {
            return 1;
        } else {
            return 0;
        }
    }); // userId

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public ResponseEntity<String> registerAccount(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }

        UserInfo userInfo = new UserInfo();
        String userCode = UUID.randomUUID().toString();
        if (!userInfoRepository.queryUserInfoByUserCode(userCode).isEmpty()) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1001));
        }
        String username = req.getString("username");
        userInfo.setUsername(username);
        String password = req.getString("password");
        userInfo.setPassword(password);

        if (!userInfoRepository.queryUserInfoByUsername(username).isEmpty()) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1004));
        }
        userInfo.setUserCode(userCode);
        userInfo.setStatus(0);
        SimpleDateFormat sdf = new SimpleDateFormat();// ???????????????
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a???am/pm?????????
        userInfo.setTimeCreated(sdf.format(new Date()));
        userInfo.setTimeUpdated(userInfo.getTimeCreated());
        userInfoRepository.save(userInfo);

        rst.put("userCode", userCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> cancelAccount(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        userInfoRepository.deleteUserInfoByUserCode(userCode);

        rst.put("userCode", userCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> login(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String username = req.getString("username");
        String password = req.getString("password");
        List<UserInfo> userInfoList = userInfoRepository.queryUserInfoByUsernameAndPassword(username, password);
        if (userInfoList.isEmpty()) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1005));
        }
        String userCode = userInfoList.get(0).getUserCode();
        updateTokenByUserCode(userCode);
        onlineMap.remove(userCode);
        onlineMap.put(userCode, Instant.now().getEpochSecond());
        // Mocked PlayerInfo TBD
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerType(0);
        playerInfo.setUserCode(userCode);
        playerInfo.setWorldNo(0);
        playerInfo.setPosition(new Coordinate(new BigDecimal(5), new BigDecimal(5)));
        playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        playerInfo.setSceneNo(1);
        SceneModel scenes = new SceneModel();
        scenes.setCenter(playerInfo.getSceneNo());
        playerInfo.setScenes(scenes);
        playerInfo.setFaceDirection(BigDecimal.ZERO);
        playerInfo.setAvatar("1");
        playerInfo.setFirstName("??????");
        playerInfo.setLastName("???");
        playerInfo.setNickname("??????");
        playerInfo.setNameColor("#990000");
        playerInfo.setCreature("1");
        playerInfo.setGender("2");
        playerInfo.setSkinColor("3");
        playerInfo.setHairstyle("2");
        playerInfo.setHairColor("2");
        playerInfo.setEyes("2");
        playerInfo.setMaxSpeed(new BigDecimal(0.1));
        playerInfo.setAcceleration(new BigDecimal(0.01));
        playerInfo.setHpMax(1000);
        playerInfo.setHp(playerInfo.getHpMax());
        playerInfo.setVpMax(1000);
        playerInfo.setVp(playerInfo.getVpMax());
        playerInfo.setHungerMax(1000);
        playerInfo.setHunger(playerInfo.getHungerMax());
        playerInfo.setThirstMax(1000);
        playerInfo.setThirst(playerInfo.getThirstMax());
        playerInfo.setLevel(1);
        playerInfo.setExp(0);
        playerInfo.setExpMax(100);
        playerInfo.setMoney(1);
        playerInfo.setCapacity(new BigDecimal(100));
        playerInfo.setCapacityMax(new BigDecimal(500));
        playerService.getPlayerInfoMap().put(userCode, playerInfo);
        rst.put("userCode", userCode);
        rst.put("token", tokenMap.get(userCode));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> logoff(String userCode, String token) {
        JSONObject rst = ContentUtil.generateRst();
        if (token.equals(tokenMap.get(userCode))) {
            tokenMap.remove(userCode);
            onlineMap.remove(userCode);
            playerService.getPlayerInfoMap().remove(userCode);
        } else {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1006));
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public String getTokenByUserCode(String userCode) {
        return tokenMap.getOrDefault(userCode, null);
    }

    @Override
    public Long getOnlineTimestampByUserCode(String userCode) {
        return onlineMap.getOrDefault(userCode, null);
    }

    @Override
    public Session getSessionByUserCode(String userCode) {
        return sessionMap.getOrDefault(userCode, null);
    }

    @Override
    public Map<String, Session> getSessionMap() {
        return sessionMap;
    }

    @Override
    public String updateTokenByUserCode(String userCode) {
        String token = UUID.randomUUID().toString();
        tokenMap.put(userCode, token);
        return token;
    }

    @Override
    public Map<String, String> getTokenMap() {
        return tokenMap;
    }

    @Override
    public Map<String, Long> getOnlineMap() {
        return onlineMap;
    }

    @Override
    public Queue<String> getOnlineQueue() {
        return onlineQueue;
    }

}
