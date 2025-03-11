package spring.ecommerce.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dto.OrderInputDto;
import spring.ecommerce.dto.PageResponseDto;
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
     * Retrieves paginated order details, optionally filtered by a search key.
     *
     * @param page      The page number (0-based index).
     * @param size      The number of records per page.
     * @param searchKey The search keyword to filter orders by full name.
     * @return ResponseEntity containing a paginated response with order details or an error message.
     */
    @GetMapping("/getMyOrderDetailsPaginated")
    public ResponseEntity<?> getMyOrderDetailsOrderedByNameWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String searchKey) {
        
        log.info("Received request for paginated order details. Page: {}, Size: {}, SearchKey: '{}'", page, size, searchKey);

        try {
            PageResponseDto<OrderDetailEntity> pagedResponse = orderDetailService
                    .getMyOrderDetailsBySearchKeyWithPagination(page, size, searchKey);
            
            log.info("Successfully retrieved {} orders across {} pages.", 
                     pagedResponse.getTotalElements(), pagedResponse.getTotalPages());

            return ResponseEntity.ok(pagedResponse);
        } catch (Exception e) {
            log.error("Error occurred while retrieving paginated order details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving paginated order details.");
        }
    }
    
    /**
     * Retrieves paginated order details, optionally filtered by search key and order status.
     *
     * @param page      The page number to retrieve (default is 0).
     * @param size      The number of records per page (default is 10).
     * @param searchKey The search keyword to filter orders by full name (default is empty, meaning no filtering).
     * @param status    The status of the orders to filter. If "all", no status filtering is applied.
     * @return A {@link ResponseEntity} containing a {@link PageResponseDto} with the paginated list of 
     *         {@link OrderDetailEntity}, or an error message in case of failure.
     */
    @GetMapping("/getAllOrderDetailsPaginated/{status}")
    public ResponseEntity<?> getAllOrderDetailsOrderedByNameWithPagination(
    		@PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String searchKey
            
    ) {
        
        log.info("Received request for paginated order details. Page: {}, Size: {}, SearchKey: '{}'", page, size, searchKey);

        try {
            PageResponseDto<OrderDetailEntity> pagedResponse = orderDetailService
                    .getOrderDetailsBySearchKeyWithPagination(page, size, searchKey, status);
            
            log.info("Successfully retrieved {} orders across {} pages.", 
                     pagedResponse.getTotalElements(), pagedResponse.getTotalPages());

            return ResponseEntity.ok(pagedResponse);
        } catch (Exception e) {
            log.error("Error occurred while retrieving paginated order details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving paginated order details.");
        }
    }
    
    /**
     * Marks an order as delivered based on the given order ID.
     *
     * @param orderId the ID of the order to be marked as delivered
     * @return ResponseEntity with a success or error message
     */
    @PatchMapping("/markOrderAsDelivered/{orderId}")
    public ResponseEntity<Map<String, String>> markOrderAsDelivered(
        @PathVariable Integer orderId,
        @RequestBody Map<String, String> requestBody
    ) {
        log.info("Received request to mark order {} as delivered.", orderId);

        try {
            String newStatus = requestBody.get("status");
            this.orderDetailService.markOrderAsDelivered(orderId, newStatus);
            log.info("Order {} successfully marked as delivered.", orderId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Order marked as delivered.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error marking order {} as delivered: {}", orderId, e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to mark order as delivered.");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }



}
