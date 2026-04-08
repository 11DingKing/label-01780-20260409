package com.help.mp.service;

import com.help.mp.common.BizException;
import com.help.mp.entity.TipOrder;
import com.help.mp.mapper.TipOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipOrderServiceTest {

    @Mock
    private TipOrderMapper tipOrderMapper;
    @Mock
    private WechatPayService wechatPayService;
    @Mock
    private RedFlowerService redFlowerService;
    @Mock
    private HelpInteractionService helpInteractionService;

    @InjectMocks
    private TipOrderService tipOrderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tipOrderService, "flowerPerYuan", 1);
    }

    @Test
    void create_amountTooSmall_throws() {
        assertThrows(BizException.class, () -> tipOrderService.create(1L, 1L, 99, "oid"));
    }

    @Test
    void create_amountTooLarge_throws() {
        assertThrows(BizException.class, () -> tipOrderService.create(1L, 1L, 10001, "oid"));
    }

    @Test
    void create_valid_insertsOrder() {
        when(tipOrderMapper.insert((TipOrder) any())).thenAnswer(inv -> {
            TipOrder o = inv.getArgument(0);
            o.setId(10L);
            o.setOrderNo("T123");
            return 1;
        });
        when(wechatPayService.createPayParams(any(), any())).thenReturn(Map.of("paySign", "x"));

        TipOrder order = tipOrderService.create(1L, 1L, 500, "oid");
        assertNotNull(order.getOrderNo());
        assertTrue(order.getOrderNo().startsWith("T"));
        assertEquals(1L, order.getHelpId());
        assertEquals(1L, order.getUserId());
        assertEquals(500, order.getAmountCents());
        assertEquals(0, order.getStatus());
        verify(tipOrderMapper).insert((TipOrder) any());
    }

    @Test
    void onPaySuccess_updatesOrderAndAddsFlower() {
        TipOrder order = new TipOrder();
        order.setId(1L);
        order.setOrderNo("T123");
        order.setHelpId(2L);
        order.setUserId(3L);
        order.setAmountCents(200);
        order.setStatus(0);
        when(tipOrderMapper.selectOne(any())).thenReturn(order);
        when(tipOrderMapper.updateById((TipOrder) any())).thenReturn(1);

        tipOrderService.onPaySuccess("T123", "wx_tx_1");

        verify(tipOrderMapper).updateById((TipOrder) argThat(o -> ((TipOrder) o).getStatus() == 1 && ((TipOrder) o).getWxTransactionId() != null));
        verify(redFlowerService).addFlower(eq(3L), eq("tip"), eq("T123"), eq(2), anyString());
        verify(helpInteractionService).recordTip(eq(2L), eq(3L), eq("T123"), eq(2));
    }

    @Test
    void onPaySuccess_alreadyPaid_skips() {
        TipOrder order = new TipOrder();
        order.setOrderNo("T123");
        order.setStatus(1);
        when(tipOrderMapper.selectOne(any())).thenReturn(order);

        tipOrderService.onPaySuccess("T123", "wx_1");

        verify(redFlowerService, never()).addFlower(any(), any(), any(), anyInt(), any());
    }

    @Test
    void handleNotify_emptyBody_returnsFalse() {
        assertFalse(tipOrderService.handleNotify(null));
        assertFalse(tipOrderService.handleNotify(""));
    }

    @Test
    void handleNotify_hasBody_returnsTrue() {
        assertTrue(tipOrderService.handleNotify("<xml>...</xml>"));
    }
}
