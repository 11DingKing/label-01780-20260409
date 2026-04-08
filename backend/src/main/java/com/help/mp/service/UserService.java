package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.common.BizException;
import com.help.mp.entity.User;
import com.help.mp.mapper.UserMapper;
import com.help.mp.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final AesEncryptor aesEncryptor;

    public User getByOpenid(String openid) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }

    @Transactional(rollbackFor = Exception.class)
    public User getOrCreate(String openid, String sessionKey, String unionid) {
        User u = getByOpenid(openid);
        if (u != null) {
            u.setSessionKey(sessionKey);
            if (unionid != null) u.setUnionid(unionid);
            userMapper.updateById(u);
            return u;
        }
        User newUser = new User();
        newUser.setOpenid(openid);
        newUser.setSessionKey(sessionKey);
        newUser.setUnionid(unionid);
        newUser.setRedFlowerTotal(0);
        newUser.setBadgeLevel(0);
        newUser.setStatus(1);
        newUser.setPhoneAnon(0);
        userMapper.insert(newUser);
        log.info("New user registered: userId={}, openid={}", newUser.getId(), openid);
        return newUser;
    }

    public User getById(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(404, "用户不存在");
        return u;
    }

    /**
     * 获取用户信息（解密敏感字段后返回）
     */
    public User getByIdDecrypted(Long userId) {
        User u = getById(userId);
        if (u.getPhoneEnc() != null && !u.getPhoneEnc().isEmpty()) {
            u.setPhoneEnc(aesEncryptor.decrypt(u.getPhoneEnc()));
        }
        return u;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, String nickName, String avatarUrl, String phoneEnc, Integer phoneAnon) {
        User u = getById(userId);
        if (nickName != null) u.setNickName(nickName);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        if (phoneEnc != null) {
            // 对手机号进行 AES 加密存储
            u.setPhoneEnc(aesEncryptor.encrypt(phoneEnc));
        }
        if (phoneAnon != null) u.setPhoneAnon(phoneAnon);
        userMapper.updateById(u);
        log.info("User profile updated: userId={}", userId);
    }

    public void updateBadgeLevelIfNeeded(Long userId, int newTotal) {
        User u = getById(userId);
        int currentLevel = u.getBadgeLevel() != null ? u.getBadgeLevel() : 0;
        int newLevel = badgeLevelByFlowers(newTotal);
        if (newLevel > currentLevel) {
            u.setBadgeLevel(newLevel);
            userMapper.updateById(u);
        }
    }

    private int badgeLevelByFlowers(int flowers) {
        if (flowers >= 1000) return 5;
        if (flowers >= 500) return 4;
        if (flowers >= 200) return 3;
        if (flowers >= 50) return 2;
        if (flowers >= 10) return 1;
        return 0;
    }
}
