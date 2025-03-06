package spring.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dto.OrderInputDto;
import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.service.OrderDetailService;

/**
 * REST controller for handling order-related requests.
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    /**
     * Endpoint to place an order.
     * 
     * @param orderInputDto Data Transfer Object containing order information.
     */
    @PostMapping("/placeOrder")
    public void placeOrder(@RequestBody OrderInputDto orderInputDto) {
        log.info("Received order placement request for user: {}", orderInputDto.getFullName());

        try {
            this.orderDetailService.placeOrder(orderInputDto);
            log.info("Order successfully placed for user: {}", orderInputDto.getFullName());
        } catch (Exception e) {
            log.error("Failed to place order for user: {}. Error: {}", orderInputDto.getFullName(), e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves the order details for the authenticated user.
     *
     * @return ResponseEntity containing the list of order details or an error message.
     */
    @GetMapping("/getOrderDetails")
    public ResponseEntity<?> getOrderDetails() {
        log.info("Received request to fetch order details");

        try {
            List<OrderDetailEntity> orderDetails = orderDetailService.getOrderDetails();

            if (orderDetails.isEmpty()) {
                log.info("No orders found for the authenticated user");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No orders found.");
            }

            log.info("Successfully retrieved {} orders", orderDetails.size());
            return ResponseEntity.ok(orderDetails);

        } catch (Exception e) {
            log.error("Error retrieving order details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching order details.");
        }
    }
}
