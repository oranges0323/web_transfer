package com.oranges.fileuploaderbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
@ActiveProfiles("prod")
@SpringBootTest
class FileInfoUploaderBackendApplicationTests {
    @Resource
    private RedisTemplate redisTemplate;
    @Test
    void contextLoads() {
    }
    @Test
    void redisDemo(){
        redisTemplate.opsForValue().set("name","zhangsan");
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }

}
