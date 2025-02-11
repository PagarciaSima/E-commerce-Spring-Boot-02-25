package spring.ecommerce.jwt;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.service.CustomUserDetailService;

/**
 * Filter for authorizing requests based on JWT tokens.
 * This filter intercepts each request and validates the JWT token if present.
 */
@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private CustomUserDetailService customUserDetailService;
    
    
    /**
     * Processes the incoming request to check for a valid JWT token.
     * If the token is valid, it extracts the claims and sets authentication in the security context.
     * Otherwise, it clears the security context.
     *
     * @param request     The incoming HTTP request.
     * @param response    The HTTP response.
     * @param filterChain The filter chain to pass the request to the next filter.
     * @throws ServletException If an error occurs during request processing.
     * @throws IOException      If an input/output error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.info("Processing JWT authentication for request: {}", request.getRequestURI());

            if (JwtValidate.tokenExists(request, response)) {
                log.info("JWT token detected, extracting claims...");

                Claims claims = JwtValidate.extractClaimsFromJwt(request);
                log.info("Claims extracted: {}", claims);

                if (claims.get("authorities") != null) {
                    log.info("Authorities found, setting authentication.");
                    JwtValidate.setAuthentication(claims, customUserDetailService);
                } else {
                    log.warn("No authorities found in claims. Clearing security context.");
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.warn("No valid JWT token found. Clearing security context.");
                SecurityContextHolder.clearContext();
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired.", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT token has expired.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token.", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unsupported JWT token.");
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token.", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Malformed JWT token.");
        }
    }
}
