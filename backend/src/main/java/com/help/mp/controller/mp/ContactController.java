package com.help.mp.controller.mp;

import com.help.mp.common.Result;
import com.help.mp.context.UserContext;
import com.help.mp.dto.ContactDTO;
import com.help.mp.entity.HelpContact;
import com.help.mp.service.HelpContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mp/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final HelpContactService helpContactService;

    @GetMapping
    public Result<List<HelpContact>> list() {
        return Result.ok(helpContactService.listByUser(UserContext.getUserId()));
    }

    @PostMapping
    public Result<HelpContact> add(@Valid @RequestBody ContactDTO dto) {
        return Result.ok(helpContactService.add(UserContext.getUserId(), dto.getNameEnc(), dto.getPhoneEnc(), dto.getRelation()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ContactDTO dto) {
        helpContactService.update(id, UserContext.getUserId(), dto.getNameEnc(), dto.getPhoneEnc(), dto.getRelation());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        helpContactService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
