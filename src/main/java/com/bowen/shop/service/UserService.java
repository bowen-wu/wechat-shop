package com.bowen.shop.service;

import com.bowen.shop.generate.User;
import com.bowen.shop.generate.UserExample;
import com.bowen.shop.generate.UserMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService {
    private final UserMapper userMapper;

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
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

    public Optional<User> getUserInfoByTel(String tel) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andTelEqualTo(tel);
        return Optional.ofNullable(userMapper.selectByExample(userExample).get(0));
    }
}
