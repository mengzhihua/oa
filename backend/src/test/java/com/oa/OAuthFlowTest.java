package com.oa;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OAuthFlowTest {
    @Autowired private MockMvc mockMvc;
    @Test public void 登录接口可签发统一令牌() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content("{\"username\":\"zhangsan\",\"password\":\"emp123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.token").isString());
    }
}
