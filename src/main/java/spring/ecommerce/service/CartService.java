package spring.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import spring.ecommerce.dao.CartDao;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.entity.CartEntity;
import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.entity.UserEntity;

@Service
@AllArgsConstructor
public class CartService {

    private final ProductDao productDao;
    private final CartDao cartDao;
    private final CommonService commonService;
    

    /**
     * Adds a product to the authenticated user's shopping cart.
     * <p>
     * This method retrieves the product corresponding to the given {@code productId} 
     * and creates a new cart entry for the user. If the product does not exist, 
     * an exception is thrown.
     * </p>
     * 
     * @param productId The ID of the product to be added to the cart.
     * @return The newly created {@link CartEntity} saved in the database.
     * @throws RuntimeException If the product with the given {@code productId} is not found in the database.
     */
    @Transactional
    public CartEntity addToCart(Integer productId) {
        ProductEntity productEntity = productDao.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        UserEntity user = commonService.getAuthenticatedUser();
        CartEntity cartEntity = new CartEntity(productEntity, user);
        return cartDao.save(cartEntity);

    }

}
