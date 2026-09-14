package com.oa.system.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.util.*;

@Component
public class TokenService {
    private final byte[] key; private final long ttl; private final ObjectMapper mapper = new ObjectMapper();
    public TokenService(@Value("${oa.auth.secret:}") String secret, @Value("${oa.auth.token-ttl:12h}") Duration ttl) {
        if (secret == null || secret.trim().isEmpty()) { key = new byte[32]; new SecureRandom().nextBytes(key); } else key = secret.getBytes(StandardCharsets.UTF_8); this.ttl = ttl.toMillis();
    }
    public String issue(Long uid, String username) { Map<String,Object> p = new HashMap<String,Object>(); p.put("sub",uid); p.put("username",username); p.put("exp",System.currentTimeMillis()+ttl); p.put("typ","oa"); return issue(p); }
    public String issueOAuth(Long uid, String username, String clientId, long expires) { Map<String,Object> p = new HashMap<String,Object>(); p.put("sub",uid); p.put("username",username); p.put("exp",System.currentTimeMillis()+expires); p.put("typ","oauth"); p.put("cid",clientId); return issue(p); }
    public String issue(Map<String,Object> p) { try { String h = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8)); String b = b64(mapper.writeValueAsBytes(p)); return h+"."+b+"."+b64(sign(h+"."+b)); } catch (Exception e) { throw new IllegalStateException(e); } }
    public Principal parse(String token) {
        try { if (token == null) return null; String[] x=token.split("\\."); if(x.length!=3 || !MessageDigest.isEqual(Base64.getUrlDecoder().decode(x[2]),sign(x[0]+"."+x[1]))) return null; Map<String,Object> p=mapper.readValue(Base64.getUrlDecoder().decode(x[1]),new TypeReference<Map<String,Object>>(){}); long exp=((Number)p.get("exp")).longValue(); if(exp<System.currentTimeMillis()) return null; return new Principal(((Number)p.get("sub")).longValue(),String.valueOf(p.get("username")),String.valueOf(p.get("typ")),p); } catch(Exception e){return null;}
    }
    private byte[] sign(String s) { try { Mac m=Mac.getInstance("HmacSHA256");m.init(new SecretKeySpec(key,"HmacSHA256"));return m.doFinal(s.getBytes(StandardCharsets.UTF_8)); } catch(Exception e){throw new IllegalStateException(e);} }
    private static String b64(byte[] b){return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
    public static class Principal { private final Long userId; private final String username, type; private final Map<String,Object> claims; public Principal(Long id,String u,String t,Map<String,Object> c){userId=id;username=u;type=t;claims=c;} public Long getUserId(){return userId;} public String getUsername(){return username;} public String getType(){return type;} public Map<String,Object> getClaims(){return claims;} }
}
