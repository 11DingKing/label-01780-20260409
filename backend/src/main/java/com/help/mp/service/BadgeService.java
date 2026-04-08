package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.entity.Badge;
import com.help.mp.entity.UserBadge;
import com.help.mp.mapper.BadgeMapper;
import com.help.mp.mapper.UserBadgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;

    public List<Badge> listAll() {
        return badgeMapper.selectList(new LambdaQueryWrapper<Badge>().orderByAsc(Badge::getSortOrder));
    }

    public List<UserBadge> myBadges(Long userId) {
        return userBadgeMapper.selectList(new LambdaQueryWrapper<UserBadge>().eq(UserBadge::getUserId, userId).orderByDesc(UserBadge::getBadgeLevel));
    }

    public void grantIfEligible(Long userId, int badgeLevel) {
        if (badgeLevel <= 0) return;
        long exist = userBadgeMapper.selectCount(
                new LambdaQueryWrapper<UserBadge>().eq(UserBadge::getUserId, userId).eq(UserBadge::getBadgeLevel, badgeLevel));
        if (exist > 0) return;
        UserBadge ub = new UserBadge();
        ub.setUserId(userId);
        ub.setBadgeLevel(badgeLevel);
        userBadgeMapper.insert(ub);
    }
}
