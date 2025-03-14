package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import spring.ecommerce.dao.RoleDao;
import spring.ecommerce.entity.RoleEntity;
import spring.ecommerce.exception.RoleNotFoundException;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private RoleService roleService;

    private RoleEntity role;

    @BeforeEach
    void setUp() {
        role = new RoleEntity("ADMIN", "Administrator role");
    }

    @Test
    void createNewRole_ShouldSaveAndReturnRole() {
        when(roleDao.save(role)).thenReturn(role);
        
        RoleEntity savedRole = roleService.createNewRole(role);
        
        assertNotNull(savedRole);
        assertEquals("ADMIN", savedRole.getRoleName());
        verify(roleDao, times(1)).save(role);
    }

    @Test
    void findByRoleName_ShouldReturnRole_WhenRoleExists() {
        when(roleDao.findByRoleName("ADMIN")).thenReturn(role);
        
        RoleEntity foundRole = roleService.findByRoleName("ADMIN");
        
        assertNotNull(foundRole);
        assertEquals("ADMIN", foundRole.getRoleName());
        verify(roleDao, times(1)).findByRoleName("ADMIN");
    }

    @Test
    void findByRoleName_ShouldThrowException_WhenRoleDoesNotExist() {
        when(roleDao.findByRoleName("USER")).thenReturn(null);
        
        assertThrows(RoleNotFoundException.class, () -> roleService.findByRoleName("USER"));
        verify(roleDao, times(1)).findByRoleName("USER");
    }
}
