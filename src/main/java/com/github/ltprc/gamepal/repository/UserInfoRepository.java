package com.github.ltprc.gamepal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.ltprc.gamepal.repository.entity.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    @Query("select new UserInfo(id, userCode, username, password, status, timeCreated, timeUpdated) from UserInfo where id=:id and status=0")
    List<UserInfo> queryUserInfoById(@Param("id") Long id);

    @Query("select new UserInfo(id, userCode, username, password, status, timeCreated, timeUpdated) from UserInfo where userCode=:userCode and status=0")
    List<UserInfo> queryUserInfoByUserCode(@Param("userCode") String userCode);

    @Query("select new UserInfo(id, userCode, username, password, status, timeCreated, timeUpdated) from UserInfo where username=:username and status=0")
    List<UserInfo> queryUserInfoByUsername(@Param("username") String username);

    @Query("select new UserInfo(id, userCode, username, password, status, timeCreated, timeUpdated) from UserInfo where username=:username and password=:password and status=0")
    List<UserInfo> queryUserInfoByUsernameAndPassword(@Param("username") String username,
            @Param("password") String password);

    @Modifying
    @Query("delete from UserInfo where userCode=:userCode")
    void deleteUserInfoByUserCode(@Param("userCode") String userCode);
}
