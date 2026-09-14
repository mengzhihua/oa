package com.oa.system.auth;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.R;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.*;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokens; private final ObjectMapper mapper; private final AccessPolicy policy; private final JdbcTemplate jdbc;
    public AuthInterceptor(TokenService t,ObjectMapper m,AccessPolicy p,JdbcTemplate j){tokens=t;mapper=m;policy=p;jdbc=j;}
    public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) throws Exception {
        String path=req.getRequestURI(); if("OPTIONS".equalsIgnoreCase(req.getMethod())||path.equals("/api/auth/login")||path.startsWith("/api/oauth/"))return true;
        String h=req.getHeader("Authorization"); String token=h!=null&&h.regionMatches(true,0,"Bearer ",0,7)?h.substring(7).trim():req.getParameter("oa_token"); TokenService.Principal p=tokens.parse(token);
        if(p==null)return reject(res,401,"未登录或登录已过期"); if(!policy.allowed(p.getUserId(),req.getMethod(),path))return reject(res,403,"无权访问"); CurrentUser.set(p); return true;
    }
    public void afterCompletion(HttpServletRequest r,HttpServletResponse s,Object h,Exception e){try{if(CurrentUser.id()!=null&&("POST".equalsIgnoreCase(r.getMethod())||"PUT".equalsIgnoreCase(r.getMethod())||"DELETE".equalsIgnoreCase(r.getMethod())))jdbc.update("INSERT INTO sys_op_log(username,method,path,response_body) SELECT username,?,?,? FROM sys_user WHERE id=?",r.getMethod(),r.getRequestURI(),String.valueOf(s.getStatus()),CurrentUser.id());}finally{CurrentUser.clear();}}
    private boolean reject(HttpServletResponse r,int status,String msg)throws Exception{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");r.getWriter().write(mapper.writeValueAsString(R.fail(status,msg)));return false;}
}
