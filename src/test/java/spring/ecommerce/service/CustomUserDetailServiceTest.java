package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import spring.ecommerce.entity.RoleEntity;
import spring.ecommerce.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setUserName("testUser");
        mockUser.setUserPassword("password123");
        mockUser.setRole(Set.of(new RoleEntity("AdminRole", "Grants all permissions")));
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userService.findByUserName("testUser")).thenReturn(mockUser);

        UserDetails userDetails = customUserDetailService.loadUserByUsername("testUser");

        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_AdminRole")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userService.findByUserName("unknownUser")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailService.loadUserByUsername("unknownUser"));
    }
}
