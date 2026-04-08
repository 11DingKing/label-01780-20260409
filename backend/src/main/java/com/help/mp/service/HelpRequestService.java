package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.BizException;
import com.help.mp.entity.HelpImage;
import com.help.mp.entity.HelpRequest;
import com.help.mp.entity.User;
import com.help.mp.mapper.HelpImageMapper;
import com.help.mp.mapper.HelpRequestMapper;
import com.help.mp.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpRequestService {

    private final HelpRequestMapper helpRequestMapper;
    private final HelpImageMapper helpImageMapper;
    private final UserMapper userMapper;
    private final WechatOfficialService wechatOfficialService;

    @Value("${app.flower.bless:1}")
    private int flowerBless;

    @Transactional(rollbackFor = Exception.class)
    public HelpRequest publish(Long userId, BigDecimal latitude, BigDecimal longitude, String address,
                               Integer addressAnon, Integer urgencyLevel, String content, List<String> imageUrls,
                               Long contactId) {
        if (urgencyLevel == null || urgencyLevel < 1 || urgencyLevel > 3)
            throw new BizException(400, "紧急程度无效");
        HelpRequest req = new HelpRequest();
        req.setUserId(userId);
        req.setLatitude(latitude);
        req.setLongitude(longitude);
        req.setAddress(address);
        req.setAddressAnon(addressAnon != null ? addressAnon : 0);
        req.setUrgencyLevel(urgencyLevel);
        req.setContent(content);
        req.setContactId(contactId);
        req.setStatus(1);
        req.setPublishTime(LocalDateTime.now());
        helpRequestMapper.insert(req);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                HelpImage img = new HelpImage();
                img.setHelpId(req.getId());
                img.setUrl(imageUrls.get(i));
                img.setSortOrder(i);
                helpImageMapper.insert(img);
            }
        }
        log.info("Help published: helpId={}, userId={}, urgency={}", req.getId(), userId, urgencyLevel);
        // 异步触发公众号推送
        try {
            wechatOfficialService.pushForNewHelp(req);
        } catch (Exception e) {
            log.warn("Push notification failed for helpId={}: {}", req.getId(), e.getMessage());
        }
        return req;
    }

    public HelpRequest getDetail(Long id, Long currentUserId) {
        HelpRequest req = helpRequestMapper.selectById(id);
        if (req == null) throw new BizException(404, "求助不存在");
        if (req.getStatus() == 0) throw new BizException(404, "求助已删除");
        // 填充发布者信息（昵称、头像、勋章等级）
        User publisher = userMapper.selectById(req.getUserId());
        if (publisher != null) {
            req.setPublisherNickName(publisher.getNickName());
            req.setPublisherAvatar(publisher.getAvatarUrl());
            req.setPublisherBadgeLevel(publisher.getBadgeLevel());
        }
        return req;
    }

    public List<HelpImage> listImages(Long helpId) {
        return helpImageMapper.selectList(
                new LambdaQueryWrapper<HelpImage>().eq(HelpImage::getHelpId, helpId).orderByAsc(HelpImage::getSortOrder));
    }

    public Page<HelpRequest> list(int page, int size, Integer status) {
        if (status == null) status = 1;
        Page<HelpRequest> p = helpRequestMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<HelpRequest>().eq(HelpRequest::getStatus, status).orderByDesc(HelpRequest::getPublishTime));
        fillPublisher(p.getRecords());
        return p;
    }

    public List<HelpRequest> nearby(BigDecimal lat, BigDecimal lng, double radiusKm, int limit) {
        if (lat == null || lng == null) throw new BizException(400, "请提供位置信息");
        List<HelpRequest> list = helpRequestMapper.selectNearby(lat, lng, radiusKm, 1, limit);
        fillPublisher(list);
        return list;
    }

    public Page<HelpRequest> myList(Long userId, int page, int size) {
        Page<HelpRequest> p = helpRequestMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<HelpRequest>().eq(HelpRequest::getUserId, userId).orderByDesc(HelpRequest::getPublishTime));
        fillPublisher(p.getRecords());
        return p;
    }

    private void fillPublisher(List<HelpRequest> list) {
        if (list == null || list.isEmpty()) return;
        List<Long> userIds = list.stream().map(HelpRequest::getUserId).distinct().collect(Collectors.toList());
        for (Long uid : userIds) {
            User u = userMapper.selectById(uid);
            if (u != null) {
                for (HelpRequest r : list) {
                    if (r.getUserId().equals(uid)) {
                        r.setPublisherNickName(u.getNickName());
                        r.setPublisherAvatar(u.getAvatarUrl());
                        r.setPublisherBadgeLevel(u.getBadgeLevel());
                    }
                }
            }
        }
    }

    public void close(Long helpId, Long userId) {
        HelpRequest req = helpRequestMapper.selectById(helpId);
        if (req == null || !req.getUserId().equals(userId)) throw new BizException(403, "无权限");
        req.setStatus(2);
        helpRequestMapper.updateById(req);
    }
}
