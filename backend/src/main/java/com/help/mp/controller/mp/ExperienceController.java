package com.help.mp.controller.mp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.help.mp.common.Result;
import com.help.mp.context.UserContext;
import com.help.mp.dto.ExperienceNoteDTO;
import com.help.mp.entity.ExperienceNote;
import com.help.mp.entity.HelpInteraction;
import com.help.mp.service.ExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mp/experience")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping("/timeline")
    public Result<Page<HelpInteraction>> timeline(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return Result.ok(experienceService.myHelpTimeline(UserContext.getUserId(), page, size));
    }

    @GetMapping("/thanks")
    public Result<List<HelpInteraction>> thanks() {
        return Result.ok(experienceService.thanksForMyHelp(UserContext.getUserId()));
    }

    // ==================== 手动管理助人经历 ====================

    @GetMapping("/notes")
    public Result<Page<ExperienceNote>> notes(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.ok(experienceService.myNotes(UserContext.getUserId(), page, size));
    }

    @PostMapping("/notes")
    public Result<ExperienceNote> createNote(@Valid @RequestBody ExperienceNoteDTO dto) {
        ExperienceNote note = experienceService.createNote(
                UserContext.getUserId(), dto.getTitle(), dto.getContent(), dto.getHelpId());
        return Result.ok(note);
    }

    @PutMapping("/notes/{id}")
    public Result<Void> updateNote(@PathVariable Long id, @RequestBody ExperienceNoteDTO dto) {
        experienceService.updateNote(id, UserContext.getUserId(), dto.getTitle(), dto.getContent());
        return Result.ok();
    }

    @DeleteMapping("/notes/{id}")
    public Result<Void> deleteNote(@PathVariable Long id) {
        experienceService.deleteNote(id, UserContext.getUserId());
        return Result.ok();
    }
}
