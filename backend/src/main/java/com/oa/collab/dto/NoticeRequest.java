package com.oa.collab.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NoticeRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String type;
    private Long deptId;
    private Integer pinned;
}
