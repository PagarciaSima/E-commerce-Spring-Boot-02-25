package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import spring.ecommerce.dao.CartDao;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.entity.CartEntity;
import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private CartDao cartDao;

    @Mock
    private CommonService commonService;

    @InjectMocks
    private CartService cartService;

    private UserEntity mockUser;
    private ProductEntity mockProduct;
    private CartEntity mockCartItem;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setUserName("testUser");

        mockProduct = new ProductEntity();
        mockProduct.setProductId(1);
        mockProduct.setProductName("Test Product");

        mockCartItem = new CartEntity(mockProduct, mockUser);
    }

    @Test
    void testAddToCart_Success() {
        when(productDao.findById(1)).thenReturn(Optional.of(mockProduct));
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(cartDao.findByUserEntity(mockUser)).thenReturn(List.of());
        when(cartDao.save(any(CartEntity.class))).thenReturn(mockCartItem);

        CartEntity result = cartService.addToCart(1);

        assertNotNull(result);
        assertEquals(mockProduct, result.getProductEntity());
        assertEquals(mockUser, result.getUserEntity());

        verify(productDao).findById(1);
        verify(cartDao).findByUserEntity(mockUser);
        verify(cartDao).save(any(CartEntity.class));
    }

    @Test
    void testAddToCart_ProductNotFound() {
        when(productDao.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cartService.addToCart(1));
        assertEquals("Product not found", exception.getMessage());

        verify(productDao).findById(1);
        verify(cartDao, never()).findByUserEntity(any());
        verify(cartDao, never()).save(any());
    }

    @Test
    void testAddToCart_AlreadyInCart() {
        when(productDao.findById(1)).thenReturn(Optional.of(mockProduct));
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(cartDao.findByUserEntity(mockUser)).thenReturn(List.of(mockCartItem));

        CartEntity result = cartService.addToCart(1);

        assertNull(result);

        verify(productDao).findById(1);
        verify(cartDao).findByUserEntity(mockUser);
        verify(cartDao, never()).save(any());
    }

    @Test
    void testGetCartDetails() {
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(cartDao.findByUserEntity(mockUser)).thenReturn(List.of(mockCartItem));

        List<CartEntity> cartDetails = cartService.getCartDetails();

        assertNotNull(cartDetails);
        assertEquals(1, cartDetails.size());
        assertEquals(mockCartItem, cartDetails.get(0));

        verify(cartDao).findByUserEntity(mockUser);
    }

    @Test
    void testDeleteCartItem_Success() {
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(cartDao.findById(1)).thenReturn(Optional.of(mockCartItem));

        cartService.deleteCartItem(1);

        verify(cartDao).deleteById(1);
    }

    @Test
    void testDeleteCartItem_NotFound() {
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(cartDao.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cartService.deleteCartItem(1));
        assertEquals("Cart item not found", exception.getMessage());

        verify(cartDao, never()).deleteById(any());
    }
}
