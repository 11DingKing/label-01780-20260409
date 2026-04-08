package com.help.mp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import com.help.mp.entity.User;
import com.help.mp.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping("/list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        Page<User> p = userMapper.selectPage(new Page<>(page, size), null);
        return Result.ok(p);
    }

    /**
     * 封禁/解封用户
     */
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1))
            throw new BizException(400, "状态值无效(0禁用/1正常)");
        User user = userMapper.selectById(id);
        if (user == null) throw new BizException(404, "用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok();
    }
}
