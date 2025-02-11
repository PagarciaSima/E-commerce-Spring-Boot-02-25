package spring.ecommerce.jwt;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.service.CustomUserDetailService;

/**
 * Utility class for validating and processing JWT tokens.
 */
@Slf4j
public class JwtValidate {

	
	 /**
     * Checks if a valid JWT token is present in the request header.
     * 
     * @param httpServletRequest  The HTTP request.
     * @param httpServletResponse The HTTP response.
     * @return {@code true} if the token exists and starts with "Bearer ", otherwise {@code false}.
     */
	public static boolean tokenExists(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String header = httpServletRequest.getHeader(Constants.HEADER_AUTHORIZATION);

		if (null == header || !header.startsWith(Constants.TOKEN_BEARER_PREFIX)) {
			log.warn("Token not found or invalid format.");
			return false;
		}

		log.info("Token found in request header.");
		return true;
	}

	/**
     * Extracts the claims from a JWT token.
     * 
     * @param httpServletRequest The HTTP request containing the JWT token.
     * @return The extracted claims.
     */
	public static Claims extractClaimsFromJwt(HttpServletRequest httpServletRequest) {
		String jwtToken = httpServletRequest.getHeader(Constants.HEADER_AUTHORIZATION)
				.replace(Constants.TOKEN_BEARER_PREFIX, "");

		log.info("Extracting claims from JWT.");

		Claims claims = Jwts.parserBuilder().setSigningKey(Constants.getSignedKey(Constants.SECRET_KEY)).build()
				.parseClaimsJws(jwtToken).getBody();

		log.info("Claims extracted successfully.");
		return claims;
	}

	/**
     * Authenticates a user based on the JWT claims.
     * 
     * @param claims                  The extracted claims from the JWT.
     * @param customUserDetailService The user details service to load user data.
     */
	public static void setAuthentication(Claims claims, CustomUserDetailService customUserDetailService) {
        String username = claims.getSubject();
        log.info("Attempting authentication for user: {}", username);

        UserDetails userDetails = customUserDetailService.loadUserByUsername(username);
        if (userDetails == null) {
        	log.warn("User not found: {}", username);
            return;
        }

        log.info("User found: {}. Creating authentication token.", username);
        UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        log.info("User {} authenticated successfully.", username);
    }
	
}
