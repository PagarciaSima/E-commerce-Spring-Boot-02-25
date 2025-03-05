package spring.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dto.PageResponseDto;
import spring.ecommerce.entity.CartEntity;
import spring.ecommerce.entity.UserEntity;
import spring.ecommerce.service.CartService;
import spring.ecommerce.service.CommonService;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1/cart")
public class CartController {

	private final CartService cartService;
	private final CommonService commonService;

	/**
	 * Adds a product to the authenticated user's cart.
	 *
	 * @param productId the ID of the product to add
	 * @return ResponseEntity with success or error status
	 */
	@PostMapping("/addToCart/{productId}")
	public ResponseEntity<?> addToCart(@PathVariable(name = "productId") Integer productId) {
		log.info("Request to add product with ID {} to cart", productId);

		try {
			// Add the product to the cart

			// Log success after the operation is complete
			log.info("Product with ID {} successfully added to cart", productId);

			// Return a successful response with HTTP CREATED status
			return new ResponseEntity<>(this.cartService.addToCart(productId), HttpStatus.CREATED);
		} catch (Exception e) {
			// Log the error with exception details
			log.error("Failed to add product with ID {} to cart: {}", productId, e.getMessage(), e);

			// Return an internal server error response
			return new ResponseEntity<>("Failed to add product to cart", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retrieves the shopping cart details for the authenticated user.
	 * <p>
	 * This method fetches the items currently in the authenticated user's shopping
	 * cart. If the operation is successful, it returns a list of {@link CartEntity}
	 * objects. If an error occurs, an appropriate HTTP status is returned.
	 * </p>
	 *
	 * @return {@link ResponseEntity} containing the list of cart items with HTTP
	 *         200 (OK) status, or an error message with HTTP 500 (Internal Server
	 *         Error) status if the retrieval fails.
	 */
	@GetMapping("/cartDetails")
	public ResponseEntity<?> getCartDetails() {
		log.info("Request to retrieve cart details");

		try {
			var cartDetails = this.cartService.getCartDetails();
			log.info("Cart details retrieved successfully with {} items", cartDetails.size());
			return ResponseEntity.ok(cartDetails);
		} catch (Exception e) {
			log.error("Failed to retrieve cart details: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve cart details");
		}
	}

	/**
	 * Retrieves paginated cart details for the authenticated user, optionally
	 * filtered by a search key.
	 *
	 * @param page      The page number (default is 0).
	 * @param size      The number of items per page (default is 10).
	 * @param searchKey The search keyword for filtering cart items by product name
	 *                  (default is empty).
	 * @return ResponseEntity containing a paginated list of cart items or an error
	 *         response.
	 */
	@GetMapping("/cartDetails/paginated")
	public ResponseEntity<?> getCartDetailsOrderedByNameWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String searchKey) {
		try {
			// Usamos el servicio para obtener los productos filtrados o no según el
			// searchKey
			PageResponseDto<CartEntity> pagedResponse = this.cartService.getCartDetailsOrderedByNameWithPagination(page,
					size, searchKey);
			return ResponseEntity.ok(pagedResponse);
		} catch (Exception e) {
			log.error("Error occurred while retrieving paginated product list", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Deletes a cart item by its ID for the authenticated user.
	 * 
	 * <p>This method attempts to delete a cart item by its {@code cartId}. It will first check if the 
	 * item exists and belongs to the authenticated user. If the item is found and belongs to the user, 
	 * it will be deleted. If the item is not found or does not belong to the authenticated user, 
	 * an appropriate exception will be thrown.</p>
	 * 
	 * @param cartId The ID of the cart item to be deleted.
	 * @throws RuntimeException If the cart item is not found or does not belong to the authenticated user.
	 */
	@DeleteMapping("/deleteCartItem/{cartId}")
	public void deleteCartItem(@PathVariable(name = "cartId") Integer cartId) {
	    UserEntity user = this.commonService.getAuthenticatedUser();
	    try {
	        // Call the service to delete the cart item
	        this.cartService.deleteCartItem(cartId);
	        log.info("Successfully deleted cart item with ID {} for user {}", cartId, user.getUserName());
	    } catch (RuntimeException e) {
	        // Log the error if something goes wrong
	        log.error("Failed to delete cart item with ID {} for user {}: {}", cartId, user.getUserName(), e.getMessage());
	    }
	}
	
	/**
	 * Deletes all cart items for the authenticated user.
	 * 
	 * <p>This method retrieves the authenticated user and deletes all cart items associated with them.
	 * If no items are found, the method completes without error.</p>
	 * 
	 * @throws RuntimeException If an error occurs during the deletion process.
	 */
	@DeleteMapping("/clearCart")
	public void clearCart() {
	    UserEntity user = this.commonService.getAuthenticatedUser();
	    log.info("Attempting to clear cart for user {}", user.getUserName());

	    // Obtener los items del carrito directamente desde el servicio
	    List<CartEntity> cartItems = this.cartService.getCartDetails();

	    if (cartItems.isEmpty()) {
	        log.info("No items found in cart for user {}", user.getUserName());
	        return;
	    }

	    // Eliminar todos los elementos del carrito
	    this.cartService.deleteCartItems(cartItems);
	    log.info("Successfully cleared cart for user {}", user.getUserName());
	}

}
