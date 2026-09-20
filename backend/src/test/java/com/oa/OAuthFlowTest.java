package com.oa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OAuthFlowTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void OAuth全链路与安全校验() throws Exception {
        String oaToken = login();
        String code = authorize(oaToken, null);
        JsonNode exchanged = objectMapper.readTree(exchange(code, null));
        String accessToken = exchanged.get("access_token").asText();

        mockMvc.perform(get("/api/oauth/userinfo")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").exists())
                .andExpect(jsonPath("$.username").value("zhangsan"));

        String refreshToken = exchanged.get("refresh_token").asText();
        JsonNode first = objectMapper.readTree(refresh(refreshToken));
        String rotatedRefreshToken = first.get("refresh_token").asText();
        String rotatedAccessToken = first.get("access_token").asText();
        mockMvc.perform(post("/api/oauth/introspect")
                        .param("token", rotatedAccessToken)
                        .header("Authorization", basic("sap-client", "sap-client-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        String second = refresh(rotatedRefreshToken);
        mockMvc.perform(post("/api/oauth/introspect")
                        .param("token", rotatedAccessToken)
                        .header("Authorization", basic("sap-client", "sap-client-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        JsonNode secondToken = objectMapper.readTree(second);
        mockMvc.perform(post("/api/oauth/revoke")
                        .param("token", secondToken.get("access_token").asText())
                        .header("Authorization", basic("sap-client", "sap-client-secret")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/oauth/introspect")
                        .param("token", secondToken.get("access_token").asText())
                        .header("Authorization", basic("sap-client", "sap-client-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    public void PKCE错误验证码与授权码重放失败() throws Exception {
        String oaToken = login();
        String verifier = "correct-verifier";
        String code = authorize(oaToken, "challenge");
        mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "authorization_code")
                        .param("client_id", "sap-client")
                        .param("client_secret", "sap-client-secret")
                        .param("code", code)
                        .param("redirect_uri", "http://localhost:5175/sso/callback")
                        .param("code_verifier", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));

        String validCode = authorize(oaToken, "challenge");
        mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "authorization_code")
                        .param("client_id", "sap-client")
                        .param("client_secret", "sap-client-secret")
                        .param("code", validCode)
                        .param("redirect_uri", "http://localhost:5175/sso/callback")
                        .param("code_verifier", verifier))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "authorization_code")
                        .param("client_id", "sap-client")
                        .param("client_secret", "sap-client-secret")
                        .param("code", validCode)
                        .param("redirect_uri", "http://localhost:5175/sso/callback")
                        .param("code_verifier", verifier))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void 错误客户端密钥返回401() throws Exception {
        mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "authorization_code")
                        .param("client_id", "sap-client")
                        .param("client_secret", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    public void 账号失效后刷新令牌兑换失败() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "password")
                        .param("client_id", "sap-client")
                        .param("client_secret", "sap-client-secret")
                        .param("username", "zhangsan")
                        .param("password", "emp123"))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("refresh_token").asText();
        Long employeeId = jdbc.queryForObject(
                "SELECT employee_id FROM sys_user WHERE username = 'zhangsan'",
                Long.class);
        String oldStatus = jdbc.queryForObject(
                "SELECT employment_status FROM hr_employee WHERE id = ?",
                String.class, employeeId);
        try {
            jdbc.update("UPDATE hr_employee SET employment_status = 'LEAVING', "
                            + "leave_date = ? WHERE id = ?",
                    LocalDate.now().minusDays(1), employeeId);
            mockMvc.perform(post("/api/oauth/token")
                            .param("grant_type", "refresh_token")
                            .param("client_id", "sap-client")
                            .param("client_secret", "sap-client-secret")
                            .param("refresh_token", refreshToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_grant"));
        } finally {
            jdbc.update("UPDATE hr_employee SET employment_status = ?, leave_date = NULL "
                            + "WHERE id = ?", oldStatus, employeeId);
        }
    }

    @Test
    public void 客户端未登记授权类型返回unauthorizedClient() throws Exception {
        mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "client_credentials")
                        .param("client_id", "srm-client")
                        .param("client_secret", "srm-client-secret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unauthorized_client"));
    }

    @Test
    public void 撤销令牌必须匹配当前客户端() throws Exception {
        String oaToken = login();
        JsonNode exchanged = objectMapper.readTree(exchange(authorize(oaToken, null), null));
        String accessToken = exchanged.get("access_token").asText();

        mockMvc.perform(post("/api/oauth/revoke")
                        .param("token", accessToken)
                        .header("Authorization", basic("srm-client", "srm-client-secret")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/oauth/introspect")
                        .param("token", accessToken)
                        .header("Authorization", basic("sap-client", "sap-client-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(post("/api/oauth/revoke")
                        .param("token", accessToken)
                        .header("Authorization", basic("sap-client", "sap-client-secret")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/oauth/introspect")
                        .param("token", accessToken)
                        .header("Authorization", basic("sap-client", "sap-client-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"zhangsan\",\"password\":\"emp123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("token").asText();
    }

    private String authorize(String oaToken, String challenge) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get("/api/oauth/authorize")
                        .param("response_type", "code")
                        .param("client_id", "sap-client")
                        .param("redirect_uri", "http://localhost:5175/sso/callback")
                        .param("scope", "openid profile email phone roles")
                        .header("Authorization", "Bearer " + oaToken);
        if (challenge != null) {
            request.param("code_challenge_method", "S256");
            request.param("code_challenge", base64Url("correct-verifier"));
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        String redirect = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("redirectUrl").asText();
        String query = URI.create(redirect).getQuery();
        for (String item : query.split("&")) {
            if (item.startsWith("code=")) {
                return URLDecoder.decode(item.substring(5), StandardCharsets.UTF_8.name());
            }
        }
        throw new IllegalStateException("未返回授权码");
    }

    private String exchange(String code, String verifier) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                post("/api/oauth/token")
                        .param("grant_type", "authorization_code")
                        .param("client_id", "sap-client")
                        .param("client_secret", "sap-client-secret")
                        .param("code", code)
                        .param("redirect_uri", "http://localhost:5175/sso/callback");
        if (verifier != null) {
            request.param("code_verifier", verifier);
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private String refresh(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "refresh_token")
                        .param("client_id", "sap-client")
                        .param("client_secret", "sap-client-secret")
                        .param("refresh_token", refreshToken))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private String basic(String clientId, String secret) {
        return "Basic " + Base64.getEncoder().encodeToString(
                (clientId + ":" + secret).getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(String value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                java.security.MessageDigest.getInstance("SHA-256")
                        .digest(value.getBytes(StandardCharsets.US_ASCII)));
    }
}
