package spring.ecommerce.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import io.jsonwebtoken.security.Keys;

public class Constants {
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String TOKEN_BEARER_PREFIX = "Bearer ";
	
	public static final String SECRET_KEY = "BALmhuXUaZK3YgRyxvQ0z8u2XyMolP6Tyqs9hl3ktNFBZPNCuRsTABmqG2ToINu47iSPF+B0I3mHTxUZhYA1kA==";
	public static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1 hour
	
	public static Key getSignedKey(String secretKey) {
		byte [] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
