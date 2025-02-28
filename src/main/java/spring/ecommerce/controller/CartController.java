package spring.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.service.CartService;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    /**
     * Adds a product to the authenticated user's cart.
     *
     * @param productId the ID of the product to add
     * @return ResponseEntity with success or error status
     */
    @GetMapping("/addToCart/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable(name = "productId") Integer productId) {
        log.info("Request to add product with ID {} to cart", productId);

        try {
            // Add the product to the cart

            // Log success after the operation is complete
            log.info("Product with ID {} successfully added to cart", productId);

            // Return a successful response with HTTP CREATED status
            return new ResponseEntity<>(cartService.addToCart(productId), HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the error with exception details
            log.error("Failed to add product with ID {} to cart: {}", productId, e.getMessage(), e);

            // Return an internal server error response
            return new ResponseEntity<>("Failed to add product to cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
