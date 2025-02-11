package spring.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.model.User;
import spring.ecommerce.service.UserService;

/**
 * Controller responsible for handling user-related operations.
 * Provides an endpoint for user registration.
 */
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/v1")

public class UserController {

    private final UserService userService;
    private final PasswordEncoder bcryptPasswordEncoder;
    
    /**
     * Registers a new user in the system.
     * The password is securely hashed before storing it.
     *
     * @param user The user object containing registration details.
     * @return A {@link ResponseEntity} containing the created {@link User} 
     *         or an error response in case of failure.
     */
    @PostMapping("/registerNewUser")
    public ResponseEntity<User> createNewUser(@RequestBody @Valid User user) {
        log.info("Attempting to create a new user: {}", user.getUserName());

        try {
            user.setUserPassword(bcryptPasswordEncoder.encode(user.getUserPassword()));
            User createdUser = userService.createNewUser(user);
            log.info("User created successfully: {}", user.getUserName());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while creating user: {}", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles validation errors for user registration requests.
     *
     * @param ex The exception containing validation error details.
     * @return A {@link ResponseEntity} with error messages and a {@link HttpStatus#BAD_REQUEST} status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessages = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getAllErrors().forEach(error -> 
            errorMessages.append(error.getDefaultMessage()).append(" ")
        );
        log.warn("Validation errors: {}", errorMessages.toString());
        return new ResponseEntity<>(errorMessages.toString(), HttpStatus.BAD_REQUEST);
    }
}
