package spring.ecommerce.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import spring.ecommerce.jwt.Constants;

/**
 * Service responsible for generating JWT tokens for user authentication.
 */
@Service
public class JWTGeneratorService {

	private static final Logger logger = LoggerFactory.getLogger(JWTGeneratorService.class);

    /**
     * Generates a JWT token for an authenticated user.
     *
     * @param username The authenticated user's username.
     * @return A JWT token in Bearer format.
     */
    public String getToken(String username) {
        logger.info("Generating JWT token for user: {}", username);

        // Retrieve the roles of the authenticated user
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        logger.debug("Assigned roles for the token: {}", roles);

        // Build the JWT token
        String token = Jwts.builder()
            .setId("E-commerce")
            .setSubject(username)
            .claim("authorities", roles) // Save roles as a list of Strings
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + Constants.TOKEN_EXPIRATION_TIME))
            .signWith(Constants.getSignedKey(Constants.SECRET_KEY), SignatureAlgorithm.HS512)
            .compact();

        logger.info("JWT token successfully generated for user: {}", username);
        return "Bearer " + token;
    }

}
