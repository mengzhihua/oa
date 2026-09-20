package com.oa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void 客户端凭据令牌可查询并撤销() throws Exception {
        MvcResult tokenResult = mockMvc.perform(post("/api/oauth/token")
                        .param("grant_type", "client_credentials")
                        .param("client_id", "sap-client")
                        .param("client_secret", "sap-client-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refresh_token").doesNotExist())
                .andReturn();
        JsonNode token = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String accessToken = token.get("access_token").asText();
        String basic = basic("sap-client", "sap-client-secret");

        mockMvc.perform(post("/api/oauth/introspect")
                        .param("token", accessToken)
                        .header("Authorization", basic))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.client_id").value("sap-client"))
                .andExpect(jsonPath("$.sub").doesNotExist());

        mockMvc.perform(post("/api/oauth/revoke")
                        .param("token", accessToken)
                        .header("Authorization", basic))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/oauth/introspect")
                        .param("token", accessToken)
                        .header("Authorization", basic))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private String basic(String clientId, String secret) {
        return "Basic " + Base64.getEncoder().encodeToString(
                (clientId + ":" + secret).getBytes(StandardCharsets.UTF_8));
    }
}
