package com.help.mp.controller.mp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import com.help.mp.context.UserContext;
import com.help.mp.dto.PublishHelpDTO;
import com.help.mp.entity.HelpContact;
import com.help.mp.entity.HelpImage;
import com.help.mp.entity.HelpRequest;
import com.help.mp.service.HelpContactService;
import com.help.mp.service.HelpRequestService;
import com.help.mp.util.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mp/help")
@RequiredArgsConstructor
public class HelpController {

    private final HelpRequestService helpRequestService;
    private final HelpContactService helpContactService;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.publish-per-hour:10}")
    private int publishPerHour;

    @PostMapping
    public Result<HelpRequest> publish(@Valid @RequestBody PublishHelpDTO dto) {
        Long userId = UserContext.getUserId();
        // 发布限流：每用户每小时最多 N 次
        if (!rateLimiter.isAllowed("publish:" + userId, publishPerHour, Duration.ofHours(1))) {
            throw new BizException(429, "发布过于频繁，请稍后再试");
        }
        HelpRequest req = helpRequestService.publish(userId, dto.getLatitude(), dto.getLongitude(),
                dto.getAddress(), dto.getAddressAnon(), dto.getUrgencyLevel(), dto.getContent(), dto.getImageUrls(),
                dto.getContactId());
        return Result.ok(req);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        HelpRequest req = helpRequestService.getDetail(id, userId);
        List<HelpImage> images = helpRequestService.listImages(id);
        Map<String, Object> data = new HashMap<>();
        data.put("help", req);
        data.put("images", images);
        // 返回关联的紧急联系人信息（解密后）
        if (req.getContactId() != null) {
            try {
                List<HelpContact> contacts = helpContactService.listByUser(req.getUserId());
                contacts.stream()
                        .filter(c -> c.getId().equals(req.getContactId()))
                        .findFirst()
                        .ifPresent(c -> data.put("contact", c));
            } catch (Exception e) {
                // 联系人查询失败不影响详情展示
            }
        }
        return Result.ok(data);
    }

    @GetMapping("/list")
    public Result<Page<HelpRequest>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) Integer status) {
        Page<HelpRequest> p = helpRequestService.list(page, size, status);
        return Result.ok(p);
    }

    @GetMapping("/nearby")
    public Result<List<HelpRequest>> nearby(@RequestParam BigDecimal lat, @RequestParam BigDecimal lng,
                                             @RequestParam(defaultValue = "10") double radiusKm,
                                             @RequestParam(defaultValue = "50") int limit) {
        List<HelpRequest> list = helpRequestService.nearby(lat, lng, radiusKm, limit);
        return Result.ok(list);
    }

    @GetMapping("/my")
    public Result<Page<HelpRequest>> my(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Page<HelpRequest> p = helpRequestService.myList(UserContext.getUserId(), page, size);
        return Result.ok(p);
    }

    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        helpRequestService.close(id, UserContext.getUserId());
        return Result.ok();
    }
}
