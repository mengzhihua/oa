package com.oa.oauth.controller;

import com.oa.common.BizException;
import com.oa.common.R;
import com.oa.oauth.dto.OauthClientRequest;
import com.oa.oauth.vo.OauthClientView;
import com.oa.system.auth.AuthInterceptor;
import com.oa.system.auth.CurrentUser;
import com.oa.system.auth.PasswordHasher;
import com.oa.system.auth.TokenService;
import com.oa.system.auth.UserAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {
    private static final Logger log = LoggerFactory.getLogger(OAuthController.class);
    private final JdbcTemplate jdbc;
    private final TokenService tokenService;
    private final UserAccessService userAccessService;

    public OAuthController(JdbcTemplate jdbc, TokenService tokenService,
                           UserAccessService userAccessService) {
        this.jdbc = jdbc;
        this.tokenService = tokenService;
        this.userAccessService = userAccessService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<?> authorize(
            @RequestParam String response_type,
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam(required = false, defaultValue = "openid profile") String scope,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code_challenge,
            @RequestParam(required = false) String code_challenge_method,
            HttpServletRequest request) {
        TokenService.Principal principal = tokenService.parse(AuthInterceptor.bearer(request));
        if (principal == null || !activeUser(principal.getUserId())) {
            Map<String, String> result = new HashMap<>();
            result.put("loginUrl", "/login?redirect="
                    + URLEncoder.encode(redirect_uri, StandardCharsets.UTF_8));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        if (!"code".equals(response_type)) {
            return ResponseEntity.badRequest().body(error("unsupported_response_type", "仅支持 code"));
        }
        Map<String, Object> client = client(client_id);
        if (client == null || !contains(string(client, "REDIRECT_URIS"), redirect_uri)) {
            return ResponseEntity.badRequest().body(error("invalid_request", "回调地址未登记"));
        }
        validateScope(scope, client);
        String code = UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO oauth_authorization_code "
                        + "(code, client_id, user_id, redirect_uri, scope, code_challenge, "
                        + "code_challenge_method, expires_at, used) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                code, client_id, principal.getUserId(), redirect_uri, scope, code_challenge,
                code_challenge_method, LocalDateTime.now().plusMinutes(10));
        String redirect = redirect_uri + (redirect_uri.contains("?") ? "&" : "?")
                + "code=" + code;
        if (state != null) {
            redirect += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok(Collections.singletonMap("redirectUrl", redirect));
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@RequestParam Map<String, String> form,
                                   HttpServletRequest request) {
        try {
            String clientId = form.get("client_id");
            String clientSecret = form.get("client_secret");
            String basic = request.getHeader("Authorization");
            if ((clientId == null || clientSecret == null)
                    && basic != null && basic.startsWith("Basic ")) {
                String value = new String(Base64.getDecoder().decode(basic.substring(6)),
                        StandardCharsets.UTF_8);
                int split = value.indexOf(':');
                clientId = value.substring(0, split);
                clientSecret = value.substring(split + 1);
            }
            Map<String, Object> client = client(clientId);
            if (client == null || !PasswordHasher.verify(clientSecret,
                    string(client, "CLIENT_SECRET_HASH"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(error("invalid_client", "客户端认证失败"));
            }
            String grantType = form.get("grant_type");
            if (!contains(string(client, "GRANT_TYPES"), grantType)) {
                return ResponseEntity.badRequest()
                        .body(error("unauthorized_client", "客户端未授权该授权类型"));
            }
            if ("authorization_code".equals(grantType)) {
                return ResponseEntity.ok(exchangeCode(form, clientId, client));
            }
            if ("refresh_token".equals(grantType)) {
                return ResponseEntity.ok(refresh(form.get("refresh_token"), clientId, client));
            }
            if ("client_credentials".equals(grantType)) {
                return ResponseEntity.ok(issue(null, clientId, requestedScope(form.get("scope"), client),
                        client));
            }
            if ("password".equals(grantType)) {
                return ResponseEntity.ok(passwordGrant(form, clientId, client));
            }
            return ResponseEntity.badRequest()
                    .body(error("unsupported_grant_type", "不支持的授权类型"));
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(error("invalid_grant", exception.getMessage()));
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> userinfo(HttpServletRequest request) {
        String accessToken = AuthInterceptor.bearer(request);
        TokenService.Principal principal = validOauthToken(accessToken);
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(error("invalid_token", "访问令牌无效"));
        }
        Map<String, Object> user = jdbc.queryForMap(
                "SELECT u.id, u.username, u.real_name, e.email, e.mobile, e.employee_no, "
                        + "e.dept_id, d.name dept_name FROM sys_user u "
                        + "LEFT JOIN hr_employee e ON e.id = u.employee_id "
                        + "LEFT JOIN org_dept d ON d.id = e.dept_id WHERE u.id = ?",
                principal.getUserId());
        String scope = tokenScope(accessToken);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sub", principal.getUserId());
        result.put("username", value(user, "USERNAME"));
        if (hasScope(scope, "profile")) {
            result.put("name", value(user, "REAL_NAME"));
            result.put("employeeNo", value(user, "EMPLOYEE_NO"));
            result.put("deptId", value(user, "DEPT_ID"));
            result.put("deptName", value(user, "DEPT_NAME"));
        }
        if (hasScope(scope, "email")) {
            result.put("email", value(user, "EMAIL"));
        }
        if (hasScope(scope, "phone")) {
            result.put("mobile", value(user, "MOBILE"));
        }
        if (hasScope(scope, "roles")) {
            result.put("roles", jdbc.queryForList(
                    "SELECT r.code FROM sys_role r JOIN sys_user_role ur "
                            + "ON ur.role_id = r.id WHERE ur.user_id = ?",
                    principal.getUserId()));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/introspect")
    public Map<String, Object> introspect(@RequestParam String token,
                                          HttpServletRequest request) {
        if (!authenticateClient(request)) {
            return Collections.singletonMap("active", false);
        }
        TokenService.Principal principal = validOauthToken(token);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("active", principal != null);
        if (principal != null) {
            result.put("sub", principal.getUserId());
            result.put("client_id", principal.getClientId());
            result.put("scope", tokenScope(token));
            result.put("exp", ((Number) principal.getClaims().get("exp")).longValue() / 1000);
        }
        return result;
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revoke(@RequestParam String token, HttpServletRequest request) {
        String clientId = authenticatedClientId(request);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(error("invalid_client", "客户端认证失败"));
        }
        List<Map<String, Object>> ownerRows = jdbc.queryForList(
                "SELECT client_id FROM oauth_token "
                        + "WHERE access_token = ? OR refresh_token = ?", token, token);
        if (!ownerRows.isEmpty()
                && !clientId.equals(value(ownerRows.get(0), "CLIENT_ID"))) {
            log.warn("OAuth revoke ownership mismatch: requester={}, tokenOwner={}",
                    clientId, value(ownerRows.get(0), "CLIENT_ID"));
        }
        jdbc.update("UPDATE oauth_token SET revoked = 1 "
                        + "WHERE (access_token = ? OR refresh_token = ?) AND client_id = ?",
                token, token, clientId);
        return ResponseEntity.ok(Collections.singletonMap("revoked", true));
    }

    @GetMapping("/clients")
    public R<List<OauthClientView>> clients() {
        return R.ok(jdbc.query("SELECT id, client_id, client_name, redirect_uris, "
                        + "grant_types, scopes, access_token_ttl, refresh_token_ttl, status "
                        + "FROM oauth_client ORDER BY id",
                (result, row) -> {
                    OauthClientView view = new OauthClientView();
                    view.setId(result.getLong("id"));
                    view.setClientId(result.getString("client_id"));
                    view.setClientName(result.getString("client_name"));
                    view.setRedirectUris(result.getString("redirect_uris"));
                    view.setGrantTypes(result.getString("grant_types"));
                    view.setScopes(result.getString("scopes"));
                    view.setAccessTokenTtl(result.getInt("access_token_ttl"));
                    view.setRefreshTokenTtl(result.getInt("refresh_token_ttl"));
                    view.setStatus(result.getInt("status"));
                    return view;
                }));
    }

    @RequestMapping(value = "/clients", method = {RequestMethod.POST, RequestMethod.PUT})
    public R<Map<String, String>> saveClient(@Valid @RequestBody OauthClientRequest request) {
        boolean suppliedSecret = request.getClientSecret() != null;
        String secret = suppliedSecret ? request.getClientSecret() : UUID.randomUUID().toString();
        if (request.getId() == null) {
            jdbc.update("INSERT INTO oauth_client "
                            + "(client_id, client_secret_hash, client_name, redirect_uris, "
                            + "grant_types, scopes, access_token_ttl, refresh_token_ttl, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    request.getClientId(), PasswordHasher.hash(secret), request.getClientName(),
                    request.getRedirectUris(), defaultValue(request.getGrantTypes(),
                            "authorization_code,refresh_token"),
                    defaultValue(request.getScopes(), "openid profile"),
                    request.getAccessTokenTtl() == null ? 7200 : request.getAccessTokenTtl(),
                    request.getRefreshTokenTtl() == null ? 2592000 : request.getRefreshTokenTtl(),
                    request.getStatus() == null ? 1 : request.getStatus());
        } else {
            if (suppliedSecret) {
                jdbc.update("UPDATE oauth_client SET client_secret_hash = ?, client_name = ?, "
                                + "redirect_uris = ?, grant_types = ?, scopes = ?, access_token_ttl = ?, "
                                + "refresh_token_ttl = ?, status = ? WHERE id = ?",
                        PasswordHasher.hash(secret), request.getClientName(), request.getRedirectUris(),
                        request.getGrantTypes(), request.getScopes(), request.getAccessTokenTtl(),
                        request.getRefreshTokenTtl(), request.getStatus(), request.getId());
            } else {
                jdbc.update("UPDATE oauth_client SET client_name = ?, redirect_uris = ?, "
                                + "grant_types = ?, scopes = ?, access_token_ttl = ?, "
                                + "refresh_token_ttl = ?, status = ? WHERE id = ?",
                        request.getClientName(), request.getRedirectUris(),
                        request.getGrantTypes(), request.getScopes(), request.getAccessTokenTtl(),
                        request.getRefreshTokenTtl(), request.getStatus(), request.getId());
            }
        }
        Map<String, String> response = new LinkedHashMap<>();
        if (request.getId() == null || suppliedSecret) {
            response.put("clientSecret", secret);
        }
        return R.ok(response);
    }

    private Map<String, Object> exchangeCode(Map<String, String> form, String clientId,
                                             Map<String, Object> client) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM oauth_authorization_code WHERE code = ? AND client_id = ? "
                        + "AND used = 0 AND expires_at > ?", form.get("code"), clientId,
                LocalDateTime.now());
        if (rows.isEmpty()) {
            throw new BizException("授权码无效或已使用");
        }
        Map<String, Object> code = rows.get(0);
        if (form.get("redirect_uri") != null
                && !form.get("redirect_uri").equals(value(code, "REDIRECT_URI"))) {
            throw new BizException("回调地址不一致");
        }
        String challenge = value(code, "CODE_CHALLENGE");
        if (challenge != null && !challenge.trim().isEmpty()) {
            String verifier = form.get("code_verifier");
            if (verifier == null || !challenge.equals(base64Url(sha256(verifier)))) {
                throw new BizException("PKCE校验失败");
            }
        }
        validateScope(value(code, "SCOPE"), client);
        int updated = jdbc.update("UPDATE oauth_authorization_code SET used = 1 "
                        + "WHERE id = ? AND used = 0", value(code, "ID"));
        if (updated != 1) {
            throw new BizException("授权码已使用或无效");
        }
        return issue(number(code, "USER_ID"), clientId, value(code, "SCOPE"), client);
    }

    private Map<String, Object> refresh(String refreshToken, String clientId,
                                        Map<String, Object> client) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM oauth_token WHERE refresh_token = ? AND client_id = ? "
                        + "AND revoked = 0 AND refresh_expires_at > ?",
                refreshToken, clientId, LocalDateTime.now());
        if (rows.isEmpty()) {
            throw new BizException("刷新令牌无效或已过期");
        }
        Map<String, Object> token = rows.get(0);
        validateScope(value(token, "SCOPE"), client);
        jdbc.update("UPDATE oauth_token SET revoked = 1 WHERE id = ?", value(token, "ID"));
        return issue(number(token, "USER_ID"), clientId, value(token, "SCOPE"), client);
    }

    private Map<String, Object> issue(Long userId, String clientId, String scope,
                                      Map<String, Object> client) {
        if (userId != null && !userAccessService.isActive(userId)) {
            throw new BizException("账号已停用");
        }
        int accessTtl = Integer.parseInt(string(client, "ACCESS_TOKEN_TTL"));
        int refreshTtl = Integer.parseInt(string(client, "REFRESH_TOKEN_TTL"));
        String username = userId == null ? "client" : jdbc.queryForObject(
                "SELECT username FROM sys_user WHERE id = ?", String.class, userId);
        String accessToken = tokenService.issueOAuth(userId, username, clientId,
                accessTtl * 1000L);
        String refreshToken = userId == null ? null : UUID.randomUUID().toString();
        jdbc.update("INSERT INTO oauth_token "
                        + "(access_token, refresh_token, client_id, user_id, scope, "
                        + "access_expires_at, refresh_expires_at, revoked) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, 0)",
                accessToken, refreshToken, clientId, userId, scope,
                LocalDateTime.now().plusSeconds(accessTtl),
                refreshToken == null ? null : LocalDateTime.now().plusSeconds(refreshTtl));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("access_token", accessToken);
        result.put("token_type", "Bearer");
        result.put("expires_in", accessTtl);
        if (refreshToken != null) {
            result.put("refresh_token", refreshToken);
        }
        result.put("scope", scope);
        return result;
    }

    private Map<String, Object> passwordGrant(Map<String, String> form, String clientId,
                                              Map<String, Object> client) {
        Map<String, Object> user = jdbc.queryForMap(
                "SELECT id, password_hash FROM sys_user WHERE username = ? AND status = 1",
                form.get("username"));
        if (!PasswordHasher.verify(form.get("password"), value(user, "PASSWORD_HASH"))) {
            throw new BizException("用户名或密码错误");
        }
        Long userId = number(user, "ID");
        return issue(userId, clientId, requestedScope(form.get("scope"), client), client);
    }

    private TokenService.Principal validOauthToken(String token) {
        TokenService.Principal principal = tokenService.parse(token);
        if (principal == null || !"oauth".equals(principal.getType())
                || !activeUser(principal.getUserId())) {
            return null;
        }
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM oauth_token WHERE access_token = ? AND revoked = 0 "
                        + "AND access_expires_at > ?", Integer.class, token, LocalDateTime.now());
        return count != null && count > 0 ? principal : null;
    }

    private boolean activeUser(Long userId) {
        if (userId == null) {
            return true;
        }
        return userAccessService.isActive(userId);
    }

    private String tokenScope(String token) {
        return jdbc.queryForObject("SELECT scope FROM oauth_token WHERE access_token = ?",
                String.class, token);
    }

    private boolean authenticateClient(HttpServletRequest request) {
        return authenticatedClientId(request) != null;
    }

    private String authenticatedClientId(HttpServletRequest request) {
        String basic = request.getHeader("Authorization");
        if (basic == null || !basic.startsWith("Basic ")) {
            return null;
        }
        String value = new String(Base64.getDecoder().decode(basic.substring(6)),
                StandardCharsets.UTF_8);
        int split = value.indexOf(':');
        if (split < 1) {
            return null;
        }
        String clientId = value.substring(0, split);
        Map<String, Object> client = client(clientId);
        return client != null && PasswordHasher.verify(value.substring(split + 1),
                string(client, "CLIENT_SECRET_HASH")) ? clientId : null;
    }

    private Map<String, Object> client(String clientId) {
        if (clientId == null) {
            return null;
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM oauth_client WHERE client_id = ? AND status = 1", clientId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static boolean contains(String csv, String value) {
        if (csv == null || value == null) {
            return false;
        }
        for (String item : csv.split(",")) {
            if (item.trim().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private String requestedScope(String requested, Map<String, Object> client) {
        String scope = requested == null || requested.trim().isEmpty()
                ? string(client, "SCOPES").replace(',', ' ') : requested;
        validateScope(scope, client);
        return scope;
    }

    private void validateScope(String scope, Map<String, Object> client) {
        String registered = string(client, "SCOPES");
        java.util.Set<String> allowed = new java.util.HashSet<>(
                java.util.Arrays.asList(registered.replace(',', ' ').trim().split("\\s+")));
        for (String requested : scope.trim().split("\\s+")) {
            if (!allowed.contains(requested)) {
                throw new BizException("scope 超出客户端授权范围");
            }
        }
    }

    private static boolean hasScope(String scope, String expected) {
        return scope != null && java.util.Arrays.asList(scope.split("\\s+")).contains(expected);
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static Map<String, String> error(String code, String message) {
        Map<String, String> result = new HashMap<>();
        result.put("error", code);
        result.put("error_description", message);
        return result;
    }

    private static String string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Long number(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
