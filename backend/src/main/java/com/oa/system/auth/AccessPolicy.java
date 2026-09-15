package com.oa.system.auth;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AccessPolicy {
    private final List<Rule> rules = Arrays.asList(
            Rule.any("/api/payroll/my/", "GET", "POST", "PUT", "DELETE"),
            Rule.path("/api/payroll/slips/mine", "GET"),
            Rule.prefix("/api/attendance/balances/mine", "*"),
            Rule.suffix("/api/attendance/", "/mine", "*"),
            Rule.prefix("/api/attendance/clock", "*"),
            Rule.suffix("/api/attendance/requests/", "/cancel", "*"),
            Rule.path("/api/attendance/leaves", "POST"),
            Rule.path("/api/attendance/overtimes", "POST"),
            Rule.path("/api/attendance/patches", "POST"),
            Rule.path("/api/attendance/trips", "POST"),
            Rule.prefix("/api/attendance/shifts", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/attendance/shifts", "*", "HR", "ADMIN"),
            Rule.prefix("/api/attendance/schedules", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/attendance/schedules", "*", "HR", "ADMIN"),
            Rule.prefix("/api/attendance/holidays", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/attendance/holidays", "*", "HR", "ADMIN"),
            Rule.prefix("/api/attendance/monthly", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/attendance/monthly", "*", "HR", "ADMIN"),
            Rule.prefix("/api/attendance/daily", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/attendance/daily", "*", "HR", "ADMIN"),
            Rule.suffix("/api/collab/expenses/", "/pay", "*", "FINANCE", "ADMIN"),
            Rule.path("/api/notices", "GET", "ADMIN", "HR"),
            Rule.prefix("/api/oauth/clients", "*", "ADMIN"),
            Rule.prefix("/api/system/oplogs", "*", "ADMIN"),
            Rule.prefix("/api/system/users", "*", "ADMIN", "HR"),
            Rule.prefix("/api/system/", "*", "ADMIN"),
            Rule.prefix("/api/org/", "GET"),
            Rule.prefix("/api/org/", "*", "HR", "ADMIN"),
            Rule.prefix("/api/hr/employees", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/hr/contracts", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/hr/changes", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.suffix("/api/hr/", "/export", "GET", "HR", "ADMIN", "MANAGER"),
            Rule.prefix("/api/hr/", "GET", "HR", "ADMIN"),
            Rule.prefix("/api/hr/", "*", "HR", "ADMIN"),
            Rule.suffix("/api/payroll/periods/", "/approve", "*", "FINANCE", "ADMIN"),
            Rule.suffix("/api/payroll/periods/", "/pay", "*", "FINANCE", "ADMIN"),
            Rule.prefix("/api/payroll/", "*", "HR", "FINANCE", "ADMIN")
    );

    public boolean allowed(String method, String path, Set<String> roles) {
        for (Rule rule : rules) {
            if (rule.matches(method, path)) {
                return rule.roles.isEmpty() || rule.roles.stream().anyMatch(roles::contains);
            }
        }
        return true;
    }

    private static class Rule {
        private final String prefix;
        private final String suffix;
        private final String exact;
        private final Set<String> methods;
        private final Set<String> roles;

        private Rule(String prefix, String suffix, String exact, Set<String> methods,
                     Set<String> roles) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.exact = exact;
            this.methods = methods;
            this.roles = roles;
        }

        private static Rule prefix(String prefix, String method, String... roles) {
            return new Rule(prefix, null, null, methods(method), roleSet(roles));
        }

        private static Rule path(String path, String method, String... roles) {
            return new Rule(null, null, path, methods(method), roleSet(roles));
        }

        private static Rule suffix(String prefix, String suffix, String method,
                                   String... roles) {
            return new Rule(prefix, suffix, null, methods(method), roleSet(roles));
        }

        private static Rule any(String prefix, String... methods) {
            return new Rule(prefix, null, null, methods(methods), Collections.emptySet());
        }

        private boolean matches(String method, String path) {
            boolean pathMatches = exact != null ? exact.equals(path) : path.startsWith(prefix)
                    && (suffix == null || path.endsWith(suffix));
            return pathMatches && (methods.isEmpty() || methods.contains("*")
                    || methods.contains(method.toUpperCase()));
        }

        private static Set<String> methods(String... methods) {
            return new HashSet<>(Arrays.asList(methods));
        }

        private static Set<String> roleSet(String... roles) {
            return new HashSet<>(Arrays.asList(roles));
        }
    }
}
