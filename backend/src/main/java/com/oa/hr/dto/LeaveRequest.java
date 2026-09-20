package com.oa.hr.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequest {
    private LocalDate leaveDate;
    private String reason;
}
