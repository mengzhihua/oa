package com.oa.attendance.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class ClockRequest {
    @Pattern(regexp = "IN|OUT", message = "打卡类型只能是 IN 或 OUT")
    private String clockType;
    private String source;
    private String device;
    private String remark;
}
