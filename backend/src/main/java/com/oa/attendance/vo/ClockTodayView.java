package com.oa.attendance.vo;

import com.oa.attendance.entity.AttClockRecord;
import com.oa.attendance.entity.AttDaily;
import lombok.Data;

import java.util.List;

@Data
public class ClockTodayView {
    private List<AttClockRecord> records;
    private AttDaily daily;
}
