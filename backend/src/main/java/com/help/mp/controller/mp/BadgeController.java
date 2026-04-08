package com.help.mp.controller.mp;

import com.help.mp.common.Result;
import com.help.mp.context.UserContext;
import com.help.mp.entity.Badge;
import com.help.mp.entity.UserBadge;
import com.help.mp.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mp/badge")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/list")
    public Result<List<Badge>> list() {
        return Result.ok(badgeService.listAll());
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> my() {
        Long userId = UserContext.getUserId();
        List<Badge> all = badgeService.listAll();
        List<UserBadge> my = badgeService.myBadges(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("badges", all);
        data.put("myBadges", my);
        return Result.ok(data);
    }
}
