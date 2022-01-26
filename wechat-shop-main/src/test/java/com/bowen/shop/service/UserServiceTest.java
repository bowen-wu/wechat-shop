package com.bowen.shop.service;

import com.bowen.shop.generate.User;
import com.bowen.shop.generate.UserExample;
import com.bowen.shop.generate.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserMapper userMapper;
    @InjectMocks
    UserService userService;

    @Test
    public void testCreateUserSuccess() {
        User user = userService.createUserIfNotExist("110");
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).insert(argument.capture());
        assertEquals("110", argument.getValue().getTel());
        assertEquals("110", user.getTel());
    }

    @Test
    public void testDuplicateKeyExceptionWhenCreateUser() {
        String tel = "110";
        User user = new User();
        user.setTel(tel);

        Mockito.when(userMapper.insert(any())).thenThrow(new DuplicateKeyException(""));
        Mockito.when(userMapper.selectByExample(any())).thenReturn(Collections.singletonList(user));
        User userIfNotExist = userService.createUserIfNotExist("110");

        ArgumentCaptor<UserExample> argument = ArgumentCaptor.forClass(UserExample.class);
        Mockito.verify(userMapper).selectByExample(argument.capture());

        assertEquals("110", argument.getValue().getOredCriteria().get(0).getCriteria().get(0).getValue());
        assertEquals(user, userIfNotExist);
    }
}
