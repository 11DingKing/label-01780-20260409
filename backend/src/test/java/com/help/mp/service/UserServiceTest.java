package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.common.BizException;
import com.help.mp.entity.User;
import com.help.mp.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setOpenid("oid1");
        existingUser.setNickName("测试");
        existingUser.setRedFlowerTotal(10);
        existingUser.setBadgeLevel(1);
    }

    @Test
    void getByOpenid_found_returnsUser() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        User u = userService.getByOpenid("oid1");
        assertNotNull(u);
        assertEquals(1L, u.getId());
        assertEquals("oid1", u.getOpenid());
    }

    @Test
    void getByOpenid_notFound_returnsNull() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        assertNull(userService.getByOpenid("unknown"));
    }

    @Test
    void getOrCreate_existingUser_updatesAndReturns() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        when(userMapper.updateById((User) any())).thenReturn(1);

        User u = userService.getOrCreate("oid1", "newSession", "union1");
        assertNotNull(u);
        assertEquals(1L, u.getId());
        verify(userMapper).updateById(existingUser);
        verify(userMapper, never()).insert((User) any());
    }

    @Test
    void getOrCreate_newUser_insertsAndReturns() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert((User) any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return 1;
        });

        User u = userService.getOrCreate("newOid", "sk", null);
        assertNotNull(u);
        assertEquals(2L, u.getId());
        assertEquals("newOid", u.getOpenid());
        assertEquals(0, u.getRedFlowerTotal());
        assertEquals(0, u.getBadgeLevel());
        verify(userMapper).insert((User) any());
    }

    @Test
    void getById_found_returnsUser() {
        when(userMapper.selectById(1L)).thenReturn(existingUser);
        User u = userService.getById(1L);
        assertEquals(1L, u.getId());
    }

    @Test
    void getById_notFound_throwsBizException() {
        when(userMapper.selectById(999L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> userService.getById(999L));
        assertEquals(404, ex.getCode());
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void updateProfile_updatesNonNullFields() {
        when(userMapper.selectById(1L)).thenReturn(existingUser);
        when(userMapper.updateById((User) any())).thenReturn(1);

        userService.updateProfile(1L, "新昵称", null, null, null);

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(cap.capture());
        assertEquals("新昵称", cap.getValue().getNickName());
    }
}
