package spring.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.ecommerce.entity.UserEntity;
import spring.ecommerce.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void createNewUser_Success() throws Exception {
        UserEntity user = new UserEntity("testUser", "John", "Doe", "Password123!", null);
        UserEntity savedUser = new UserEntity("testUser", "John", "Doe", "hashedPassword", null);

        when(userService.existsByUserName(user.getUserName())).thenReturn(false);
        when(passwordEncoder.encode(user.getUserPassword())).thenReturn("hashedPassword");
        when(userService.createNewUser(any(UserEntity.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("testUser"));
    }

    @Test
    void createNewUser_UsernameAlreadyExists() throws Exception {
        UserEntity user = new UserEntity("testUser", "John", "Doe", "Password123!", null);
        
        when(userService.existsByUserName(user.getUserName())).thenReturn(true);

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    void createNewUser_InternalServerError() throws Exception {
        UserEntity user = new UserEntity("testUser", "John", "Doe", "Password123!", null);
        
        when(userService.existsByUserName(user.getUserName())).thenReturn(false);
        when(passwordEncoder.encode(user.getUserPassword())).thenReturn("hashedPassword");
        when(userService.createNewUser(any(UserEntity.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError());
    }
}