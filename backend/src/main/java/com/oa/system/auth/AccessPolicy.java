package com.oa.system.auth;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AccessPolicy {
    public boolean allowed(String method, String path, Set<String> roles) {
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }
        if (roles.contains("ADMIN")) {
            return true;
        }
        if (path.startsWith("/api/system/") || path.startsWith("/api/org/")
                || path.startsWith("/api/hr/")) {
            return roles.contains("HR");
        }
        if (path.startsWith("/api/oauth/clients")) {
            return roles.contains("ADMIN");
        }
        if (path.startsWith("/api/workflow/tasks/")) {
            return roles.contains("MANAGER") || roles.contains("HR")
                    || roles.contains("FINANCE");
        }
        return true;
    }
}
