package spring.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
     * Retrieves paginated order details, optionally filtered by a search key.
     *
     * @param page      The page number (0-based index).
     * @param size      The number of records per page.
     * @param searchKey The search keyword to filter orders by full name.
     * @return ResponseEntity containing a paginated response with order details or an error message.
     */
    @GetMapping("/getMyOrderDetailsPaginated")
    public ResponseEntity<?> getAllOrderDetailsOrderedByNameWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String searchKey) {
        
        log.info("Received request for paginated order details. Page: {}, Size: {}, SearchKey: '{}'", page, size, searchKey);

        try {
            PageResponseDto<OrderDetailEntity> pagedResponse = orderDetailService
                    .getOrderDetailsBySearchKeyWithPagination(page, size, searchKey);
            
            log.info("Successfully retrieved {} orders across {} pages.", 
                     pagedResponse.getTotalElements(), pagedResponse.getTotalPages());

            return ResponseEntity.ok(pagedResponse);
        } catch (Exception e) {
            log.error("Error occurred while retrieving paginated order details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving paginated order details.");
        }
    }


}
