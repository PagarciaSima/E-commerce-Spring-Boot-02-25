package spring.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.entity.User;
import spring.ecommerce.service.UserService;

@RestController
@Slf4j
@AllArgsConstructor
public class UserController {

private final UserService userService;
	
	@PostMapping("/registerNewUser")
    public ResponseEntity<User> createNewUser(@RequestBody @Valid User user) {
        log.info("Attempting to create a new user: {}", user.getUserName());

        try {
            User createdUser = userService.createNewUser(user);
            log.info("User created successfully: {}", user.getUserName());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while creating user: {}", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
