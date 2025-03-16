package spring.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.entity.RoleEntity;
import spring.ecommerce.exception.RoleNotFoundException;
import spring.ecommerce.service.RoleService;

/**
 * Controller responsible for handling role-related operations. Provides an
 * endpoint for creating new roles.
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Roles", description = "API for managing roles")
@SecurityRequirement(name = "bearerAuth")  

public class RoleController {

	private final RoleService roleService;

	/**
	 * Creates a new role in the system.
	 *
	 * @param role The role object containing role details.
	 * @return A {@link ResponseEntity} containing the created {@link RoleEntity} or
	 *         an error response in case of failure.
	 */
	@Operation(
	    summary = "Create a new role",
	    description = "Creates a new role in the system.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
	        description = "Role details",
	        required = true,
	        content = @Content(
	            mediaType = MediaType.APPLICATION_JSON_VALUE,
	            schema = @Schema(implementation = RoleEntity.class)
	        )
	    ),
	    responses = {
	        @ApiResponse(responseCode = "201", description = "Role successfully created",
	            content = @Content(schema = @Schema(implementation = RoleEntity.class))),
	        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input"),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@PostMapping("/createNewRole")
	public ResponseEntity<RoleEntity> createNewRole(@RequestBody @Valid RoleEntity role) {
		log.info("Attempting to create a new role: {}", role.getRoleName());

		try {
			RoleEntity createdRole = this.roleService.createNewRole(role);
			log.info("Role created successfully: {}", createdRole.getRoleName());
			return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error occurred while creating role: {}", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
    /**
     * Handles RoleNotFoundException when a specified role is not found in the system.
     * This method returns a response with the exception message and HTTP status 404 (Not Found).
     * 
     * @param ex The {@link RoleNotFoundException} thrown when the role is not found.
     * @return A {@link ResponseEntity} with the exception message and HTTP status 404 (Not Found).
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<String> handleRoleNotFoundException(RoleNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

}
