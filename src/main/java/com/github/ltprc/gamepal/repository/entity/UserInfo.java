package com.github.ltprc.gamepal.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "userCode", nullable = false)
    private String userCode;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    /**
     * 0-normal 1-disabled
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    @Column(name = "timeCreated", nullable = false)
    private String timeCreated;
    @Column(name = "timeUpdated", nullable = false)
    private String timeUpdated;

    public UserInfo() {
        super();
    }

    public UserInfo(Long id, String userCode, String username, String password, Integer status, String timeCreated,
            String timeUpdated) {
        super();
        this.id = id;
        this.userCode = userCode;
        this.username = username;
        this.password = password;
        this.status = status;
        this.timeCreated = timeCreated;
        this.timeUpdated = timeUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getTimeUpdated() {
        return timeUpdated;
    }

    public void setTimeUpdated(String timeUpdated) {
        this.timeUpdated = timeUpdated;
    }
}
