package com.github.ltprc.gamepal.repository;

import com.github.ltprc.gamepal.repository.entity.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

    @Query(value="select new UserCharacter(id, userCode, avatar, fullName, firstName, lastName, nickname, nameColor, creature, gender, skinColor, hairstyle, hairColor, eyes, createTime, updateTime) from UserCharacter where userCode=:userCode")
    public List<UserCharacter> queryUserCharacterByUserCode(@Param("userCode") String userCode);

//    @Modifying
//    @Query("update UserCharacter set firstName=:uc.firstName where uuid=:uc.uuid")
//    void updateUserCharacter(@Param("uc") UserCharacter uc);
}
