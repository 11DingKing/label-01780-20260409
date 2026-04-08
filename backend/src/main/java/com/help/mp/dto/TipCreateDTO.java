package com.help.mp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TipCreateDTO {
    @NotNull
    private Long helpId;
    @NotNull
    @Min(100)
    private Integer amountCents; // 分
}
