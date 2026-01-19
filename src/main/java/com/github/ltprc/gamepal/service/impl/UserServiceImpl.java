package com.github.ltprc.gamepal.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.ItemConstants;
import com.github.ltprc.gamepal.config.MissionConstants;
import com.github.ltprc.gamepal.manager.ItemManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.QwenResponse;
import com.github.ltprc.gamepal.model.creature.MissionInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WebService;
import com.github.ltprc.gamepal.service.WorldService;
import org.apache.commons.lang3.StringUtils;
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
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;

@Service
public class UserServiceImpl implements UserService {

    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private WebService webService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private ItemManager itemManager;

    private Map<String, String> userWorldMap = new LinkedHashMap<>(); // userCode, worldId

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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
        String worldId = req.getString("worldId");
        if (StringUtils.isBlank(worldId)) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        List<UserInfo> userInfoList = userInfoRepository.queryUserInfoByUsernameAndPassword(username, password);
        if (userInfoList.isEmpty()) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1005));
        }
        String userCode = userInfoList.get(0).getUserCode();
        GameWorld world = worldService.getWorldMap().get(worldId);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        if (!world.getCreatureMap().containsKey(userCode)) {
            // Initialize creature info
            world.getCreatureMap().put(userCode, npcManager.createCreature(world, GamePalConstants.PLAYER_TYPE_HUMAN,
                    CreatureConstants.CREATURE_TYPE_HUMAN, userCode));
        }
        Block player = world.getCreatureMap().get(userCode);
        npcManager.putCreature(world, userCode, player.getWorldCoordinate());
        if (world.getPlayerInfoMap().get(userCode).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_INIT) {
            // Initialize items
            worldService.getItemMap().keySet()
                    .forEach(itemNo -> {
                        switch (itemNo.charAt(0)) {
                            case ItemConstants.ITEM_CHARACTER_TOOL:
                            case ItemConstants.ITEM_CHARACTER_OUTFIT:
                                itemManager.getItem(world, userCode, itemNo, 1);
                                break;
                            case ItemConstants.ITEM_CHARACTER_CONSUMABLE:
                                itemManager.getItem(world, userCode, itemNo, 5);
                                break;
                            case ItemConstants.ITEM_CHARACTER_AMMO:
                                itemManager.getItem(world, userCode, itemNo, 10);
                                break;
                            default:
                                break;
                        }
                    });
            // Initialize missions
            QwenResponse qwenResponse = webService.callQwenApi(
                    "qwen-plus", "以JSON数组的结构生成随机1到5个字符串，内容是编造的游戏任务条目。");
            List<MissionInfo> missions = JSON.parseArray(qwenResponse.getOutput().getText()).toJavaList(String.class)
                    .stream()
                    .map(content -> new MissionInfo(MissionConstants.MISSION_STATUS_INITIATED, content))
                    .collect(Collectors.toList());
            world.getPlayerInfoMap().get(userCode).getMissions().addAll(missions);
        }
        // Update online token
        String token = UUID.randomUUID().toString();
        world.getTokenMap().put(userCode, token);
        rst.put("userCode", userCode);
        rst.put("token", world.getTokenMap().get(userCode));
        return ResponseEntity.ok().body(rst.toString());
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
    @Transactional
    public ResponseEntity<String> logoff(String userCode, String token, boolean needToken) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1018));
        }
        if (needToken && !token.equals(world.getTokenMap().get(userCode))) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1006));
        }
        worldService.registerOffline(world, userCode);
        world.getMessageMap().remove(userCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> getWorldNames(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONArray worldNames = new JSONArray();
        worldService.getWorldMap().values().stream().map(world -> {
            JSONObject worldName = new JSONObject();
            worldName.put("id", world.getId());
            worldName.put("name", world.getName());
            return worldName;
        }).forEach(worldNames::add);
        rst.put("worldNames", worldNames);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public GameWorld getWorldByUserCode(String userCode) {
        GameWorld world = worldService.getWorldMap().get(userWorldMap.get(userCode));
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016 + "userCode: " + userCode);
        }
        return world;
    }

    @Override
    public void addUserIntoWorldMap(String userCode, String worldId) {
        userWorldMap.put(userCode, worldId);
    }
}
