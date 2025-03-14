package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import spring.ecommerce.dao.UserDao;
import spring.ecommerce.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class CommonServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommonService commonService;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setUserName("testUser");
    }

    @Test
    void testGetAuthenticatedUser_Success() {
        when(authentication.getName()).thenReturn("testUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userDao.findById("testUser")).thenReturn(Optional.of(mockUser));

        UserEntity result = commonService.getAuthenticatedUser();

        assertNotNull(result);
        assertEquals("testUser", result.getUserName());

        verify(userDao).findById("testUser");
    }

    @Test
    void testGetAuthenticatedUser_UserNotFound() {
        when(authentication.getName()).thenReturn("unknownUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userDao.findById("unknownUser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> commonService.getAuthenticatedUser());
        assertEquals("Authenticated user not found", exception.getMessage());

        verify(userDao).findById("unknownUser");
    }
}
