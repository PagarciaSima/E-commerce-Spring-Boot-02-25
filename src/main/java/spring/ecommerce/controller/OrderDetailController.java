package spring.ecommerce.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dto.OrderInputDto;
import spring.ecommerce.service.OrderDetailService;

/**
 * REST controller for handling order-related requests.
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
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
}
