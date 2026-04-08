package com.help.mp.service;

import com.help.mp.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 微信小程序登录等接口封装。生产环境需替换为真实请求。
 */
@Slf4j
@Service
public class WechatMpService {

    @Value("${wechat.mp.app-id:}")
    private String appId;
    @Value("${wechat.mp.app-secret:}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * code2Session，返回 openid、session_key 等
     */
    public Map<String, Object> code2Session(String code) {
        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            log.warn("WeChat MP not configured, using mock openid");
            return Map.of("openid", "mock_openid_" + System.currentTimeMillis(), "session_key", "mock_sk");
        }
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = restTemplate.getForObject(url, Map.class);
            if (res == null || res.containsKey("errcode")) {
                Integer err = res != null ? (Integer) res.get("errcode") : -1;
                throw new BizException(400, "微信登录失败: " + (res != null ? res.get("errmsg") : "unknown"));
            }
            return res;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("code2Session error", e);
            throw new BizException(500, "微信服务异常");
        }
    }
}
