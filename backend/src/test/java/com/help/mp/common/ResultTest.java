package com.help.mp.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void ok_withData() {
        Result<String> r = Result.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals("hello", r.getData());
    }

    @Test
    void ok_withoutData() {
        Result<Void> r = Result.ok();
        assertEquals(200, r.getCode());
        assertNull(r.getData());
    }

    @Test
    void fail_withCode() {
        Result<?> r = Result.fail(404, "未找到");
        assertEquals(404, r.getCode());
        assertEquals("未找到", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void fail_messageOnly() {
        Result<?> r = Result.fail("错误");
        assertEquals(500, r.getCode());
        assertEquals("错误", r.getMessage());
    }
}
