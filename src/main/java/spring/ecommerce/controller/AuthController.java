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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Authentication", description = "API for authentication")
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
    private final JWTGeneratorService jwtGenerator;
    private final UserService userService;
    
    /**
     * Authenticates a user and generates a JWT token upon successful login.
     *
     * This method handles user authentication by validating the provided 
     * username and password. If successful, it sets the authentication context 
     * and generates a JWT token for the authenticated user, which can be used 
     * for subsequent requests requiring authentication.
     *
     * @param jwtRequest The authentication request containing the username and password.
     *                   It should include the user's credentials for login.
     * @return A {@link ResponseEntity} containing a {@link JwtResponseDto} with the authenticated 
     *         user's information and a JWT token, or an error response in case of failure.
     *         - 200 OK: If the login is successful.
     *         - 401 Unauthorized: If authentication fails due to incorrect credentials.
     *         - 400 Bad Request: If the request is malformed.
     */
    @Operation(
        summary = "Authenticate user and generate JWT token",
        description = "Validates user credentials and returns a JWT token for authenticated access.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User successfully authenticated. JWT token provided.",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JwtResponseDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request. Missing or invalid request parameters."
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Invalid username or password."
            )
        }
    )
    @PostMapping("/authenticate")
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequestDto jwtRequest) {
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
