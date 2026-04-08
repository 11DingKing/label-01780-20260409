package com.help.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.entity.ExperienceNote;
import com.help.mp.entity.HelpInteraction;
import com.help.mp.entity.HelpRequest;
import com.help.mp.mapper.ExperienceNoteMapper;
import com.help.mp.mapper.HelpInteractionMapper;
import com.help.mp.mapper.HelpRequestMapper;
import com.help.mp.mapper.UserMapper;
import com.help.mp.entity.User;
import com.help.mp.common.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final HelpInteractionMapper interactionMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final UserMapper userMapper;
    private final ExperienceNoteMapper experienceNoteMapper;

    /**
     * 我帮助别人的记录时间轴：祝福、转发、打赏
     */
    public Page<HelpInteraction> myHelpTimeline(Long userId, int page, int size) {
        Page<HelpInteraction> p = interactionMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<HelpInteraction>().eq(HelpInteraction::getUserId, userId).orderByDesc(HelpInteraction::getCreateTime));
        List<HelpInteraction> list = p.getRecords();
        if (!list.isEmpty()) {
            List<Long> helpIds = list.stream().map(HelpInteraction::getHelpId).distinct().collect(Collectors.toList());
            if (!helpIds.isEmpty()) {
                List<HelpRequest> helps = helpRequestMapper.selectBatchIds(helpIds);
                Map<Long, HelpRequest> helpMap = helps.stream().collect(Collectors.toMap(HelpRequest::getId, h -> h));
                for (HelpInteraction i : list) {
                    i.setHelpContent(helpMap.containsKey(i.getHelpId()) ? helpMap.get(i.getHelpId()).getContent() : null);
                }
            }
        }
        return p;
    }

    /**
     * 我收到的感谢：别人对我的求助进行了祝福/转发/打赏（即对我发布的求助的互动）
     */
    public List<HelpInteraction> thanksForMyHelp(Long userId) {
        List<HelpRequest> myHelps = helpRequestMapper.selectList(
                new LambdaQueryWrapper<HelpRequest>().eq(HelpRequest::getUserId, userId).eq(HelpRequest::getStatus, 1));
        if (myHelps.isEmpty()) return new ArrayList<>();
        List<Long> helpIds = myHelps.stream().map(HelpRequest::getId).collect(Collectors.toList());
        List<HelpInteraction> list = interactionMapper.selectList(
                new LambdaQueryWrapper<HelpInteraction>().in(HelpInteraction::getHelpId, helpIds).orderByDesc(HelpInteraction::getCreateTime));
        if (list.isEmpty()) return new ArrayList<>();
        List<Long> userIds = list.stream().map(HelpInteraction::getUserId).distinct().collect(Collectors.toList());
        if (userIds.isEmpty()) return list;
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        for (HelpInteraction i : list) {
            User u = userMap.get(i.getUserId());
            i.setFromUserNickName(u != null ? u.getNickName() : null);
        }
        return list;
    }

    // ==================== 用户手动管理助人经历 ====================

    public Page<ExperienceNote> myNotes(Long userId, int page, int size) {
        return experienceNoteMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                new LambdaQueryWrapper<ExperienceNote>()
                        .eq(ExperienceNote::getUserId, userId)
                        .eq(ExperienceNote::getStatus, 1)
                        .orderByDesc(ExperienceNote::getCreateTime));
    }

    public ExperienceNote createNote(Long userId, String title, String content, Long helpId) {
        ExperienceNote note = new ExperienceNote();
        note.setUserId(userId);
        note.setTitle(title);
        note.setContent(content);
        note.setHelpId(helpId);
        note.setStatus(1);
        experienceNoteMapper.insert(note);
        return note;
    }

    public void updateNote(Long noteId, Long userId, String title, String content) {
        ExperienceNote note = experienceNoteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId) || note.getStatus() == 0) {
            throw new BizException(403, "无权限或记录不存在");
        }
        if (title != null) note.setTitle(title);
        if (content != null) note.setContent(content);
        experienceNoteMapper.updateById(note);
    }

    public void deleteNote(Long noteId, Long userId) {
        ExperienceNote note = experienceNoteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new BizException(403, "无权限");
        }
        note.setStatus(0);
        experienceNoteMapper.updateById(note);
    }
}
