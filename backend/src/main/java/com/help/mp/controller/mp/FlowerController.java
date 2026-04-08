package com.help.mp.controller.mp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.Result;
import com.help.mp.context.UserContext;
import com.help.mp.entity.RedFlowerLog;
import com.help.mp.entity.User;
import com.help.mp.service.RedFlowerService;
import com.help.mp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mp/flower")
@RequiredArgsConstructor
public class FlowerController {

    private final UserService userService;
    private final RedFlowerService redFlowerService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("redFlowerTotal", user.getRedFlowerTotal() != null ? user.getRedFlowerTotal() : 0);
        data.put("badgeLevel", user.getBadgeLevel() != null ? user.getBadgeLevel() : 0);
        return Result.ok(data);
    }

    @GetMapping("/logs")
    public Result<Page<RedFlowerLog>> logs(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return Result.ok(redFlowerService.listLogs(UserContext.getUserId(), page, size));
    }
}
