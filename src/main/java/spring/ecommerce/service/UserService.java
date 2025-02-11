package spring.ecommerce.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.UserDao;
import spring.ecommerce.exception.UserNotFoundException;
import spring.ecommerce.model.User;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserDao userDao;

    /**
     * Creates a new user in the system.
     *
     * @param user the user to be saved
     * @return the saved user
     */
    public User createNewUser(User user) {
        log.info("Creating a new user with username: {}", user.getUserName());
        User savedUser = userDao.save(user);
        log.info("User created successfully: {}", savedUser.getUserName());
        return savedUser;
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return the found user
     * @throws UserNotFoundException if the user does not exist
     */
    public User findByUserName(String username) {
        log.info("Searching for user with username: {}", username);
        return Optional.ofNullable(userDao.findByUserName(username))
                .orElseThrow(() -> {
                    log.warn("User with username '{}' not found", username);
                    return new UserNotFoundException("User with username '" + username + "' not found");
                });
    }
}
