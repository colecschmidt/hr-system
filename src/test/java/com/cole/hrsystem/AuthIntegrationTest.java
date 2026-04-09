package com.cole.hrsystem;

import com.cole.hrsystem.model.AppUser;
import com.cole.hrsystem.repository.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRepository.save(new AppUser(
                "admin",
                passwordEncoder.encode("password123"),
                AppUser.Role.ROLE_ADMIN
        ));

        userRepository.save(new AppUser(
                "employee",
                passwordEncoder.encode("password123"),
                AppUser.Role.ROLE_EMPLOYEE
        ));
    }

    @Test
    void unauthenticatedUserCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeeCanReadButCannotCreateDepartment() throws Exception {
        String token = loginAndGetToken("employee", "password123");

        mockMvc.perform(get("/api/v1/departments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Engineering",
                "description", "Software team"
        ));

        mockMvc.perform(post("/api/v1/departments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateDepartment() throws Exception {
        String token = loginAndGetToken("admin", "password123");

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Engineering",
                "description", "Software team"
        ));

        mockMvc.perform(post("/api/v1/departments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String loginBody = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}