package com.github.ltprc.gamepal;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ltprc.gamepal.repository.UserInfoRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
class SpringBootProjectApplicationTests {

    @Resource
    private UserInfoRepository userInfoRepository;

    @Test
    public void contextLoads() {
        System.out.println("Hello world!");
        Assert.assertTrue(true);
    }

}
