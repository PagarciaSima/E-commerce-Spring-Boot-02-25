package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import spring.ecommerce.jwt.Constants;

@ExtendWith(MockitoExtension.class)
class JWTGeneratorServiceTest {

    @InjectMocks
    private JWTGeneratorService jwtGeneratorService;

    @BeforeEach
    void setUp() {
        // Mock del SecurityContext y Authentication
        SecurityContext securityContext = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetToken() {
        String token = jwtGeneratorService.getToken("testUser");

        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));

        // Extraer la parte sin "Bearer "
        String jwt = token.substring(7);

        // Verificar que el token es válido decodificándolo (usar la misma clave secreta de Constants)
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Constants.getSignedKey(Constants.SECRET_KEY))
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        assertNotNull(claims);
        assertTrue(claims.getSubject().equals("testUser"));
        assertNotNull(claims.get("authorities"));
    }
}
