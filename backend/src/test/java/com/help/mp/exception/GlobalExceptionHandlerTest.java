package com.help.mp.exception;

import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBiz_returnsResultWithCodeAndMessage() {
        Result<?> r = handler.handleBiz(new BizException(404, "未找到"));
        assertEquals(404, r.getCode());
        assertEquals("未找到", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void handleBiz_500default() {
        Result<?> r = handler.handleBiz(new BizException("服务器错误"));
        assertEquals(500, r.getCode());
        assertEquals("服务器错误", r.getMessage());
    }
}
