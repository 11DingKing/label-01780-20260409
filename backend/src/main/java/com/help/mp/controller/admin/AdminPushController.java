package com.help.mp.controller.admin;

import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import com.help.mp.entity.HelpRequest;
import com.help.mp.entity.PushLog;
import com.help.mp.entity.PushRule;
import com.help.mp.mapper.HelpRequestMapper;
import com.help.mp.mapper.PushRuleMapper;
import com.help.mp.service.WechatOfficialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/push")
@RequiredArgsConstructor
public class AdminPushController {

    private final PushRuleMapper pushRuleMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final WechatOfficialService wechatOfficialService;

    @GetMapping("/rules")
    public Result<List<PushRule>> rules() {
        return Result.ok(pushRuleMapper.selectList(null));
    }

    @PostMapping("/rules")
    public Result<PushRule> saveRule(@RequestBody PushRule rule) {
        if (rule.getId() != null) pushRuleMapper.updateById(rule);
        else pushRuleMapper.insert(rule);
        return Result.ok(rule);
    }

    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        pushRuleMapper.deleteById(id);
        return Result.ok();
    }

    /**
     * 手动触发推送（针对指定求助）
     */
    @PostMapping("/trigger")
    public Result<Map<String, Object>> trigger(@RequestBody Map<String, Long> body) {
        Long helpId = body.get("helpId");
        if (helpId == null) throw new BizException(400, "请指定求助ID");
        HelpRequest help = helpRequestMapper.selectById(helpId);
        if (help == null) throw new BizException(404, "求助不存在");
        int count = wechatOfficialService.triggerPush(helpId, help);
        return Result.ok(Map.of("helpId", helpId, "pushed", count));
    }

    /**
     * 推送日志与到达率统计
     */
    @GetMapping("/logs")
    public Result<List<PushLog>> pushLogs(@RequestParam(required = false) Long helpId) {
        return Result.ok(wechatOfficialService.getPushLogs(helpId));
    }
}
