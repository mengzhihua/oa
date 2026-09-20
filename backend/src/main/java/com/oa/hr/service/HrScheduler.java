package com.oa.hr.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HrScheduler {
    private final JdbcTemplate jdbc;

    public HrScheduler(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(cron = "${oa.hr.leave-cron:0 10 0 * * *}")
    @Transactional
    public void scheduledExpireLeaving() {
        expireLeaving();
    }

    @Transactional
    public int expireLeaving() {
        int users = jdbc.update("UPDATE sys_user SET status = 0 WHERE employee_id IN "
                + "(SELECT id FROM hr_employee WHERE employment_status = 'LEAVING' "
                + "AND leave_date < CURRENT_DATE)");
        int employees = jdbc.update("UPDATE hr_employee SET employment_status = 'LEFT' "
                + "WHERE employment_status = 'LEAVING' AND leave_date < CURRENT_DATE");
        return users + employees;
    }
}
