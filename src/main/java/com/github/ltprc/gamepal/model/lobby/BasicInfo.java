package com.github.ltprc.gamepal.model.lobby;

import com.github.ltprc.gamepal.model.map.UserCoordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfo {
    private int playerType; // 0-human player 1-npc
    private String userCode;
    private int worldNo; // TBD
    private UserCoordinate userCoordinate = new UserCoordinate();
    private String avatar;
    private String fullName;
    private String firstName;
    private String lastName;
    private String nickname;
    private String nameColor;
    private String creature;
    private String gender;
    private String skinColor;
    private String hairstyle;
    private String hairColor;
    private String eyes;
    private Set<String> equipments = new ConcurrentSkipListSet<>();
}
