package spring.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.entity.UserEntity;
import spring.ecommerce.service.UserService;

/**
 * Controller responsible for handling user-related operations.
 * Provides an endpoint for user registration.
 */
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "http://localhost:4200")

public class UserController {

    private final UserService userService;
    private final PasswordEncoder bcryptPasswordEncoder;
    
    /**
     * Registers a new user in the system.
     * The password is securely hashed before storing it.
     *
     * @param user The user object containing registration details.
     * @return A {@link ResponseEntity} containing the created {@link UserEntity} 
     *         or an error response in case of failure.
     */
    @PostMapping("/register")
    public ResponseEntity<UserEntity> createNewUser(@RequestBody @Valid UserEntity user) {
        log.info("Attempting to create a new user: {}", user.getUserName());

        try {
            user.setUserPassword(bcryptPasswordEncoder.encode(user.getUserPassword()));
            UserEntity createdUser = userService.createNewUser(user);
            log.info("User created successfully: {}", user.getUserName());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while creating user: {}", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
