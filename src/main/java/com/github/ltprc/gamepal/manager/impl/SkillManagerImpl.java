package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.SkillManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SkillManagerImpl implements SkillManager {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserService userService;

    @Override
    public void updateSkills(PlayerInfo playerInfo) {
        int[][] skills = new int[4][4];
        if (playerInfo.getTools().isEmpty()) {
            skills[0] = new int[]{GamePalConstants.SKILL_CODE_CURSE, GamePalConstants.SKILL_MODE_AUTO, 0,
                    GamePalConstants.SKILL_DEFAULT_TIME};
            skills[1] = new int[]{GamePalConstants.SKILL_CODE_HIT, GamePalConstants.SKILL_MODE_AUTO, 0,
                    GamePalConstants.FRAME_PER_SECOND / 5};
            skills[2] = new int[]{GamePalConstants.SKILL_CODE_KICK, GamePalConstants.SKILL_MODE_SEMI_AUTO, 0,
                    GamePalConstants.SKILL_DEFAULT_TIME * 2};
            skills[3] = new int[]{GamePalConstants.SKILL_CODE_CHEER, GamePalConstants.SKILL_MODE_AUTO, 0,
                    GamePalConstants.SKILL_DEFAULT_TIME};
        }
        playerInfo.setSkill(skills);
    }
}
