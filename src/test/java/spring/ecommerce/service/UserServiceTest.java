package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import spring.ecommerce.dao.UserDao;
import spring.ecommerce.entity.UserEntity;
import spring.ecommerce.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity("testUser", "John", "Doe", "P@ssw0rd!", null);
    }

    @Test
    void createNewUser_ShouldSaveAndReturnUser() {
        when(userDao.save(user)).thenReturn(user);

        UserEntity savedUser = userService.createNewUser(user);

        assertNotNull(savedUser);
        assertEquals("testUser", savedUser.getUserName());
        verify(userDao, times(1)).save(user);
    }

    @Test
    void findByUserName_ShouldReturnUser_WhenUserExists() {
        when(userDao.findByUserName("testUser")).thenReturn(user);

        UserEntity foundUser = userService.findByUserName("testUser");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUserName());
        verify(userDao, times(1)).findByUserName("testUser");
    }

    @Test
    void findByUserName_ShouldThrowException_WhenUserDoesNotExist() {
        when(userDao.findByUserName("unknownUser")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.findByUserName("unknownUser"));
        verify(userDao, times(1)).findByUserName("unknownUser");
    }

    @Test
    void existsByUserName_ShouldReturnTrue_WhenUserExists() {
        when(userDao.existsByUserName("testUser")).thenReturn(true);

        boolean exists = userService.existsByUserName("testUser");

        assertTrue(exists);
        verify(userDao, times(1)).existsByUserName("testUser");
    }

    @Test
    void existsByUserName_ShouldReturnFalse_WhenUserDoesNotExist() {
        when(userDao.existsByUserName("unknownUser")).thenReturn(false);

        boolean exists = userService.existsByUserName("unknownUser");

        assertFalse(exists);
        verify(userDao, times(1)).existsByUserName("unknownUser");
    }
}
