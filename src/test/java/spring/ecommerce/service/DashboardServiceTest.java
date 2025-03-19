package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import spring.ecommerce.constants.ConstantsEcommerce;
import spring.ecommerce.dao.OrderDetailDao;
import spring.ecommerce.dto.OrderAndProductDto;
import spring.ecommerce.dto.SalesDataDTO;
import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.entity.RoleEntity;
import spring.ecommerce.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private OrderDetailDao orderDetailRepository;

    @Mock
    private CommonService commonService;

    @InjectMocks
    private DashboardService dashboardService;

    private UserEntity mockUser;
    private List<OrderDetailEntity> mockOrders;

    @BeforeEach
    void setUp() {
        // Simular usuario autenticado
        mockUser = new UserEntity();
        mockUser.setUserName("testuser");
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(new RoleEntity("Admin", "Admin Role Description"));
        mockUser.setRole(roles);
        
        // Simular pedidos
        mockOrders = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            OrderDetailEntity order = new OrderDetailEntity();
            order.setOrderId((int) i);
            order.setOrderDate(LocalDateTime.now().minusMonths(i));
            order.setOrderStatus(i % 2 == 0 ? "Placed" : "Delivered");
            order.setUser(mockUser);
            mockOrders.add(order);
        }
    }

    @Test
    void testGetOrdersPerMonth() {
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(orderDetailRepository.findByUser(mockUser)).thenReturn(mockOrders);

        SalesDataDTO result = dashboardService.getOrdersPerMonth();

        assertNotNull(result);
        assertEquals(5, result.getLabels().size());
        assertEquals(5, result.getValues().size());
    }

    @Test
    void testGetOrdersByStatusAsAdmin() {
        UserEntity adminUser = new UserEntity();
        adminUser.setUserName("admin");
        RoleEntity adminRole = new RoleEntity();
        adminRole.setRoleName(ConstantsEcommerce.ADMIN_ROLE);
        adminUser.setRole(Set.of(adminRole));

        when(commonService.getAuthenticatedUser()).thenReturn(adminUser);
        when(orderDetailRepository.findAll()).thenReturn(mockOrders);

        SalesDataDTO result = dashboardService.getOrdersByStatus();

        assertNotNull(result);
        assertEquals(2, result.getLabels().size()); // Placed y Delivered
    }

    @Test
    void testGetOrdersByStatusAsUser() {
        when(commonService.getAuthenticatedUser()).thenReturn(mockUser);
        when(orderDetailRepository.findByUser(mockUser)).thenReturn(mockOrders);

        SalesDataDTO result = dashboardService.getOrdersByStatus();

        assertNotNull(result);
        assertEquals(2, result.getLabels().size()); // Placed y Delivered
    }

    @Test
    void testGetLastFourOrders() {
        List<OrderAndProductDto> mockLastOrders = Arrays.asList(new OrderAndProductDto(), new OrderAndProductDto());

        when(orderDetailRepository.findLastFourOrders(PageRequest.of(0, 4))).thenReturn(mockLastOrders);

        List<OrderAndProductDto> result = dashboardService.getLastFourOrders();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetSalesPerMonth() {
        List<Object[]> mockSalesData = Arrays.asList(
            new Object[]{"2024-01", 1500.0},
            new Object[]{"2024-02", 2000.0}
        );

        when(orderDetailRepository.getSalesPerMonth()).thenReturn(mockSalesData);

        SalesDataDTO result = dashboardService.getSalesPerMonth();

        assertNotNull(result);
        assertEquals(2, result.getLabels().size());
        assertEquals(1500.0, result.getValues().get(0));
    }

    @Test
    void testGetTop5BestSellingProducts() {
        List<Object[]> mockTopProducts = Arrays.asList(
            new Object[]{1L, "Product A", 50},
            new Object[]{2L, "Product B", 30}
        );

        when(orderDetailRepository.findTopSellingProducts(any(LocalDateTime.class), any(Pageable.class)))
            .thenReturn(mockTopProducts);

        List<Map<String, Object>> result = dashboardService.getTop5BestSellingProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product A", result.get(0).get("productName"));
    }
}
