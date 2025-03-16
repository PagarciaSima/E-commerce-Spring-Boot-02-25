package spring.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.ecommerce.entity.RoleEntity;
import spring.ecommerce.service.RoleService;

class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
    }

    @Test
    void createNewRole_ShouldReturn201_WhenRoleIsCreated() throws Exception {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ADMIN"); // Asegurar que el campo requerido no es nulo
        role.setRoleDescription("Description");

        when(roleService.createNewRole(any(RoleEntity.class))).thenReturn(role);

        mockMvc.perform(post("/api/v1/createNewRole")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role))) // Convertir a JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }


    @Test
    void createNewRole_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        RoleEntity role = new RoleEntity();
        role.setRoleName("USER");
        role.setRoleDescription("Description");

        when(roleService.createNewRole(any(RoleEntity.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/v1/createNewRole")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isInternalServerError());
    }
}
