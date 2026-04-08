package com.help.mp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactDTO {
    @NotBlank(message = "姓名不能为空")
    private String nameEnc;
    @NotBlank(message = "电话不能为空")
    private String phoneEnc;
    @NotBlank(message = "关系不能为空")
    private String relation;
}
