package com.oa.system.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class RoleAssignRequest {
    @NotNull(message = "角色不能为空")
    private List<Long> roleIds;
}
