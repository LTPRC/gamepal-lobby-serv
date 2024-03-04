package com.github.ltprc.gamepal.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WebSocketService;
import com.github.ltprc.gamepal.service.WorldService;
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

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    private Map<String, GameWorld> userWorldMap = new LinkedHashMap<>(); // userCode, world

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
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
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
        String worldCode = req.getString("worldCode");
        List<UserInfo> userInfoList = userInfoRepository.queryUserInfoByUsernameAndPassword(username, password);
        if (userInfoList.isEmpty()) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1005));
        }
        String userCode = userInfoList.get(0).getUserCode();
        GameWorld world = worldService.getWorldMap().get(worldCode);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        userWorldMap.put(userCode, world);
        // Update online token
        String token = UUID.randomUUID().toString();
        world.getTokenMap().put(userCode, token);
        // Update online record
        world.getOnlineMap().remove(userCode);
        world.getOnlineMap().put(userCode, Instant.now().getEpochSecond());
        if (!playerService.getPlayerInfoMap().containsKey(userCode)) {
            PlayerInfo playerInfo = new PlayerInfo();
            initiatePlayerInfo(playerInfo);
            playerInfo.setId(userCode);
            playerInfo.setCode("");
            playerService.getPlayerInfoMap().put(userCode, playerInfo);
        }
        rst.put("userCode", userCode);
        rst.put("token", world.getTokenMap().get(userCode));
        return ResponseEntity.ok().body(rst.toString());
    }

    private void initiatePlayerInfo(PlayerInfo playerInfo) {
        playerInfo.setPlayerType(0);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        playerInfo.setRegionNo(1);
        playerInfo.setSceneCoordinate(new IntegerCoordinate(0, 0));
        playerInfo.setCoordinate(new Coordinate(new BigDecimal(5), new BigDecimal(5)));
        playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        playerInfo.setFaceDirection(BigDecimal.ZERO);
        playerInfo.setAvatar("1");
        playerInfo.setFirstName("克强");
        playerInfo.setLastName("曾");
        playerInfo.setNickname("大曾");
        playerInfo.setNameColor("#990000");
        playerInfo.setCreature("1");
        playerInfo.setGender("2");
        playerInfo.setSkinColor("3");
        playerInfo.setHairstyle("2");
        playerInfo.setHairColor("2");
        playerInfo.setEyes("2");
        playerInfo.setMaxSpeed(BigDecimal.valueOf(0.1));
        playerInfo.setAcceleration(BigDecimal.valueOf(0.01));
        playerInfo.setHpMax(1000);
        playerInfo.setHp(playerInfo.getHpMax() / 2);
        playerInfo.setVpMax(1000);
        playerInfo.setVp(playerInfo.getVpMax() / 2);
        playerInfo.setHungerMax(1000);
        playerInfo.setHunger(playerInfo.getHungerMax() / 2);
        playerInfo.setThirstMax(1000);
        playerInfo.setThirst(playerInfo.getThirstMax() / 2);
        playerInfo.setLevel(1);
        playerInfo.setExp(0);
        playerInfo.setExpMax(100);
        playerInfo.setMoney(1);
        playerInfo.setCapacity(new BigDecimal(0));
        playerInfo.setCapacityMax(new BigDecimal(500));
        playerInfo.setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
        int[][] skill = new int[4][4];
        skill[0] = new int[]{GamePalConstants.SKILL_CODE_SHOOT, GamePalConstants.SKILL_MODE_SEMI_AUTO, 0,
                1 * GamePalConstants.FRAME_PER_SECOND};
        skill[1] = new int[]{GamePalConstants.SKILL_CODE_HIT, GamePalConstants.SKILL_MODE_AUTO, 0, 5};
        skill[2] = new int[]{GamePalConstants.SKILL_CODE_BLOCK, GamePalConstants.SKILL_MODE_SEMI_AUTO, 0,
                GamePalConstants.SKILL_DEFAULT_TIME};
        skill[3] = new int[]{GamePalConstants.SKILL_CODE_HEAL, GamePalConstants.SKILL_MODE_SEMI_AUTO, 0,
                GamePalConstants.SKILL_DEFAULT_TIME};
        playerInfo.setSkill(skill);
    }

    @Override
    public ResponseEntity<String> logoff(HttpServletRequest request) {
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        String token = req.getString("token");
        return logoff(userCode, token, true);
    }

    @Override
    public ResponseEntity<String> logoff(String userCode, String token, boolean needToken) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1018));
        }
        if (!needToken || token.equals(world.getTokenMap().get(userCode))) {
            world.getTokenMap().remove(userCode);
            world.getOnlineMap().remove(userCode);
            world.getSessionMap().remove(userCode);
            playerService.getPlayerInfoMap().remove(userCode);
            webSocketService.onClose(userCode);
        } else {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1006));
        }
        userWorldMap.remove(userCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public GameWorld getWorldByUserCode(String userCode) {
        return userWorldMap.get(userCode);
    }
}
