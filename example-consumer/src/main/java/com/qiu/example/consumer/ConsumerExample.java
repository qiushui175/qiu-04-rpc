package com.qiu.example.consumer;

import com.qiu.example.common.model.User;
import com.qiu.example.common.service.UserService;

/**
 * @author qiu
 * @version 1.0
 * @className ConsumerExample
 * @packageName com.qiu.example.consumer
 * @Description
 * @date 2026/1/9 15:42
 * @since 1.0
 */
public class ConsumerExample {

    public static void main(String[] args) {
        // TODO 实现消费服务的获取
        UserService userService = null;

        User user = userService.getUser(722);
        System.out.println("获取到用户信息: " + user);
    }

}
