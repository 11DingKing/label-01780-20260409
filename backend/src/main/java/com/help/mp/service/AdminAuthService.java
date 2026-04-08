package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.common.BizException;
import com.help.mp.config.JwtUtil;
import com.help.mp.entity.AdminUser;
import com.help.mp.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public String login(String username, String password) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username));
        if (admin == null || admin.getStatus() != 1) throw new BizException(401, "用户名或密码错误");
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) throw new BizException(401, "用户名或密码错误");
        return jwtUtil.createToken(admin.getId(), "admin_" + admin.getUsername());
    }
}
