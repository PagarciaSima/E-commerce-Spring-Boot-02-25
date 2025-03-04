package spring.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.entity.RoleEntity;
import spring.ecommerce.service.RoleService;

/**
 * Controller responsible for handling role-related operations. Provides an
 * endpoint for creating new roles.
 */
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:4200")

public class RoleController {

	private final RoleService roleService;

	/**
	 * Creates a new role in the system.
	 *
	 * @param role The role object containing role details.
	 * @return A {@link ResponseEntity} containing the created {@link RoleEntity} or
	 *         an error response in case of failure.
	 */
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

}
