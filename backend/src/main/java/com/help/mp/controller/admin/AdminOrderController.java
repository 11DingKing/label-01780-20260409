package com.help.mp.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import com.help.mp.entity.TipOrder;
import com.help.mp.mapper.TipOrderMapper;
import com.help.mp.service.TipOrderService;
import com.help.mp.service.WechatPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final TipOrderMapper tipOrderMapper;
    private final WechatPayService wechatPayService;
    private final TipOrderService tipOrderService;

    @GetMapping("/list")
    public Result<Page<TipOrder>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<TipOrder> q = new LambdaQueryWrapper<TipOrder>()
                .orderByDesc(TipOrder::getCreateTime);
        if (status != null) q.eq(TipOrder::getStatus, status);
        return Result.ok(tipOrderMapper.selectPage(new Page<>(page, size), q));
    }

    /**
     * 查询单笔订单的微信支付状态（对账用）
     */
    /**
     * 查询单笔订单的微信支付状态（对账用）。
     * 沙箱模式下返回模拟数据。
     */
    @GetMapping("/{orderNo}/query")
    public Result<Map<String, Object>> queryWxOrder(@PathVariable String orderNo) {
        Map<String, Object> result = wechatPayService.queryOrder(orderNo);
        if (result == null) {
            throw new BizException(500, "查询失败（网络异常）");
        }
        return Result.ok(result);
    }

    /**
     * 手动补单：根据商户订单号查询微信支付状态，若已支付则补发小红花
     */
    @PostMapping("/{orderNo}/reconcile")
    public Result<String> reconcile(@PathVariable String orderNo) {
        Map<String, Object> wxResult = wechatPayService.queryOrder(orderNo);
        if (wxResult == null) {
            throw new BizException(500, "查询微信支付失败");
        }
        String tradeState = (String) wxResult.get("trade_state");
        if ("SUCCESS".equals(tradeState)) {
            String transactionId = (String) wxResult.get("transaction_id");
            tipOrderService.onPaySuccess(orderNo, transactionId);
            return Result.ok("补单成功: " + orderNo);
        }
        return Result.ok("订单状态: " + tradeState + "，无需补单");
    }

    /**
     * 下载指定日期的交易账单
     */
    /**
     * 下载指定日期的交易账单。
     * 沙箱模式下返回模拟账单。
     */
    @GetMapping("/bill")
    public Result<String> downloadBill(@RequestParam String billDate) {
        String bill = wechatPayService.downloadTradeBill(billDate);
        if (bill == null) {
            throw new BizException(500, "下载账单失败（日期无效或网络异常）");
        }
        return Result.ok(bill);
    }
}
