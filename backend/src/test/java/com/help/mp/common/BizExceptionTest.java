package com.help.mp.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BizExceptionTest {

    @Test
    void constructor_messageOnly_defaultCode500() {
        BizException e = new BizException("错误信息");
        assertEquals(500, e.getCode());
        assertEquals("错误信息", e.getMessage());
    }

    @Test
    void constructor_codeAndMessage() {
        BizException e = new BizException(404, "未找到");
        assertEquals(404, e.getCode());
        assertEquals("未找到", e.getMessage());
    }
}
