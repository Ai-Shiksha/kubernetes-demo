package com.example.kubernetesdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/")
    public String hello() {
        return "Hello from Kubernetes Demo!";
    }

    @GetMapping("/api/test")
    public String test() {
        // Test Redis
        redisTemplate.opsForValue().set("test-key", "test-value");
        String value = redisTemplate.opsForValue().get("test-key");

        return "Application is running! Redis value: " + value;
    }
}
