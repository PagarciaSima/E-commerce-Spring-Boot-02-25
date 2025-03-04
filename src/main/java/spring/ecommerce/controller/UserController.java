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
 * Controller responsible for handling user-related operations. Provides an
 * endpoint for user registration.
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
	 * Registers a new user in the system. The password is securely hashed before
	 * storing it. If the username already exists, the request is rejected.
	 *
	 * @param user The user object containing registration details.
	 * @return A {@link ResponseEntity} containing:
	 *         <ul>
	 *         <li>The created {@link UserEntity} with status {@code 201 CREATED} if
	 *         successful.</li>
	 *         <li>A {@code 400 BAD REQUEST} status with an error message if the
	 *         username is already taken.</li>
	 *         <li>A {@code 500 INTERNAL SERVER ERROR} status in case of an
	 *         unexpected failure.</li>
	 *         </ul>
	 */
	@PostMapping("/register")
	public ResponseEntity<?> createNewUser(@RequestBody @Valid UserEntity user) {
		log.info("Attempting to create a new user: {}", user.getUserName());
		if (this.userService.existsByUserName(user.getUserName())) {
			log.warn("User already exists with username: {}", user.getUserName());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
		}

		try {
			user.setUserPassword(bcryptPasswordEncoder.encode(user.getUserPassword()));
			UserEntity createdUser = this.userService.createNewUser(user);
			log.info("User created successfully: {}", user.getUserName());
			return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error occurred while creating user: {}", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
