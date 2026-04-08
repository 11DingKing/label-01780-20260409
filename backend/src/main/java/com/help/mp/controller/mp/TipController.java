package com.help.mp.controller.mp;

import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import com.help.mp.context.UserContext;
import com.help.mp.dto.TipCreateDTO;
import com.help.mp.entity.TipOrder;
import com.help.mp.entity.User;
import com.help.mp.service.TipOrderService;
import com.help.mp.service.UserService;
import com.help.mp.service.WechatPayService;
import com.help.mp.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mp/tip")
@RequiredArgsConstructor
public class TipController {

    private final TipOrderService tipOrderService;
    private final UserService userService;
    private final WechatPayService wechatPayService;
    private final RateLimiter rateLimiter;

    @PostMapping("/create")
    public Result<Map<String, Object>> create(@Valid @RequestBody TipCreateDTO dto) {
        Long userId = UserContext.getUserId();
        // 打赏限流：每用户每分钟最多5次
        if (!rateLimiter.isAllowed("tip:" + userId, 5, Duration.ofMinutes(1))) {
            throw new BizException(429, "操作过于频繁，请稍后再试");
        }
        User user = userService.getById(userId);
        TipOrder order = tipOrderService.create(dto.getHelpId(), userId, dto.getAmountCents(), user.getOpenid());
        Map<String, String> payParams = tipOrderService.getPayParams(order.getId(), userId, user.getOpenid());
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("amountCents", order.getAmountCents());
        data.put("payParams", payParams);
        return Result.ok(data);
    }

    @GetMapping("/records")
    public Result<?> records(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return Result.ok(tipOrderService.myOrders(UserContext.getUserId(), page, size));
    }

    /**
     * 微信支付 API v3 异步回调（JSON 格式）。
     * 流程：验签 → AEAD 解密 → 处理业务 → 返回应答。
     */
    @PostMapping("/notify")
    public Map<String, String> notify(HttpServletRequest request) {
        try {
            // 1. 读取请求体
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            String body = sb.toString();

            // 2. 验证回调签名
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String signature = request.getHeader("Wechatpay-Signature");
            String serial = request.getHeader("Wechatpay-Serial");

            if (!wechatPayService.verifyNotifySignature(timestamp, nonce, body, signature, serial)) {
                log.warn("Pay notify signature verification failed");
                return Map.of("code", "FAIL", "message", "签名验证失败");
            }

            // 3. 解析并解密
            @SuppressWarnings("unchecked")
            Map<String, Object> notifyBody = new com.fasterxml.jackson.databind.ObjectMapper().readValue(body, Map.class);
            Map<String, Object> decrypted = wechatPayService.decryptNotify(notifyBody);

            if (decrypted != null) {
                String tradeState = (String) decrypted.get("trade_state");
                if ("SUCCESS".equals(tradeState)) {
                    String orderNo = (String) decrypted.get("out_trade_no");
                    String transactionId = (String) decrypted.get("transaction_id");
                    if (orderNo != null) {
                        tipOrderService.onPaySuccess(orderNo, transactionId);
                    }
                } else {
                    log.info("Pay notify trade_state={}, skipping", tradeState);
                }
            } else {
                log.warn("Pay notify decryption returned null — callback rejected");
                return Map.of("code", "FAIL", "message", "解密失败");
            }

            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (Exception e) {
            log.error("Pay notify error", e);
            return Map.of("code", "FAIL", "message", e.getMessage() != null ? e.getMessage() : "处理失败");
        }
    }
}
