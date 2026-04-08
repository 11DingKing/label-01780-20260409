package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.help.mp.common.BizException;
import com.help.mp.entity.HelpContact;
import com.help.mp.mapper.HelpContactMapper;
import com.help.mp.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HelpContactService {

    private final HelpContactMapper helpContactMapper;
    private final AesEncryptor aesEncryptor;

    /**
     * 查询联系人列表（解密后返回）
     */
    public List<HelpContact> listByUser(Long userId) {
        List<HelpContact> list = helpContactMapper.selectList(
                new LambdaQueryWrapper<HelpContact>().eq(HelpContact::getUserId, userId).orderByAsc(HelpContact::getSortOrder));
        for (HelpContact c : list) {
            c.setNameEnc(aesEncryptor.decrypt(c.getNameEnc()));
            c.setPhoneEnc(aesEncryptor.decrypt(c.getPhoneEnc()));
        }
        return list;
    }

    /**
     * 新增联系人（加密存储）
     */
    public HelpContact add(Long userId, String name, String phone, String relation) {
        HelpContact c = new HelpContact();
        c.setUserId(userId);
        c.setNameEnc(aesEncryptor.encrypt(name));
        c.setPhoneEnc(aesEncryptor.encrypt(phone));
        c.setRelation(relation != null ? relation : "");
        c.setSortOrder(0);
        helpContactMapper.insert(c);
        // 返回时解密
        c.setNameEnc(name);
        c.setPhoneEnc(phone);
        return c;
    }

    /**
     * 更新联系人（加密存储）
     */
    public void update(Long id, Long userId, String name, String phone, String relation) {
        HelpContact c = helpContactMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) throw new BizException(403, "无权限");
        if (name != null) c.setNameEnc(aesEncryptor.encrypt(name));
        if (phone != null) c.setPhoneEnc(aesEncryptor.encrypt(phone));
        if (relation != null) c.setRelation(relation);
        helpContactMapper.updateById(c);
    }

    public void delete(Long id, Long userId) {
        HelpContact c = helpContactMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) throw new BizException(403, "无权限");
        helpContactMapper.deleteById(id);
    }
}
