package spring.ecommerce.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * Global exception handler for all controllers and security exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	

    /**
     * Handles validation errors in request bodies.
     *
     * @param ex the exception containing validation errors
     * @return a {@link ResponseEntity} with the error details and HTTP status 400 (Bad Request)
     */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
	    Map<String, String> errors = new HashMap<>();
	    
	    // Primero captura los errores de campo
	    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
	        errors.put(error.getField(), error.getDefaultMessage());
	    }

	    // Luego captura los errores globales (de la clase)
	    for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
	        errors.put(error.getObjectName(), error.getDefaultMessage());
	        if (error.getCodes()[0].contains("ValidDiscount")) {
		        errors.put("Validation-error", "Discount");
	        }
	    }

	    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}
    
    

    /**
     * Handles authentication failures, such as invalid credentials.
     *
     * @param ex the exception thrown during authentication
     * @return a {@link ResponseEntity} with an error message and HTTP status 401 (Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles general authentication exceptions.
     *
     * @param ex the authentication exception
     * @return a {@link ResponseEntity} with an error message and HTTP status 403 (Forbidden)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
        return new ResponseEntity<>("Authentication failed: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles any unexpected errors that occur in the application.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with an error message and HTTP status 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException ex) {
        // Limpia el contexto de seguridad (desloguea al usuario)
        SecurityContextHolder.clearContext();
        
        return new ResponseEntity<>("Session expired. Please log in again.", HttpStatus.UNAUTHORIZED);
    }
}
