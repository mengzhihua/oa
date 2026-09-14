package com.oa.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_op_log")
public class SysOpLog extends BaseEntity {
    private String username;
    private String method;
    private String path;
    private String requestBody;
    private String responseBody;
}
