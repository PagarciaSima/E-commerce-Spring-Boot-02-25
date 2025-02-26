package spring.ecommerce.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.OrderDetailDao;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.dao.UserDao;
import spring.ecommerce.dto.OrderInputDto;
import spring.ecommerce.dto.OrderProductQuantityDto;
import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.entity.UserEntity;

/**
 * Service class responsible for handling order placement logic.
 */
@Service
@AllArgsConstructor
@Slf4j
public class OrderDetailService {

    private OrderDetailDao orderDetailDao;
    private ProductDao productDao;
    private UserDao userDao;

    private static final String ORDER_PLACED = "Placed";

    /**
     * Places an order based on the provided order input.
     * 
     * @param orderInputDto Data Transfer Object containing order details and product quantities.
     */
    public void placeOrder(OrderInputDto orderInputDto) {
        log.info("Starting order placement for user: {}", orderInputDto.getFullName());

        List<OrderProductQuantityDto> productQuantityList = orderInputDto.getOrderProductQuantityList();

        for (OrderProductQuantityDto orderProductQuantityDto : productQuantityList) {
            log.debug("Processing product with ID: {}", orderProductQuantityDto.getProductId());

            ProductEntity product = productDao.findById(orderProductQuantityDto.getProductId())
                    .orElseThrow(() -> {
                        log.error("Product with ID {} not found", orderProductQuantityDto.getProductId());
                        return new RuntimeException("Product not found");
                    });
            
            double priceToUse = product.getProductDiscountedPrice() > 0 
                    ? product.getProductDiscountedPrice() 
                    : product.getProductActualPrice();

            double orderAmount = priceToUse * orderProductQuantityDto.getQuantity();

            OrderDetailEntity orderDetailEntity = new OrderDetailEntity(
                    orderInputDto.getFullName(),
                    orderInputDto.getFullAddress(),
                    orderInputDto.getContactNumber(),
                    orderInputDto.getAlternateContactNumber(),
                    ORDER_PLACED,
                    orderAmount,
                    product,
                    getAuthenticatedUser()
            );

            orderDetailDao.save(orderDetailEntity);
            log.info("Order placed successfully for product ID: {}, Amount: {}", product.getProductId(), orderAmount);
        }

        log.info("Order placement completed for user: {}", orderInputDto.getFullName());
    }

    /**
     * Retrieves the authenticated user from the security context.
     * 
     * @return Authenticated {@link UserEntity}.
     */
    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Authenticated user retrieved: {}", username);

        return userDao.findById(username).orElseThrow(() -> {
            log.error("User with username '{}' not found", username);
            return new RuntimeException("Authenticated user not found");
        });
    }
}
