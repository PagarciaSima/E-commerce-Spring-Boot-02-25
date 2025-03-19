package spring.ecommerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import spring.ecommerce.dto.SalesDataDTO;
import spring.ecommerce.service.DashboardService;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    void getSalesPerMonth_ShouldReturnSalesData() throws Exception {
        SalesDataDTO mockSalesData = new SalesDataDTO(); // Simula la respuesta
        when(dashboardService.getOrdersPerMonth()).thenReturn(mockSalesData);

        mockMvc.perform(get("/api/v1/dashboard/sales-per-month"))
               .andExpect(status().isOk());
    }

    @Test
    void getOrdersByStatus_ShouldReturnOrderData() throws Exception {
        SalesDataDTO mockData = new SalesDataDTO();
        when(dashboardService.getOrdersByStatus()).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/dashboard/orders-by-status"))
               .andExpect(status().isOk());
    }

    @Test
    void getTopSellingProducts_ShouldReturnList() throws Exception {
        List<Map<String, Object>> mockProducts = List.of(Map.of("name", "Product A", "sales", 100));
        when(dashboardService.getTop5BestSellingProducts()).thenReturn(mockProducts);

        mockMvc.perform(get("/api/v1/dashboard/top-selling"))
               .andExpect(status().isOk());
    }

    @Test
    void getSalesPerMonth_ShouldHandleException() throws Exception {
        when(dashboardService.getOrdersPerMonth()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/dashboard/sales-per-month"))
               .andExpect(status().isInternalServerError());
    }
}
