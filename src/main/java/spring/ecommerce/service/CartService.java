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
			log.error("Producto con ID {} no encontrado", productId);
			return new RuntimeException("Producto no encontrado");
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

		log.info("Producto con ID {} añadido exitosamente al carrito del usuario {}", productId, user.getUserName());
		return savedCart;
	}

	/**
	 * Retrieves the shopping cart details for the authenticated user.
	 * 
	 * @return A list of {@link CartEntity} containing the user's cart items.
	 */
	public List<CartEntity> getCartDetails() {
		UserEntity user = getAuthenticatedUser();
		log.info("Recuperando detalles del carrito para el usuario {}", user.getUserName());

		List<CartEntity> cartItems = this.cartDao.findByUserEntity(user);

		log.info("Se encontraron {} elementos en el carrito del usuario {}", cartItems.size(), user.getUserName());
		return cartItems;
	}

	/**
	 * Retrieves the currently authenticated user.
	 * 
	 * @return The authenticated {@link UserEntity}.
	 */
	private UserEntity getAuthenticatedUser() {
		UserEntity user = this.commonService.getAuthenticatedUser();
		log.debug("Usuario autenticado recuperado: {}", user.getUserName());
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
		log.info("Recuperando carrito paginado para usuario: {}, página: {}, tamaño: {}, búsqueda: {}",
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

}
