package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.common.BizException;
import com.help.mp.entity.HelpInteraction;
import com.help.mp.entity.HelpRequest;
import com.help.mp.mapper.HelpInteractionMapper;
import com.help.mp.mapper.HelpRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpInteractionService {

    private final HelpInteractionMapper interactionMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final RedFlowerService redFlowerService;

    @Value("${app.flower.bless:1}")
    private int flowerBless;
    @Value("${app.flower.share:2}")
    private int flowerShare;

    @Transactional(rollbackFor = Exception.class)
    public void bless(Long helpId, Long userId) {
        HelpRequest help = helpRequestMapper.selectById(helpId);
        if (help == null || help.getStatus() != 1) throw new BizException(404, "求助不存在或已关闭");
        long exist = interactionMapper.selectCount(
                new LambdaQueryWrapper<HelpInteraction>().eq(HelpInteraction::getHelpId, helpId).eq(HelpInteraction::getUserId, userId).eq(HelpInteraction::getType, "bless"));
        if (exist > 0) throw new BizException(400, "已祝福过该求助");
        HelpInteraction i = new HelpInteraction();
        i.setHelpId(helpId);
        i.setUserId(userId);
        i.setType("bless");
        i.setRedFlowerAmount(flowerBless);
        interactionMapper.insert(i);
        redFlowerService.addFlower(userId, "bless", String.valueOf(helpId), flowerBless, "祝福求助");
        log.info("Bless: helpId={}, userId={}, flowers={}", helpId, userId, flowerBless);
    }

    @Transactional(rollbackFor = Exception.class)
    public void share(Long helpId, Long userId) {
        HelpRequest help = helpRequestMapper.selectById(helpId);
        if (help == null || help.getStatus() != 1) throw new BizException(404, "求助不存在或已关闭");
        long exist = interactionMapper.selectCount(
                new LambdaQueryWrapper<HelpInteraction>().eq(HelpInteraction::getHelpId, helpId).eq(HelpInteraction::getUserId, userId).eq(HelpInteraction::getType, "share"));
        if (exist > 0) throw new BizException(400, "已转发过该求助");
        HelpInteraction i = new HelpInteraction();
        i.setHelpId(helpId);
        i.setUserId(userId);
        i.setType("share");
        i.setRedFlowerAmount(flowerShare);
        interactionMapper.insert(i);
        redFlowerService.addFlower(userId, "share", String.valueOf(helpId), flowerShare, "转发求助");
        log.info("Share: helpId={}, userId={}, flowers={}", helpId, userId, flowerShare);
    }

    public void recordTip(Long helpId, Long userId, String orderNo, int flowerAmount) {
        // 打赏可多次，遇到唯一键冲突时累加小红花数量
        HelpInteraction existing = interactionMapper.selectOne(
                new LambdaQueryWrapper<HelpInteraction>()
                        .eq(HelpInteraction::getHelpId, helpId)
                        .eq(HelpInteraction::getUserId, userId)
                        .eq(HelpInteraction::getType, "tip"));
        if (existing != null) {
            existing.setRedFlowerAmount(existing.getRedFlowerAmount() + flowerAmount);
            existing.setExtra(orderNo);
            interactionMapper.updateById(existing);
        } else {
            HelpInteraction i = new HelpInteraction();
            i.setHelpId(helpId);
            i.setUserId(userId);
            i.setType("tip");
            i.setRedFlowerAmount(flowerAmount);
            i.setExtra(orderNo);
            interactionMapper.insert(i);
        }
    }

    public List<HelpInteraction> listByHelp(Long helpId) {
        return interactionMapper.selectList(new LambdaQueryWrapper<HelpInteraction>().eq(HelpInteraction::getHelpId, helpId));
    }

    public boolean hasBlessed(Long helpId, Long userId) {
        return interactionMapper.selectCount(
                new LambdaQueryWrapper<HelpInteraction>().eq(HelpInteraction::getHelpId, helpId).eq(HelpInteraction::getUserId, userId).eq(HelpInteraction::getType, "bless")) > 0;
    }

    public boolean hasShared(Long helpId, Long userId) {
        return interactionMapper.selectCount(
                new LambdaQueryWrapper<HelpInteraction>().eq(HelpInteraction::getHelpId, helpId).eq(HelpInteraction::getUserId, userId).eq(HelpInteraction::getType, "share")) > 0;
    }
}
