package spring.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import spring.ecommerce.dto.PageResponseDto;
import spring.ecommerce.entity.CartEntity;
import spring.ecommerce.entity.UserEntity;
import spring.ecommerce.service.CartService;
import spring.ecommerce.service.CommonService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
    
    @Mock
    private CartService cartService;
    
    @Mock
    private CommonService commonService;
    
    @InjectMocks
    private CartController cartController;
    
    private CartEntity cartItem1;
    private CartEntity cartItem2;
    
    @BeforeEach
    void setUp() {
        cartItem1 = new CartEntity();
        cartItem1.setCartId(1);
        
        cartItem2 = new CartEntity();
        cartItem2.setCartId(2);
    }
    
    @Test
    void testAddToCart_Success() {
        when(cartService.addToCart(1)).thenReturn(cartItem1);
        
        ResponseEntity<?> response = cartController.addToCart(1);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(cartItem1, response.getBody());
    }
    
    @Test
    void testAddToCart_Failure() {
        when(cartService.addToCart(1)).thenThrow(new RuntimeException("Error adding product"));
        
        ResponseEntity<?> response = cartController.addToCart(1);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to add product to cart", response.getBody());
    }
    
    @Test
    void testGetCartDetails_Success() {
        List<CartEntity> cartItems = Arrays.asList(cartItem1, cartItem2);
        when(cartService.getCartDetails()).thenReturn(cartItems);
        
        ResponseEntity<?> response = cartController.getCartDetails();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cartItems, response.getBody());
    }
    
    @Test
    void testGetCartDetails_Failure() {
        when(cartService.getCartDetails()).thenThrow(new RuntimeException("Error retrieving cart"));
        
        ResponseEntity<?> response = cartController.getCartDetails();
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to retrieve cart details", response.getBody());
    }
    
    @Test
    void testGetCartDetailsOrderedByNameWithPagination_Success() {
        PageResponseDto<CartEntity> pageResponse = new PageResponseDto<>();
        when(cartService.getCartDetailsOrderedByNameWithPagination(0, 10, ""))
            .thenReturn(pageResponse);
        
        ResponseEntity<?> response = cartController.getCartDetailsOrderedByNameWithPagination(0, 10, "");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageResponse, response.getBody());
    }
    
    @Test
    void testGetCartDetailsOrderedByNameWithPagination_Failure() {
        when(cartService.getCartDetailsOrderedByNameWithPagination(0, 10, ""))
            .thenThrow(new RuntimeException("Pagination error"));
        
        ResponseEntity<?> response = cartController.getCartDetailsOrderedByNameWithPagination(0, 10, "");
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void testDeleteCartItem() {
        UserEntity mockUser = new UserEntity();
        mockUser.setUserName("testUser");

        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);

        doNothing().when(cartService).deleteCartItem(1);

        assertDoesNotThrow(() -> cartController.deleteCartItem(1));

        verify(cartService, times(1)).deleteCartItem(1);
    }


}