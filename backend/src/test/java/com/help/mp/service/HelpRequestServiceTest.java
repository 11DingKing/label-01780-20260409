package com.help.mp.service;

import com.help.mp.common.BizException;
import com.help.mp.entity.HelpImage;
import com.help.mp.entity.HelpRequest;
import com.help.mp.mapper.HelpImageMapper;
import com.help.mp.mapper.HelpRequestMapper;
import com.help.mp.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelpRequestServiceTest {

    @Mock private HelpRequestMapper helpRequestMapper;
    @Mock private HelpImageMapper helpImageMapper;
    @Mock private UserMapper userMapper;
    @Mock private WechatOfficialService wechatOfficialService;

    @InjectMocks
    private HelpRequestService helpRequestService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(helpRequestService, "flowerBless", 1);
    }

    @Test
    void publish_invalidUrgency_throws() {
        assertThrows(BizException.class, () ->
                helpRequestService.publish(1L, BigDecimal.ONE, BigDecimal.ONE, null, 0, 0, "内容", null, null));
        assertThrows(BizException.class, () ->
                helpRequestService.publish(1L, BigDecimal.ONE, BigDecimal.ONE, null, 0, 4, "内容", null, null));
    }

    @Test
    void publish_valid_insertsHelpAndImages() {
        doAnswer(inv -> {
            HelpRequest r = inv.getArgument(0);
            r.setId(100L);
            return 1;
        }).when(helpRequestMapper).insert((HelpRequest) any());

        HelpRequest req = helpRequestService.publish(
                1L, new BigDecimal("39.9"), new BigDecimal("116.4"), "北京",
                0, 2, "需要帮助", List.of("http://img1.jpg"), null);

        assertNotNull(req.getId());
        assertEquals(100L, req.getId());
        assertEquals(1L, req.getUserId());
        assertEquals(2, req.getUrgencyLevel());
        assertEquals("需要帮助", req.getContent());
        verify(helpRequestMapper).insert((HelpRequest) any());
        verify(helpImageMapper).insert((HelpImage) any());
    }

    @Test
    void getDetail_notFound_throws() {
        when(helpRequestMapper.selectById(999L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> helpRequestService.getDetail(999L, 1L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void getDetail_deleted_throws() {
        HelpRequest r = new HelpRequest();
        r.setId(1L);
        r.setStatus(0);
        when(helpRequestMapper.selectById(1L)).thenReturn(r);
        BizException ex = assertThrows(BizException.class, () -> helpRequestService.getDetail(1L, 1L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void getDetail_found_returnsRequest() {
        HelpRequest r = new HelpRequest();
        r.setId(1L);
        r.setStatus(1);
        when(helpRequestMapper.selectById(1L)).thenReturn(r);
        HelpRequest out = helpRequestService.getDetail(1L, 1L);
        assertEquals(1L, out.getId());
    }

    @Test
    void close_notOwner_throws403() {
        HelpRequest r = new HelpRequest();
        r.setId(1L);
        r.setUserId(1L);
        when(helpRequestMapper.selectById(1L)).thenReturn(r);
        BizException ex = assertThrows(BizException.class, () -> helpRequestService.close(1L, 2L));
        assertEquals(403, ex.getCode());
    }

    @Test
    void close_owner_updatesStatus() {
        HelpRequest r = new HelpRequest();
        r.setId(1L);
        r.setUserId(1L);
        when(helpRequestMapper.selectById(1L)).thenReturn(r);
        doReturn(1).when(helpRequestMapper).updateById((HelpRequest) any());

        helpRequestService.close(1L, 1L);
        verify(helpRequestMapper).updateById((HelpRequest) argThat(h -> ((HelpRequest) h).getStatus() == 2));
    }

    @Test
    void nearby_nullLat_throws() {
        assertThrows(BizException.class, () -> helpRequestService.nearby(null, BigDecimal.ONE, 10, 20));
    }
}
