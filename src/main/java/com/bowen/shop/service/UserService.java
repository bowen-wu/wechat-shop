package com.bowen.shop.service;

import com.bowen.shop.generate.User;
import com.bowen.shop.generate.UserExample;
import com.bowen.shop.generate.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User createUserIfNotExist(String tel) {
        User user = new User();
        user.setTel(tel);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            UserExample userExample = new UserExample();
            userExample.createCriteria().andTelEqualTo(tel);
            return userMapper.selectByExample(userExample).get(0);
        }
        return user;
    }
}
