package spring.ecommerce.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.jwt.Constants;

/**
 * Service responsible for generating JWT tokens for user authentication.
 */
@Service
@Slf4j
public class JWTGeneratorService {


    /**
     * Generates a JWT token for an authenticated user.
     *
     * @param username The authenticated user's username.
     * @return A JWT token in Bearer format.
     */
    public String getToken(String username) {
       log.info("Generating JWT token for user: {}", username);

        // Retrieve the roles of the authenticated user
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        log.debug("Assigned roles for the token: {}", roles);

        // Build the JWT token
        String token = Jwts.builder()
            .setId("E-commerce")
            .setSubject(username)
            .claim("authorities", roles) // Save roles as a list of Strings
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + Constants.TOKEN_EXPIRATION_TIME))
            .signWith(Constants.getSignedKey(Constants.SECRET_KEY), SignatureAlgorithm.HS512)
            .compact();

        log.info("JWT token successfully generated for user: {}", username);
        return "Bearer " + token;
    }
}
