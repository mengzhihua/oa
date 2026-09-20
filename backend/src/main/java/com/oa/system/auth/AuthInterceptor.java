package com.oa.system.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.R;
import com.oa.system.entity.SysOpLog;
import com.oa.system.service.SysOpLogService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    private final AccessPolicy accessPolicy;
    private final ObjectMapper objectMapper;
    private final SysOpLogService opLogService;
    private final JdbcTemplate jdbc;

    public AuthInterceptor(TokenService tokenService, AccessPolicy accessPolicy,
                           ObjectMapper objectMapper, SysOpLogService opLogService,
                           JdbcTemplate jdbc) {
        this.tokenService = tokenService;
        this.accessPolicy = accessPolicy;
        this.objectMapper = objectMapper;
        this.opLogService = opLogService;
        this.jdbc = jdbc;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || "/api/auth/login".equals(path)
                || "/api/oauth/authorize".equals(path)
                || "/api/oauth/token".equals(path)
                || "/api/oauth/introspect".equals(path)
                || "/api/oauth/revoke".equals(path)
                || "/api/oauth/userinfo".equals(path)
                || path.startsWith("/api/oauth/.well-known/")
                || path.startsWith("/actuator/")
                || path.startsWith("/api/open/")) {
            return true;
        }
        TokenService.Principal principal = tokenService.parse(bearer(request));
        if (principal == null) {
            return reject(response, 401, "未登录或登录已过期");
        }
        if (!active(principal.getUserId())) {
            return reject(response, 401, "账号已停用");
        }
        if (!accessPolicy.allowed(request.getMethod(), path, principal.getRoles())) {
            return reject(response, 403, "无权访问");
        }
        CurrentUser.set(principal);
        return true;
    }

    private boolean active(Long userId) {
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception exception) {
        try {
            if (CurrentUser.id() != null && isMutation(request.getMethod())) {
                SysOpLog log = new SysOpLog();
                log.setUsername(CurrentUser.username());
                log.setMethod(request.getMethod());
                log.setPath(request.getRequestURI());
                log.setResponseBody(String.valueOf(response.getStatus()));
                opLogService.save(log);
            }
        } finally {
            CurrentUser.clear();
        }
    }

    private boolean reject(HttpServletResponse response, int status, String message)
            throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(status, message)));
        return false;
    }

    private static boolean isMutation(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    public static String bearer(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        if (value != null && value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return value.substring(7).trim();
        }
        return request.getParameter("oa_token");
    }
}
