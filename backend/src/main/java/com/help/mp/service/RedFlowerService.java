package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.entity.RedFlowerLog;
import com.help.mp.entity.User;
import com.help.mp.mapper.RedFlowerLogMapper;
import com.help.mp.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedFlowerService {

    private final RedFlowerLogMapper redFlowerLogMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    @Transactional(rollbackFor = Exception.class)
    public void addFlower(Long userId, String bizType, String bizId, int amount, String remark) {
        if (amount <= 0) return;
        User u = userMapper.selectById(userId);
        if (u == null) {
            log.warn("Cannot add red flower: user not found, userId={}", userId);
            return;
        }
        int balanceAfter = (u.getRedFlowerTotal() != null ? u.getRedFlowerTotal() : 0) + amount;
        userMapper.addRedFlower(userId, amount);
        RedFlowerLog record = new RedFlowerLog();
        record.setUserId(userId);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setAmount(amount);
        record.setBalanceAfter(balanceAfter);
        record.setRemark(remark);
        redFlowerLogMapper.insert(record);
        userService.updateBadgeLevelIfNeeded(userId, balanceAfter);
        log.info("Red flower added: userId={}, bizType={}, bizId={}, amount={}, balanceAfter={}", userId, bizType, bizId, amount, balanceAfter);
    }

    public Page<RedFlowerLog> listLogs(Long userId, int page, int size) {
        return redFlowerLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<RedFlowerLog>().eq(RedFlowerLog::getUserId, userId).orderByDesc(RedFlowerLog::getCreateTime));
    }
}
