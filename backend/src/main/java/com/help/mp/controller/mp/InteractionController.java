package com.help.mp.controller.mp;

import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import com.help.mp.context.UserContext;
import com.help.mp.service.HelpInteractionService;
import com.help.mp.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/mp/interact")
@RequiredArgsConstructor
public class InteractionController {

    private final HelpInteractionService helpInteractionService;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.interact-per-minute:30}")
    private int interactPerMinute;

    @PostMapping("/bless")
    public Result<Void> bless(@RequestParam Long helpId) {
        Long userId = UserContext.getUserId();
        if (!rateLimiter.isAllowed("interact:" + userId, interactPerMinute, Duration.ofMinutes(1))) {
            throw new BizException(429, "操作过于频繁，请稍后再试");
        }
        helpInteractionService.bless(helpId, userId);
        return Result.ok();
    }

    @PostMapping("/share")
    public Result<Void> share(@RequestParam Long helpId) {
        Long userId = UserContext.getUserId();
        if (!rateLimiter.isAllowed("interact:" + userId, interactPerMinute, Duration.ofMinutes(1))) {
            throw new BizException(429, "操作过于频繁，请稍后再试");
        }
        helpInteractionService.share(helpId, userId);
        return Result.ok();
    }
}
