package spring.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.CartDao;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.dto.PageResponseDto;
import spring.ecommerce.entity.CartEntity;
import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.entity.UserEntity;

@Service
@AllArgsConstructor
@Slf4j
public class CartService {

	private final ProductDao productDao;
	private final CartDao cartDao;
	private final CommonService commonService;

	/**
	 * Adds a product to the authenticated user's shopping cart if it is not already present.
	 * 
	 * <p>This method checks whether the product is already in the user's cart, and if it is, the product will not be added again.
	 * If the product is not found in the user's cart, it will be added and saved.</p>
	 * 
	 * @param productId The ID of the product to be added to the cart.
	 * @return The newly created {@link CartEntity} saved in the database, or {@code null} if the product is already in the cart.
	 * @throws RuntimeException If the product with the given {@code productId} is not found in the database.
	 */
	@Transactional
	public CartEntity addToCart(Integer productId) {
		log.info("Intentando añadir el producto con ID {} al carrito", productId);

		ProductEntity productEntity = this.productDao.findById(productId).orElseThrow(() -> {
			log.error("Product not found ID {}", productId);
			return new RuntimeException("Product not found");
		});

		UserEntity user = getAuthenticatedUser();

		List<CartEntity> cartList = this.cartDao.findByUserEntity(user);
		List<CartEntity> cartFilteredList = cartList.stream()
				.filter(cart -> cart.getProductEntity().getProductId() == productId).collect(Collectors.toList());
		log.info("Usuario autenticado: {} - Añadiendo producto al carrito", user.getUserName());
		if (cartFilteredList.size() > 0)
			return null;

		CartEntity cartEntity = new CartEntity(productEntity, user);
		CartEntity savedCart = this.cartDao.save(cartEntity);

		log.info("Product ID {} successfully added to the user's cart for user {}", productId, user.getUserName());
		return savedCart;
	}

	/**
	 * Retrieves the shopping cart details for the authenticated user.
	 * 
	 * @return A list of {@link CartEntity} containing the user's cart items.
	 */
	public List<CartEntity> getCartDetails() {
		UserEntity user = getAuthenticatedUser();
		log.info("Retrieving cart details for user {}", user.getUserName());

		List<CartEntity> cartItems = this.cartDao.findByUserEntity(user);

		log.info("{} elements found in the cart for user {}", cartItems.size(), user.getUserName());
		return cartItems;
	}

	/**
	 * Retrieves the currently authenticated user.
	 * 
	 * @return The authenticated {@link UserEntity}.
	 */
	private UserEntity getAuthenticatedUser() {
		UserEntity user = this.commonService.getAuthenticatedUser();
		log.debug("Authenticated user: {}", user.getUserName());
		return user;
	}

	/**
	 * Retrieves paginated and sorted cart details for the authenticated user,
	 * optionally filtered by a search key.
	 *
	 * @param page      The page number (starting from 0).
	 * @param size      The number of items per page.
	 * @param searchKey The search keyword for filtering cart items by product name
	 *                  (optional).
	 * @return A {@link PageResponseDto} containing the paginated cart items.
	 */
	public PageResponseDto<CartEntity> getCartDetailsOrderedByNameWithPagination(int page, int size, String searchKey) {
		UserEntity user = getAuthenticatedUser();
		log.info("Recovering paginated cart for user: {}, page: {}, size: {}, search key: {}",
				user.getUserName(), page, size, searchKey);

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("productEntity.productName")));

		Page<CartEntity> cartPage;
		if (searchKey == null || searchKey.trim().isEmpty()) {
			cartPage = this.cartDao.findByUserEntity(user, pageable);
		} else {
			cartPage = this.cartDao.findByUserEntityAndProductEntityProductNameContainingIgnoreCase(user, searchKey,
					pageable);
		}

		return new PageResponseDto<>(cartPage.getContent(), // Lista de elementos (CartEntity)
				cartPage.getTotalPages(), // Total de páginas
				cartPage.getTotalElements(), // Total de elementos
				cartPage.getSize(), // Tamaño de página
				cartPage.getNumber() // Página actual
		);
	}
	
	/**
	 * Deletes a specific item from the authenticated user's shopping cart.
	 * 
	 * <p>This method checks if the cart item exists before deleting it. If the item is found,
	 * it will be deleted from the cart. If no item with the given ID is found, a {@link RuntimeException}
	 * will be thrown to indicate that the item does not exist.</p>
	 * 
	 * @param cartId The ID of the cart item to be deleted.
	 * @throws RuntimeException If the cart item with the given {@code cartId} is not found.
	 */
	public void deleteCartItem(Integer cartId) {
	    UserEntity user = getAuthenticatedUser();
	    log.info("Attempting to delete cart item with ID {} for user {}", cartId, user.getUserName());

	    // Check if the cart item exists for the authenticated user
	    CartEntity cartEntity = this.findById(cartId, user);

	    // Ensure the cart item belongs to the authenticated user
	    if (!cartEntity.getUserEntity().equals(user)) {
	        log.error("Cart item with ID {} does not belong to user {}", cartId, user.getUserName());
	        throw new RuntimeException("Cart item does not belong to the authenticated user");
	    }

	    // Proceed to delete the cart item
	    this.cartDao.deleteById(cartId);
	    log.info("Successfully deleted cart item with ID {} for user {}", cartId, user.getUserName());
	}

	/**
	 * Retrieves a cart item by its ID for the specified user.
	 * 
	 * <p>This method attempts to find a cart item by its {@code cartId} for the given authenticated user. 
	 * If the cart item exists, it will be returned. If the item is not found, a {@link RuntimeException}
	 * will be thrown to indicate that the item does not exist for the user.</p>
	 * 
	 * @param cartId The ID of the cart item to be retrieved.
	 * @param user The authenticated {@link UserEntity} associated with the cart item.
	 * @return The {@link CartEntity} if found.
	 * @throws RuntimeException If the cart item with the given {@code cartId} is not found for the user.
	 */
	public CartEntity findById(Integer cartId, UserEntity user) {
	    CartEntity cartEntity = this.cartDao.findById(cartId).orElseThrow(() -> {
	        log.error("Cart item with ID {} not found for user {}", cartId, user.getUserName());
	        return new RuntimeException("Cart item not found");
	    });
	    return cartEntity;
	}

	/**
	 * Deletes a list of cart items from the database.
	 * <p>
	 * This method removes all provided cart items in a single transaction.
	 * If the list is empty or null, the method exits without performing any operations.
	 * </p>
	 *
	 * @param cartItems the list of cart items to be deleted
	 */
	@Transactional
	public void deleteCartItems(List<CartEntity> cartItems) {
	    if (cartItems == null || cartItems.isEmpty()) {
	        log.info("No cart items to delete.");
	        return;
	    }

	    log.info("Deleting {} cart items.", cartItems.size());
	    
	    try {
	        this.cartDao.deleteAll(cartItems);
	        log.info("Successfully deleted {} cart items.", cartItems.size());
	    } catch (Exception e) {
	        log.error("Error while deleting cart items: {}", e.getMessage(), e);
	        throw e;
	    }
	}



}
