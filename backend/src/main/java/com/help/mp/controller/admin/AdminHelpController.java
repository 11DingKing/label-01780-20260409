package com.help.mp.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import com.help.mp.entity.HelpRequest;
import com.help.mp.mapper.HelpRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/help")
@RequiredArgsConstructor
public class AdminHelpController {

    private final HelpRequestMapper helpRequestMapper;

    @GetMapping("/list")
    public Result<Page<HelpRequest>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<HelpRequest> q = new LambdaQueryWrapper<HelpRequest>().orderByDesc(HelpRequest::getPublishTime);
        if (status != null) q.eq(HelpRequest::getStatus, status);
        Page<HelpRequest> p = helpRequestMapper.selectPage(new Page<>(page, size), q);
        return Result.ok(p);
    }

    /**
     * 关闭/删除求助（管理员操作）
     */
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1 && status != 2))
            throw new BizException(400, "状态值无效(0删除/1发布/2关闭)");
        HelpRequest req = helpRequestMapper.selectById(id);
        if (req == null) throw new BizException(404, "求助不存在");
        req.setStatus(status);
        helpRequestMapper.updateById(req);
        return Result.ok();
    }
}
