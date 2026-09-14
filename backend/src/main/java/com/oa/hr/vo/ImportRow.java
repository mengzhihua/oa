package com.oa.hr.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportRow {
    private int row;
    private boolean success;
    private String employeeNo;
    private String message;
}
