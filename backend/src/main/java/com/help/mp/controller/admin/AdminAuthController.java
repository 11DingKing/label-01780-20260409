package com.help.mp.controller.admin;

import com.help.mp.common.Result;
import com.help.mp.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) return Result.fail(400, "用户名和密码必填");
        String token = adminAuthService.login(username, password);
        return Result.ok(Map.of("token", token));
    }
}
