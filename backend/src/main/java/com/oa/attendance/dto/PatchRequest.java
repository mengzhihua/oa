package com.oa.attendance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PatchRequest {
    @NotNull
    private LocalDate workDate;
    @NotBlank
    private String clockType;
    @NotNull
    private LocalDateTime clockTime;
    @NotBlank
    private String reason;
}
