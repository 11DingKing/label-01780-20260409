package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.entity.HelpRequest;
import com.help.mp.entity.PushLog;
import com.help.mp.entity.PushRule;
import com.help.mp.mapper.PushLogMapper;
import com.help.mp.mapper.PushRuleMapper;
import com.help.mp.mapper.UserLocationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 微信公众号模板消息推送服务。
 * 实现：获取粉丝列表 → 按推送规则(紧急程度+地理位置)过滤 → 逐个发送模板消息
 *      → 失败用户二次补偿推送 → 记录推送日志与到达率 → 低于阈值自动告警。
 */
@Slf4j
@Service
public class WechatOfficialService {

    @Value("${wechat.official.app-id:}")
    private String officialAppId;
    @Value("${wechat.official.app-secret:}")
    private String officialAppSecret;
    @Value("${wechat.official.template-id:}")
    private String templateId;
    @Value("${wechat.mp.app-id:}")
    private String mpAppId;
    @Value("${app.push.reach-rate-threshold:0.95}")
    private double reachRateThreshold;

    private final PushRuleMapper pushRuleMapper;
    private final PushLogMapper pushLogMapper;
    private final UserLocationMapper userLocationMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private volatile String accessToken;
    private volatile long tokenExpireTime;

    @Autowired
    public WechatOfficialService(PushRuleMapper pushRuleMapper, PushLogMapper pushLogMapper,
                                  UserLocationMapper userLocationMapper) {
        this.pushRuleMapper = pushRuleMapper;
        this.pushLogMapper = pushLogMapper;
        this.userLocationMapper = userLocationMapper;
    }

    private boolean isConfigured() {
        return officialAppId != null && !officialAppId.isEmpty()
                && officialAppSecret != null && !officialAppSecret.isEmpty();
    }

