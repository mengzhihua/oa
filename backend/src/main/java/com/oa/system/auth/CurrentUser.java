package com.oa.system.auth;

import java.util.Collections;
import java.util.Set;

public final class CurrentUser {
    private static final ThreadLocal<TokenService.Principal> HOLDER = new ThreadLocal<>();

    private CurrentUser() {
    }

    public static void set(TokenService.Principal principal) {
        HOLDER.set(principal);
    }

    public static TokenService.Principal get() {
        return HOLDER.get();
    }

    public static Long id() {
        return get() == null ? null : get().getUserId();
    }

    public static String username() {
        return get() == null ? null : get().getUsername();
    }

    public static Set<String> roles() {
        return get() == null ? Collections.<String>emptySet() : get().getRoles();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
