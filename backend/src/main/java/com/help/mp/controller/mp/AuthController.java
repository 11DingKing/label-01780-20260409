package com.help.mp.controller.mp;

import com.help.mp.common.Result;
import com.help.mp.config.JwtUtil;
import com.help.mp.context.UserContext;
import com.help.mp.dto.LoginDTO;
import com.help.mp.entity.User;
import com.help.mp.service.UserService;
import com.help.mp.service.WechatMpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mp/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WechatMpService wechatMpService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        @SuppressWarnings("unchecked")
        Map<String, Object> session = (Map<String, Object>) wechatMpService.code2Session(dto.getCode());
        String openid = (String) session.get("openid");
        String sessionKey = (String) session.get("session_key");
        String unionid = session.containsKey("unionid") ? (String) session.get("unionid") : null;
        User user = userService.getOrCreate(openid, sessionKey, unionid);
        String token = jwtUtil.createToken(user.getId(), openid);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickName", user.getNickName());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("redFlowerTotal", user.getRedFlowerTotal());
        data.put("badgeLevel", user.getBadgeLevel());
        return Result.ok(data);
    }

    @GetMapping("/profile")
    public Result<User> profile() {
        Long userId = UserContext.getUserId();
        // 返回解密后的用户信息
        User user = userService.getByIdDecrypted(userId);
        // 不返回敏感内部字段
        user.setSessionKey(null);
        user.setOpenid(null);
        user.setUnionid(null);
        return Result.ok(user);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> body) {
        String nickName = body.containsKey("nickName") ? (String) body.get("nickName") : null;
        String avatarUrl = body.containsKey("avatarUrl") ? (String) body.get("avatarUrl") : null;
        String phoneEnc = body.containsKey("phoneEnc") ? (String) body.get("phoneEnc") : null;
        Integer phoneAnon = body.containsKey("phoneAnon") ? ((Number) body.get("phoneAnon")).intValue() : null;
        userService.updateProfile(UserContext.getUserId(), nickName, avatarUrl, phoneEnc, phoneAnon);
        return Result.ok();
    }
}
