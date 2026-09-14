package com.oa.collab.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MeetingRoomRequest {
    @NotBlank
    private String name;
    private String location;
    private Integer capacity;
    private String equipment;
    private String status;
}
