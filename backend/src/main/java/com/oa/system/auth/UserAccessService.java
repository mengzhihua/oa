package com.oa.system.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserAccessService {
    private final JdbcTemplate jdbc;

    public UserAccessService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isActive(Long userId) {
        if (userId == null) {
            return false;
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user u "
                        + "LEFT JOIN hr_employee e ON e.id = u.employee_id "
                        + "WHERE u.id = ? AND u.status = 1 "
                        + "AND (e.id IS NULL OR e.employment_status <> 'LEAVING' "
                        + "OR e.leave_date IS NULL OR e.leave_date >= ?)",
                Integer.class, userId, LocalDate.now());
        return count != null && count > 0;
    }
}
