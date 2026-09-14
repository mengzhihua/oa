package com.oa.system.auth;
import java.util.*;
public final class CurrentUser {
    private static final ThreadLocal<TokenService.Principal> HOLDER = new ThreadLocal<TokenService.Principal>();
    private CurrentUser(){}
    public static void set(TokenService.Principal p){HOLDER.set(p);} public static TokenService.Principal get(){return HOLDER.get();} public static Long id(){return get()==null?null:get().getUserId();} public static void clear(){HOLDER.remove();}
}
