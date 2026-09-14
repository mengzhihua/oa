package com.oa.system.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class MenuAssignRequest {
    @NotNull(message = "菜单不能为空")
    private List<Long> menuIds;
}
