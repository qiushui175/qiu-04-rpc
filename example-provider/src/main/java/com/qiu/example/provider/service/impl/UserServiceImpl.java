package com.qiu.example.provider.service.impl;

import com.qiu.example.common.model.User;
import com.qiu.example.common.service.UserService;

/**
 * @author qiu
 * @version 1.0
 * @className UserServiceImpl
 * @packageName com.qiu.example.provider.service.impl
 * @Description
 * @date 2026/1/9 15:38
 * @since 1.0
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(Integer id) {
        System.out.println("UserServiceImpl.getUser: " + id);
        return new User("default" + id, id);
    }
}
