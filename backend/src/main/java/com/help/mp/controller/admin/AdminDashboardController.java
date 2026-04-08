package com.help.mp.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.common.Result;
import com.help.mp.entity.HelpRequest;
import com.help.mp.entity.PushLog;
import com.help.mp.entity.TipOrder;
import com.help.mp.entity.User;
import com.help.mp.mapper.HelpRequestMapper;
import com.help.mp.mapper.PerfLogMapper;
import com.help.mp.mapper.PushLogMapper;
import com.help.mp.mapper.TipOrderMapper;
import com.help.mp.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final TipOrderMapper tipOrderMapper;
    private final PushLogMapper pushLogMapper;
    private final PerfLogMapper perfLogMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 用户统计
        long totalUsers = userMapper.selectCount(null);
        long todayUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart));
        data.put("totalUsers", totalUsers);
        data.put("todayUsers", todayUsers);

        // 求助统计
        long totalHelps = helpRequestMapper.selectCount(null);
        long activeHelps = helpRequestMapper.selectCount(
                new LambdaQueryWrapper<HelpRequest>().eq(HelpRequest::getStatus, 1));
        long todayHelps = helpRequestMapper.selectCount(
                new LambdaQueryWrapper<HelpRequest>().ge(HelpRequest::getPublishTime, todayStart));
        data.put("totalHelps", totalHelps);
        data.put("activeHelps", activeHelps);
        data.put("todayHelps", todayHelps);

        // 打赏统计
        long totalOrders = tipOrderMapper.selectCount(
                new LambdaQueryWrapper<TipOrder>().eq(TipOrder::getStatus, 1));
        Long totalAmountCents = tipOrderMapper.sumPaidAmount();
        data.put("totalOrders", totalOrders);
        data.put("totalAmountYuan", totalAmountCents != null ? totalAmountCents / 100.0 : 0);

        // 推送统计：最近推送的平均到达率
        List<PushLog> recentLogs = pushLogMapper.selectList(
                new LambdaQueryWrapper<PushLog>()
                        .gt(PushLog::getTotalCount, 0)
                        .orderByDesc(PushLog::getCreateTime)
                        .last("LIMIT 50"));
        if (!recentLogs.isEmpty()) {
            int totalPushed = recentLogs.stream().mapToInt(PushLog::getTotalCount).sum();
            int totalSuccess = recentLogs.stream().mapToInt(PushLog::getSuccessCount).sum();
            data.put("pushCount", recentLogs.size());
            data.put("avgReachRate", totalPushed > 0 ? Math.round(totalSuccess * 10000.0 / totalPushed) / 100.0 : 0);
        } else {
            data.put("pushCount", 0);
            data.put("avgReachRate", 0);
        }

        return Result.ok(data);
    }

    /**
     * 接口性能统计：avg/p95/p99/max/慢请求数
     */
    @GetMapping("/perf")
    public Result<Map<String, Object>> perf(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "24") int hours) {
        Map<String, Object> stats = perfLogMapper.getStats(hours);
        java.util.List<Long> durations = perfLogMapper.getAllDurations(hours);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hours", hours);
        result.put("total", stats.get("total"));
        result.put("avgMs", stats.get("avgMs"));
        result.put("maxMs", stats.get("maxMs"));
        result.put("slowCount", stats.get("slowCount"));

        if (durations != null && !durations.isEmpty()) {
            int size = durations.size();
            result.put("p95Ms", durations.get((int) Math.ceil(size * 0.95) - 1));
            result.put("p99Ms", durations.get((int) Math.ceil(size * 0.99) - 1));
        } else {
            result.put("p95Ms", 0);
            result.put("p99Ms", 0);
        }

        return Result.ok(result);
    }
}
