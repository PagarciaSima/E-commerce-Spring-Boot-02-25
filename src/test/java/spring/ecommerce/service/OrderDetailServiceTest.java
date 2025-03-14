package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import spring.ecommerce.dao.OrderDetailDao;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.dto.OrderInputDto;
import spring.ecommerce.dto.OrderProductQuantityDto;
import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class OrderDetailServiceTest {

    @Mock
    private OrderDetailDao orderDetailDao;

    @Mock
    private ProductDao productDao;
    
    @Mock
    private CommonService commonService;

    @InjectMocks
    private OrderDetailService orderDetailService;

    private UserEntity mockUser;
    
    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setUserName("TestUser");
    }

    @Test
    void testGetOrderDetails() {
        List<OrderDetailEntity> mockOrders = new ArrayList<>();
        mockOrders.add(new OrderDetailEntity());
        
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(orderDetailDao.findByUser(mockUser)).thenReturn(mockOrders);
        
        List<OrderDetailEntity> result = orderDetailService.getOrderDetails();
        
        assertEquals(1, result.size());
        verify(orderDetailDao).findByUser(mockUser);
    }
    
    @Test
    void testPlaceOrder() {
        OrderInputDto orderInputDto = new OrderInputDto();
        orderInputDto.setFullName("John Doe");
        orderInputDto.setFullAddress("123 Street");
        orderInputDto.setContactNumber("123456789");

        OrderProductQuantityDto orderProduct = new OrderProductQuantityDto();
        orderProduct.setProductId(1);
        orderProduct.setQuantity(2);
        orderInputDto.setOrderProductQuantityList(List.of(orderProduct));

        ProductEntity product = new ProductEntity();
        product.setProductId(1);
        product.setProductActualPrice(100.0);
        
        when(productDao.findById(1)).thenReturn(Optional.of(product));
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        
        List<OrderDetailEntity> orders = orderDetailService.placeOrder(orderInputDto);
        
        assertEquals(1, orders.size());
        verify(orderDetailDao, times(1)).save(any(OrderDetailEntity.class));
    }
    
    @Test
    void testChangeOrderStatus() {
        OrderDetailEntity order = new OrderDetailEntity();
        order.setOrderId(1);
        order.setOrderStatus("Placed");
        
        when(orderDetailDao.findById(1)).thenReturn(Optional.of(order));
        
        orderDetailService.changeOrderStatus(1, "Delivered");
        
        assertEquals("Delivered", order.getOrderStatus());
        verify(orderDetailDao).save(order);
    }
}
