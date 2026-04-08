package com.help.mp.service;

import com.help.mp.common.BizException;
import com.help.mp.entity.HelpInteraction;
import com.help.mp.entity.HelpRequest;
import com.help.mp.mapper.HelpInteractionMapper;
import com.help.mp.mapper.HelpRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelpInteractionServiceTest {

    @Mock private HelpInteractionMapper interactionMapper;
    @Mock private HelpRequestMapper helpRequestMapper;
    @Mock private RedFlowerService redFlowerService;

    @InjectMocks
    private HelpInteractionService helpInteractionService;

    private HelpRequest activeHelp;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(helpInteractionService, "flowerBless", 1);
        ReflectionTestUtils.setField(helpInteractionService, "flowerShare", 2);
        activeHelp = new HelpRequest();
        activeHelp.setId(1L);
        activeHelp.setStatus(1);
    }

    @Test
    void bless_helpNotFound_throws() {
        when(helpRequestMapper.selectById(999L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> helpInteractionService.bless(999L, 1L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void bless_helpClosed_throws() {
        activeHelp.setStatus(2);
        when(helpRequestMapper.selectById(1L)).thenReturn(activeHelp);
        BizException ex = assertThrows(BizException.class, () -> helpInteractionService.bless(1L, 1L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void bless_alreadyBlessed_throws() {
        when(helpRequestMapper.selectById(1L)).thenReturn(activeHelp);
        when(interactionMapper.selectCount(any())).thenReturn(1L);
        BizException ex = assertThrows(BizException.class, () -> helpInteractionService.bless(1L, 1L));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("已祝福"));
    }

    @Test
    void bless_success_insertsAndAddsFlower() {
        when(helpRequestMapper.selectById(1L)).thenReturn(activeHelp);
        when(interactionMapper.selectCount(any())).thenReturn(0L);
        doReturn(1).when(interactionMapper).insert((HelpInteraction) any());

        helpInteractionService.bless(1L, 1L);

        verify(interactionMapper).insert((HelpInteraction) any());
        verify(redFlowerService).addFlower(eq(1L), eq("bless"), eq("1"), eq(1), anyString());
    }

    @Test
    void share_alreadyShared_throws() {
        when(helpRequestMapper.selectById(1L)).thenReturn(activeHelp);
        when(interactionMapper.selectCount(any())).thenReturn(1L);
        BizException ex = assertThrows(BizException.class, () -> helpInteractionService.share(1L, 1L));
        assertEquals(400, ex.getCode());
    }

    @Test
    void share_success_insertsAndAddsFlower() {
        when(helpRequestMapper.selectById(1L)).thenReturn(activeHelp);
        when(interactionMapper.selectCount(any())).thenReturn(0L);
        doReturn(1).when(interactionMapper).insert((HelpInteraction) any());

        helpInteractionService.share(1L, 1L);

        verify(redFlowerService).addFlower(eq(1L), eq("share"), eq("1"), eq(2), anyString());
    }
}
