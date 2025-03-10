package spring.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dto.JwtRequestDto;
import spring.ecommerce.dto.JwtResponseDto;
import spring.ecommerce.entity.UserEntity;
import spring.ecommerce.service.JWTGeneratorService;
import spring.ecommerce.service.UserService;

/**
 * Controller responsible for handling authentication requests.
 * Provides an endpoint for user authentication and JWT token generation.
 */
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:4200")

public class AuthController {
	
	private final AuthenticationManager authenticationManager;
    private final JWTGeneratorService jwtGenerator;
    private final UserService userService;
    
    /**
     * Authenticates a user and generates a JWT token upon successful login.
     *
     * This method handles the user authentication by validating the provided 
     * username and password, setting the authentication context, and generating
     * a JWT token for the authenticated user. The token can then be used for 
     * subsequent requests requiring authentication.
     *
     * @param jwtRequest The authentication request containing the username and password.
     *                   It should include the user's credentials for login.
     * @return A {@link ResponseEntity} containing a {@link JwtResponseDto} with the authenticated 
     *         user's information and a JWT token, or an error response in case of failure.
     *         The response will have a status of 200 OK if the login is successful.
     *         If authentication fails, the response will return a 401 Unauthorized status.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> login(@RequestBody JwtRequestDto jwtRequest) {
        log.info("Attempting login for user: {}", jwtRequest.getUserName());

        // Authenticate the user
        Authentication auth = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(jwtRequest.getUserName(), jwtRequest.getUserPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        log.info("User '{}' logged in successfully", jwtRequest.getUserName());
        log.info("User '{}' has the following roles: {}", 
                jwtRequest.getUserName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString()
        );

        UserEntity user = this.userService.findByUserName(jwtRequest.getUserName());

        // Generate JWT token
        String token = this.jwtGenerator.getToken(jwtRequest.getUserName());
        return ResponseEntity.ok(new JwtResponseDto(user, token));
    }

}
