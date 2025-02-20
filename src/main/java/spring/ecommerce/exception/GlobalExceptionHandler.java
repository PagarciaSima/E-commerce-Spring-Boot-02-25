package spring.ecommerce.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
     * Handles any unexpected errors that occur in the application.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with an error message and HTTP status 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } 
	
	// VALIDATION EXCEPTIONS
	
    /**
     * Handles validation exceptions that occur when a method argument fails validation.
     * This method processes the validation errors that occur in request bodies or query parameters,
     * including both field-level and global errors.
     *
     * @param ex The {@link MethodArgumentNotValidException} thrown when validation fails.
     *           It contains the details of the validation errors.
     * @return A {@link ResponseEntity} with a map of validation errors. 
     *         The map contains the field names as keys and the associated error messages as values.
     *         The response will have a status of 400 Bad Request, indicating invalid input.
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
    
    
	// AUTH EXCEPTIONS

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
     * Handles exceptions related to authentication service errors.
     * This method catches {@link InternalAuthenticationServiceException} thrown 
     * during the authentication process and returns an appropriate error message.
     * 
     * @param ex The {@link InternalAuthenticationServiceException} thrown during authentication.
     *           It contains the error message and details about the service failure.
     * @return A {@link ResponseEntity} containing an error message and HTTP status 500 (Internal Server Error),
     *         indicating that there was an issue with the authentication service.
     */
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<String> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        return new ResponseEntity<>("Authentication service error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
     * Handles expired JWT token exceptions.
     * This method catches {@link ExpiredJwtException} when a JWT token has expired and returns
     * an appropriate error message. It also clears the security context to log out the user.
     * 
     * @param ex The {@link ExpiredJwtException} thrown when the JWT token is expired.
     *           It contains details about the expired token.
     * @return A {@link ResponseEntity} with an error message and HTTP status 401 (Unauthorized),
     *         indicating that the session has expired and the user needs to log in again.
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException ex) {
        // Limpia el contexto de seguridad (desloguea al usuario)
        SecurityContextHolder.clearContext();
        
        return new ResponseEntity<>("Session expired. Please log in again.", HttpStatus.UNAUTHORIZED);
    }
    
    // NOT FOUND EXCEPTIONS
    
    /**
     * Handles RoleNotFoundException when a specified role is not found in the system.
     * This method returns a response with the exception message and HTTP status 404 (Not Found).
     * 
     * @param ex The {@link RoleNotFoundException} thrown when the role is not found.
     * @return A {@link ResponseEntity} with the exception message and HTTP status 404 (Not Found).
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<String> handleRoleNotFoundException(RoleNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles ProductNotFoundException when a specified product is not found in the system.
     * This method returns a response with the exception message and HTTP status 404 (Not Found).
     * 
     * @param ex The {@link ProductNotFoundException} thrown when the product is not found.
     * @return A {@link ResponseEntity} with the exception message and HTTP status 404 (Not Found).
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UserNotFoundException when a specified user is not found in the system.
     * This method returns a response with the exception message and HTTP status 404 (Not Found).
     * 
     * @param ex The {@link UserNotFoundException} thrown when the user is not found.
     * @return A {@link ResponseEntity} with the exception message and HTTP status 404 (Not Found).
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    
    // IMAGE EXCEPTIONS
    
    /**
     * Handles ImageUploadException when an error occurs during the image upload process.
     * This method returns a response with the exception message and HTTP status 500 (Internal Server Error).
     * 
     * @param ex The {@link ImageUploadException} thrown when an error occurs during the image upload process.
     * @return A {@link ResponseEntity} with the exception message and HTTP status 500 (Internal Server Error).
     */
    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<String> handleProductNotFoundException(ImageUploadException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
