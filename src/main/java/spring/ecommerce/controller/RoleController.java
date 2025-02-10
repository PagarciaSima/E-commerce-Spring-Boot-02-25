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
import spring.ecommerce.entity.Role;
import spring.ecommerce.service.RoleService;

@RestController
@Slf4j
@AllArgsConstructor
public class RoleController {

	private final RoleService roleService;
	
	@PostMapping("/createNewRole")
    public ResponseEntity<Role> createNewRole(@RequestBody @Valid Role role) {
        log.info("Attempting to create a new role: {}", role.getRoleName());

        try {
            Role createdRole = roleService.createNewRole(role);
            log.info("Role created successfully: {}", createdRole.getRoleName());
            return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while creating role: {}", e.getMessage(), e);
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
