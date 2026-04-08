package com.help.mp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.entity.RedFlowerLog;
import com.help.mp.entity.User;
import com.help.mp.mapper.RedFlowerLogMapper;
import com.help.mp.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedFlowerServiceTest {

    @Mock
    private RedFlowerLogMapper redFlowerLogMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private RedFlowerService redFlowerService;

    @Test
    void addFlower_zeroOrNegative_doesNothing() {
        redFlowerService.addFlower(1L, "bless", "1", 0, "x");
        redFlowerService.addFlower(1L, "bless", "1", -1, "x");
        verify(userMapper, never()).addRedFlower(any(), anyInt());
        verify(redFlowerLogMapper, never()).insert((RedFlowerLog) any());
    }

    @Test
    void addFlower_userNotFound_doesNothing() {
        when(userMapper.selectById(999L)).thenReturn(null);
        redFlowerService.addFlower(999L, "bless", "1", 3, "x");
        verify(userMapper, never()).addRedFlower(any(), anyInt());
        verify(redFlowerLogMapper, never()).insert((RedFlowerLog) any());
    }

    @Test
    void addFlower_positive_addsLogAndUpdatesUser() {
        User u = new User();
        u.setId(1L);
        u.setRedFlowerTotal(5);
        when(userMapper.selectById(1L)).thenReturn(u);
        when(userMapper.addRedFlower(eq(1L), eq(3))).thenReturn(1);
        when(redFlowerLogMapper.insert((RedFlowerLog) any())).thenReturn(1);

        redFlowerService.addFlower(1L, "bless", "helpId", 3, "祝福");

        verify(userMapper).addRedFlower(1L, 3);
        ArgumentCaptor<RedFlowerLog> cap = ArgumentCaptor.forClass(RedFlowerLog.class);
        verify(redFlowerLogMapper).insert(cap.capture());
        RedFlowerLog log = cap.getValue();
        assertEquals(1L, log.getUserId());
        assertEquals("bless", log.getBizType());
        assertEquals("helpId", log.getBizId());
        assertEquals(3, log.getAmount());
        assertEquals(8, log.getBalanceAfter());
        verify(userService).updateBadgeLevelIfNeeded(1L, 8);
    }

    @Test
    void listLogs_returnsPage() {
        Page<RedFlowerLog> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(redFlowerLogMapper.selectPage(any(), any())).thenReturn(page);

        Page<RedFlowerLog> result = redFlowerService.listLogs(1L, 1, 20);
        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
    }
}
