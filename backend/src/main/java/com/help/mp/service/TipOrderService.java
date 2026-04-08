package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.BizException;
import com.help.mp.entity.TipOrder;
import com.help.mp.entity.User;
import com.help.mp.mapper.TipOrderMapper;
import com.help.mp.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipOrderService {

    private final TipOrderMapper tipOrderMapper;
    private final UserMapper userMapper;
    private final WechatPayService wechatPayService;
    private final RedFlowerService redFlowerService;
    private final HelpInteractionService helpInteractionService;

    @Value("${app.flower.tip-per-yuan:1}")
    private int flowerPerYuan;

    @Transactional(rollbackFor = Exception.class)
    public TipOrder create(Long helpId, Long userId, int amountCents, String openid) {
        if (amountCents < 100) throw new BizException(400, "打赏金额至少 1 元");
        if (amountCents > 10000) throw new BizException(400, "单笔打赏不超过 100 元");
        TipOrder order = new TipOrder();
        order.setOrderNo("T" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8));
        order.setHelpId(helpId);
        order.setUserId(userId);
        order.setAmountCents(amountCents);
        order.setStatus(0);
        tipOrderMapper.insert(order);
        return order;
    }

    public Map<String, String> getPayParams(Long orderId, Long userId, String openid) {
        TipOrder order = tipOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BizException(404, "订单不存在");
        if (order.getStatus() != 0) throw new BizException(400, "订单状态不允许支付");
        Map<String, String> params = wechatPayService.createPayParams(order, openid);
        // 沙箱模式：自动完成支付流程
        if ("true".equals(params.get("sandbox"))) {
            log.info("[SANDBOX] Auto-completing payment for order {}", order.getOrderNo());
            onPaySuccess(order.getOrderNo(), "SANDBOX_" + order.getOrderNo());
        }
        return params;
    }

    /**
     * 支付成功回调后：更新订单、发放小红花、记录互动。
     */
    @Transactional(rollbackFor = Exception.class)
    public void onPaySuccess(String orderNo, String wxTransactionId) {
        TipOrder order = tipOrderMapper.selectOne(new LambdaQueryWrapper<TipOrder>().eq(TipOrder::getOrderNo, orderNo));
        if (order == null) return;
        if (order.getStatus() == 1) return;
        order.setStatus(1);
        order.setWxTransactionId(wxTransactionId);
        order.setPayTime(LocalDateTime.now());
        tipOrderMapper.updateById(order);
        int yuan = order.getAmountCents() / 100;
        int flowers = yuan * flowerPerYuan;
        redFlowerService.addFlower(order.getUserId(), "tip", orderNo, flowers, "打赏求助");
        helpInteractionService.recordTip(order.getHelpId(), order.getUserId(), orderNo, flowers);
        log.info("Tip paid: orderNo={}, userId={}, flowers={}", orderNo, order.getUserId(), flowers);
    }

    public Page<TipOrder> myOrders(Long userId, int page, int size) {
        return tipOrderMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<TipOrder>().eq(TipOrder::getUserId, userId).orderByDesc(TipOrder::getCreateTime));
    }

    /**
     * 处理微信支付异步通知。生产环境需验签并解析 body 获取 order_no、transaction_id。
     */
    public boolean handleNotify(String body) {
        if (body == null || body.isEmpty()) return false;
        // Mock: 若 body 含 order_no 可解析后调用 onPaySuccess
        return true;
    }
}
