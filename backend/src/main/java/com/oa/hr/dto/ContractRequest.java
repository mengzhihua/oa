package com.oa.hr.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class ContractRequest {
    @NotNull
    private Long employeeId;

    @NotBlank
    private String contractNo;

    @NotBlank
    private String type;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private LocalDate signDate;

    private String status;
}
