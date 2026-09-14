package com.oa.attendance.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleBatchRequest {
    @NotEmpty
    private List<Long> employeeIds;
    @NotNull
    private LocalDate from;
    @NotNull
    private LocalDate to;
    @NotNull
    private Long shiftId;
}