    /**
     * 获取公众号 access_token（带内存缓存，提前5分钟刷新）
     */
    public synchronized String getAccessToken() {
        if (!isConfigured()) {
            log.warn("Official account not configured");
            return null;
        }
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                officialAppId, officialAppSecret);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = restTemplate.getForObject(url, Map.class);
            if (res != null && res.containsKey("access_token")) {
                accessToken = (String) res.get("access_token");
                int expiresIn = (Integer) res.getOrDefault("expires_in", 7200);
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
                log.info("Official access_token refreshed, expires_in={}s", expiresIn);
                return accessToken;
            }
            log.error("Failed to get official access_token: {}", res);
        } catch (Exception e) {
            log.error("Get official access_token error", e);
        }
        return null;
    }

    /**
     * 获取公众号全部关注用户 openid 列表（分页拉取）
     */
    public List<String> getAllFollowerOpenids() {
        String token = getAccessToken();
        if (token == null) return Collections.emptyList();

        List<String> allOpenids = new ArrayList<>();
        String nextOpenid = "";

        for (int page = 0; page < 100; page++) { // 安全上限：100页 × 10000 = 100万用户
            String url = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=" + token
                    + "&next_openid=" + nextOpenid;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> res = restTemplate.getForObject(url, Map.class);
                if (res == null) break;

                Integer total = (Integer) res.get("total");
                Integer count = (Integer) res.get("count");
                if (count == null || count == 0) break;

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) res.get("data");
                if (data != null) {
                    @SuppressWarnings("unchecked")
                    List<String> openids = (List<String>) data.get("openid");
                    if (openids != null) allOpenids.addAll(openids);
                }

                nextOpenid = (String) res.get("next_openid");
                if (nextOpenid == null || nextOpenid.isEmpty() || allOpenids.size() >= total) break;
            } catch (Exception e) {
                log.error("Fetch follower list error at page {}", page, e);
                break;
            }
        }

        log.info("Fetched {} follower openids from official account", allOpenids.size());
        return allOpenids;
    }

    /**
     * 根据推送规则，对新发布的求助触发公众号模板消息推送（异步）。
     */
    @Async
    public void pushForNewHelp(HelpRequest help) {
        List<PushRule> rules = pushRuleMapper.selectList(
                new LambdaQueryWrapper<PushRule>().eq(PushRule::getEnabled, 1));
        if (rules.isEmpty()) {
            log.info("No enabled push rules, skip push for helpId={}", help.getId());
            return;
        }

        // 匹配紧急程度
        PushRule matchedRule = null;
        for (PushRule rule : rules) {
            String levels = rule.getUrgencyLevels();
            if (levels != null && !levels.isEmpty()) {
                Set<String> levelSet = new HashSet<>(Arrays.asList(levels.split(",")));
                if (!levelSet.contains(String.valueOf(help.getUrgencyLevel()))) continue;
            }
            matchedRule = rule;
            break;
        }
        if (matchedRule == null) {
            log.info("Help urgency {} not matched by any push rule, skip", help.getUrgencyLevel());
            return;
        }

        doPush(help, matchedRule);
    }

    /**
     * 执行推送：获取粉丝列表 → 按地理位置过滤 → 逐个发送 → 失败补偿重推 → 记录日志 → 低于阈值告警
     */
    private void doPush(HelpRequest help, PushRule rule) {
        if (!isConfigured() || templateId == null || templateId.isEmpty()) {
            log.warn("Push skipped: official account or template not configured. helpId={}", help.getId());
            savePushLog(help.getId(), rule != null ? rule.getId() : null, 0, 0, 0);
            return;
        }

        List<String> openids;

        // 地理位置过滤：如果规则有 radiusKm 且求助有坐标，按距离筛选
        if (rule != null && rule.getRadiusKm() != null && rule.getRadiusKm().doubleValue() > 0
                && help.getLatitude() != null && help.getLongitude() != null) {
            double radiusKm = rule.getRadiusKm().doubleValue();
            openids = userLocationMapper.selectOpenidsWithinRadius(
                    help.getLatitude(), help.getLongitude(), radiusKm);
            log.info("Geo-filtered push for helpId={}: {} followers within {}km",
                    help.getId(), openids.size(), radiusKm);

            // 如果地理过滤后无人，回退到全量推送
            if (openids.isEmpty()) {
                log.info("No followers within radius, falling back to all followers for helpId={}", help.getId());
                openids = getAllFollowerOpenids();
            }
        } else {
            openids = getAllFollowerOpenids();
        }

        if (openids.isEmpty()) {
            log.info("No followers to push for helpId={}", help.getId());
            savePushLog(help.getId(), rule != null ? rule.getId() : null, 0, 0, 0);
            return;
        }

        int total = openids.size();

        // ===== 第一轮推送 =====
        List<String> failedOpenids = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (String openid : openids) {
            if (sendToUser(openid, help)) {
                successCount.incrementAndGet();
            } else {
                failedOpenids.add(openid);
            }
        }

        // ===== 补偿推送：对第一轮失败的用户延迟后重试一轮 =====
        if (!failedOpenids.isEmpty()) {
            log.info("Compensation push for helpId={}: retrying {} failed users", help.getId(), failedOpenids.size());
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            List<String> stillFailed = new ArrayList<>();
            for (String openid : failedOpenids) {
                if (sendToUser(openid, help)) {
                    successCount.incrementAndGet();
                } else {
                    stillFailed.add(openid);
                }
            }
            failedOpenids = stillFailed;
        }

        int finalFail = failedOpenids.size();
        int finalSuccess = successCount.get();
        double reachRate = total > 0 ? (double) finalSuccess / total : 0.0;

        savePushLog(help.getId(), rule != null ? rule.getId() : null, total, finalSuccess, finalFail);

        log.info("Push completed for helpId={}: total={}, success={}, fail={}, reachRate={}%",
                help.getId(), total, finalSuccess, finalFail,
                String.format("%.2f", reachRate * 100));

        // ===== 到达率低于阈值告警 =====
        if (total > 0 && reachRate < reachRateThreshold) {
            log.error("PUSH_ALERT: Reach rate {:.2f}% below threshold {:.2f}% for helpId={}. " +
                            "total={}, success={}, fail={}. Immediate attention required!",
                    reachRate * 100, reachRateThreshold * 100, help.getId(),
                    total, finalSuccess, finalFail);
            // 如果到达率低于80%且是高紧急求助，自动扩大推送范围重试
            if (reachRate < 0.80 && help.getUrgencyLevel() != null && help.getUrgencyLevel() == 1) {
                log.warn("PUSH_AUTO_EXPAND: High urgency help with low reach rate, expanding to all followers");
                List<String> allOpenids = getAllFollowerOpenids();
                Set<String> alreadySent = new HashSet<>(openids);
                int extraSuccess = 0;
                int extraTotal = 0;
                for (String openid : allOpenids) {
                    if (!alreadySent.contains(openid)) {
                        extraTotal++;
                        if (sendToUser(openid, help)) extraSuccess++;
                    }
                }
                if (extraTotal > 0) {
                    log.info("PUSH_AUTO_EXPAND result: extra_total={}, extra_success={}", extraTotal, extraSuccess);
                    savePushLog(help.getId(), rule != null ? rule.getId() : null,
                            total + extraTotal, finalSuccess + extraSuccess, finalFail + extraTotal - extraSuccess);
                }
            }
        }
    }

    /**
     * 向单个公众号用户发送模板消息（含重试）
     */
    public boolean sendToUser(String openid, HelpRequest help) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String token = getAccessToken();
            if (token == null) return false;

            String urgencyText = help.getUrgencyLevel() == 1 ? "紧急" : (help.getUrgencyLevel() == 2 ? "一般" : "轻微");
            String content = help.getContent();
            if (content != null && content.length() > 50) content = content.substring(0, 50) + "...";

            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
            Map<String, Object> body = new HashMap<>();
            body.put("touser", openid);
            body.put("template_id", templateId);

            // 点击跳转到小程序求助详情
            String miniAppId = mpAppId != null && !mpAppId.isEmpty() ? mpAppId : officialAppId;
            body.put("miniprogram", Map.of(
                    "appid", miniAppId,
                    "pagepath", "pages/help-detail/help-detail?id=" + help.getId()));

            Map<String, Object> data = new HashMap<>();
            data.put("first", Map.of("value", "有人发布了紧急求助"));
            data.put("keyword1", Map.of("value", urgencyText));
            data.put("keyword2", Map.of("value", content != null ? content : ""));
            data.put("keyword3", Map.of("value", help.getAddress() != null ? help.getAddress() : "未知位置"));
            data.put("remark", Map.of("value", "点击查看详情，伸出援手"));
            body.put("data", data);

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> res = restTemplate.postForObject(url, body, Map.class);
                if (res != null) {
                    Integer errcode = (Integer) res.get("errcode");
                    if (errcode != null && errcode == 0) {
                        return true;
                    }
                    // 40001 = access_token 过期，清除缓存后重试
                    if (errcode != null && (errcode == 40001 || errcode == 42001)) {
                        log.warn("Access token expired, refreshing and retrying (attempt {})", attempt);
                        accessToken = null;
                        tokenExpireTime = 0;
                        continue;
                    }
                    // 43004 = 用户未关注，不重试
                    if (errcode != null && errcode == 43004) {
                        return false;
                    }
                    log.warn("Send template msg failed (attempt {}): errcode={}, errmsg={}", attempt, errcode, res.get("errmsg"));
                }
            } catch (Exception e) {
                log.error("Send template msg error (attempt {})", attempt, e);
            }

            // 重试间隔
            if (attempt < 3) {
                try { Thread.sleep(500L * attempt); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
        }
        return false;
    }

    /**
     * 保存推送日志
     */
    private void savePushLog(Long helpId, Long ruleId, int total, int success, int fail) {
        PushLog pushLog = new PushLog();
        pushLog.setHelpId(helpId);
        pushLog.setRuleId(ruleId);
        pushLog.setTotalCount(total);
        pushLog.setSuccessCount(success);
        pushLog.setFailCount(fail);
        pushLog.setReachRate(total > 0 ? (double) success / total : 0.0);
        pushLogMapper.insert(pushLog);
    }

    /**
     * 手动触发推送（管理端调用），返回推送人数
     */
    public int triggerPush(Long helpId, HelpRequest help) {
        if (help == null) {
            log.warn("triggerPush: help is null for helpId={}", helpId);
            return 0;
        }

        List<PushRule> rules = pushRuleMapper.selectList(
                new LambdaQueryWrapper<PushRule>().eq(PushRule::getEnabled, 1));
        PushRule rule = rules.isEmpty() ? null : rules.get(0);

        if (!isConfigured() || templateId == null || templateId.isEmpty()) {
            log.warn("Push skipped (manual trigger): not configured. helpId={}", helpId);
            savePushLog(helpId, rule != null ? rule.getId() : null, 0, 0, 0);
            return 0;
        }

        List<String> openids = getAllFollowerOpenids();
        if (openids.isEmpty()) {
            savePushLog(helpId, rule != null ? rule.getId() : null, 0, 0, 0);
            return 0;
        }

        int success = 0, fail = 0;
        for (String openid : openids) {
            if (sendToUser(openid, help)) success++;
            else fail++;
        }

        savePushLog(helpId, rule != null ? rule.getId() : null, openids.size(), success, fail);
        return success;
    }

    /**
     * 查询推送统计（管理端）
     */
    public List<PushLog> getPushLogs(Long helpId) {
        LambdaQueryWrapper<PushLog> q = new LambdaQueryWrapper<PushLog>()
                .orderByDesc(PushLog::getCreateTime);
        if (helpId != null) q.eq(PushLog::getHelpId, helpId);
        return pushLogMapper.selectList(q);
    }

    /**
     * 更新粉丝位置（公众号上报位置事件时调用）
     */
    public void updateFollowerLocation(String openid, java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        if (openid == null || latitude == null || longitude == null) return;
        com.help.mp.entity.UserLocation loc = userLocationMapper.selectOne(
                new LambdaQueryWrapper<com.help.mp.entity.UserLocation>()
                        .eq(com.help.mp.entity.UserLocation::getOpenid, openid));
        if (loc != null) {
            loc.setLatitude(latitude);
            loc.setLongitude(longitude);
            userLocationMapper.updateById(loc);
        } else {
            loc = new com.help.mp.entity.UserLocation();
            loc.setOpenid(openid);
            loc.setLatitude(latitude);
            loc.setLongitude(longitude);
            userLocationMapper.insert(loc);
        }
        log.debug("Updated follower location: openid={}, lat={}, lng={}", openid, latitude, longitude);
    }
}
