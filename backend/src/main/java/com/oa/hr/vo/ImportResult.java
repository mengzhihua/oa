package com.oa.hr.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int successCount;
    private int failureCount;
    private List<ImportRow> rows = new ArrayList<>();
}
