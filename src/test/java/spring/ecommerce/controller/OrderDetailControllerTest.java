package spring.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.ecommerce.dto.OrderInputDto;
import spring.ecommerce.dto.PageResponseDto;
import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.service.OrderDetailService;

@ExtendWith(MockitoExtension.class)
class OrderDetailControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderDetailService orderDetailService;

    @InjectMocks
    private OrderDetailController orderDetailController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderDetailController).build();
    }

    @Test
    void placeOrder_ShouldReturnOrderDetails() throws Exception {
        OrderInputDto orderInputDto = new OrderInputDto();
        orderInputDto.setFullName("John Doe");
        List<OrderDetailEntity> orderDetails = Collections.singletonList(new OrderDetailEntity());

        when(orderDetailService.placeOrder(any(OrderInputDto.class))).thenReturn(orderDetails);

        mockMvc.perform(post("/api/v1/order/placeOrder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderInputDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getMyOrderDetailsOrderedByNameWithPagination_ShouldReturnPaginatedResponse() throws Exception {
        PageResponseDto<OrderDetailEntity> pageResponse = new PageResponseDto<>();

        when(orderDetailService.getMyOrderDetailsBySearchKeyWithPagination(anyInt(), anyInt(), anyString()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/order/getMyOrderDetailsPaginated")
                .param("page", "0")
                .param("size", "10")
                .param("searchKey", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void markOrderAsDelivered_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(orderDetailService).changeOrderStatus(anyInt(), anyString());

        mockMvc.perform(patch("/api/v1/order/markOrderAsDelivered/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "DELIVERED"))))
                .andExpect(status().isOk());
    }
}
