package com.github.ltprc.gamepal.repository.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

public class UserCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "userCode", nullable = false)
    private String userCode;
    @Column(name = "avatar")
    private String avatar;
    @Column(name = "fullName")
    private String fullName;
    @Column(name = "firstName")
    private String firstName;
    @Column(name = "lastName")
    private String lastName;
    @Column(name = "nickname")
    private String nickname;
    @Column(name = "nameColor")
    private String nameColor;
    @Column(name = "creature")
    private String creature;
    @Column(name = "gender")
    private String gender;
    @Column(name = "skinColor")
    private String skinColor;
    @Column(name = "hairstyle")
    private String hairstyle;
    @Column(name = "hairColor")
    private String hairColor;
    @Column(name = "eyes")
    private String eyes;
    @Column(name = "createTime", nullable = false)
    private String createTime;
    @Column(name = "updateTime", nullable = false)
    private String updateTime;
}
