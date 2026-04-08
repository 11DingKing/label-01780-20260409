package com.help.mp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PublishHelpDTO {
    @NotNull
    @DecimalMin("-90") @DecimalMax("90")
    private BigDecimal latitude;
    @NotNull
    @DecimalMin("-180") @DecimalMax("180")
    private BigDecimal longitude;
    private String address;
    private Integer addressAnon;
    @NotNull(message = "紧急程度必填")
    private Integer urgencyLevel; // 1高 2中 3低
    @NotNull(message = "描述必填")
    private String content;
    private List<String> imageUrls;
    private Long contactId;
}
