package spring.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.ecommerce.dto.JwtRequestDto;
import spring.ecommerce.entity.UserEntity;
import spring.ecommerce.exception.UserNotFoundException;
import spring.ecommerce.service.JWTGeneratorService;
import spring.ecommerce.service.UserService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTGeneratorService jwtGenerator;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_SuccessfulAuthentication_ReturnsJwtToken() throws Exception {
        JwtRequestDto request = new JwtRequestDto("Admin", "password123");
        UserEntity mockUser = new UserEntity("Admin", "admin", "admin", "password123", new HashSet<>());
        String mockToken = "Bearer mocked-jwt-token";

        Authentication authentication = new UsernamePasswordAuthenticationToken("Admin", "password123");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.findByUserName("Admin")).thenReturn(mockUser);
        when(jwtGenerator.getToken("Admin")).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userName").value("Admin"))
                .andExpect(jsonPath("$.jwtToken").value(mockToken));
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        JwtRequestDto request = new JwtRequestDto("Admin", "wrongPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_UserNotFound_ReturnsNotFound() throws Exception {
        JwtRequestDto request = new JwtRequestDto("nonExistingUser", "password");

        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("nonExistingUser", "password"));
        when(userService.findByUserName("nonExistingUser"))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_MalformedRequest_ReturnsBadRequest() throws Exception {
        String malformedRequest = "{\"userName\": \"onlyUsername\"}";

        mockMvc.perform(post("/api/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedRequest))
                .andExpect(status().isBadRequest());
    }
}