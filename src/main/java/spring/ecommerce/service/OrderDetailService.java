package spring.ecommerce.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.OrderDetailDao;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.dto.OrderInputDto;
import spring.ecommerce.dto.OrderProductQuantityDto;
import spring.ecommerce.dto.PageResponseDto;
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
    private CommonService commonService;

    private static final String ORDER_PLACED = "Placed";
    
    /**
     * Retrieves the list of order details for the authenticated user.
     *
     * @return A list of {@link OrderDetailEntity} belonging to the authenticated user.
     */
    public List<OrderDetailEntity> getOrderDetails() {
        log.info("Fetching order details for the authenticated user");

        UserEntity userEntity = this.commonService.getAuthenticatedUser();
        log.debug("Authenticated user retrieved: {}", userEntity);

        List<OrderDetailEntity> orderDetails = this.orderDetailDao.findByUser(userEntity);
        log.info("Retrieved {} order details for user {}", orderDetails.size(), userEntity.getUserName());

        return orderDetails;
    }
    
    /**
     * Places an order based on the provided order input and returns the created order details.
     * 
     * @param orderInputDto Data Transfer Object containing order details and product quantities.
     * @return List of created OrderDetailEntity objects.
     */
    public List<OrderDetailEntity> placeOrder(OrderInputDto orderInputDto) {
        log.info("Starting order placement for user: {}", orderInputDto.getFullName());

        List<OrderProductQuantityDto> productQuantityList = orderInputDto.getOrderProductQuantityList();
        List<OrderDetailEntity> orderDetails = new ArrayList<>();

        for (OrderProductQuantityDto orderProductQuantityDto : productQuantityList) {
            log.debug("Processing product with ID: {}", orderProductQuantityDto.getProductId());

            ProductEntity product = this.productDao.findById(orderProductQuantityDto.getProductId())
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
                    this.commonService.getAuthenticatedUser()
            );

            this.orderDetailDao.save(orderDetailEntity);
            orderDetails.add(orderDetailEntity);
            log.info("Order placed successfully for product ID: {}, Amount: {}", product.getProductId(), orderAmount);
        }

        log.info("Order placement completed for user: {}", orderInputDto.getFullName());
        return orderDetails;
    }


    /**
     * Retrieves paginated order details based on search key and status filters.
     * 
     * @param page      The page number to retrieve.
     * @param size      The number of records per page.
     * @param searchKey The search keyword to filter orders by full name. If null or empty, no name filtering is applied.
     * @param status    The status of the orders to filter. If "all", no status filtering is applied.
     * @return A {@link PageResponseDto} containing the paginated list of {@link OrderDetailEntity} along with pagination details.
     */
    public PageResponseDto<OrderDetailEntity> getOrderDetailsBySearchKeyWithPagination(
            int page, int size, String searchKey, String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("orderFullName")));
        Page<OrderDetailEntity> orderDetailsPage;

        if (searchKey != null && !searchKey.isEmpty()) {
            if ("all".equalsIgnoreCase(status)) {
                // Search by full name without filtering by status
                orderDetailsPage = orderDetailDao.findByOrderFullNameContainingIgnoreCase(searchKey, pageable);
            } else {
                // Search by full name and filter by status
                orderDetailsPage = orderDetailDao.findByOrderFullNameContainingIgnoreCaseAndOrderStatus(searchKey, status, pageable);
            }
        } else {
            if ("all".equalsIgnoreCase(status)) {
                // Return all orders without filtering by status
                orderDetailsPage = orderDetailDao.findAll(pageable);
            } else {
                // Return only orders with the specified status
                orderDetailsPage = orderDetailDao.findByOrderStatus(status, pageable);
            }
        }

        return new PageResponseDto<>(orderDetailsPage.getContent(), 
                                     orderDetailsPage.getTotalPages(),
                                     orderDetailsPage.getTotalElements(), 
                                     orderDetailsPage.getSize(), 
                                     orderDetailsPage.getNumber());
    }


    
    /**
     * Retrieves a paginated and filtered list of the authenticated user's order details.
     *
     * @param page      The page number (0-based index).
     * @param size      The number of records per page.
     * @param searchKey The search keyword to filter orders by name.
     * @return PageResponseDto containing the paginated orders of the authenticated user.
     */
    public PageResponseDto<OrderDetailEntity> getMyOrderDetailsBySearchKeyWithPagination(int page, int size, String searchKey) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("orderFullName")));
        
        UserEntity userEntity = this.commonService.getAuthenticatedUser(); // Obtener el usuario autenticado

        Page<OrderDetailEntity> orderDetailsPage;
        
        if (searchKey != null && !searchKey.isEmpty()) {
            // Filtrar pedidos del usuario por nombre
            orderDetailsPage = this.orderDetailDao.findByUserAndOrderFullNameContainingIgnoreCase(userEntity, searchKey, pageable);
        } else {
            // Obtener todos los pedidos del usuario
            orderDetailsPage = this.orderDetailDao.findByUser(userEntity, pageable);
        }

        return new PageResponseDto<>(
            orderDetailsPage.getContent(), 
            orderDetailsPage.getTotalPages(), 
            orderDetailsPage.getTotalElements(), 
            orderDetailsPage.getSize(), 
            orderDetailsPage.getNumber()
        );
    }
    
    /**
     * Marks an order as delivered by updating its status.
     *
     * @param orderId the ID of the order to be marked as delivered
     * @param newStatus 
     */
    public void changeOrderStatus(Integer orderId, String newStatus) {
        log.info("Attempting to mark order {} as delivered.", orderId);
        
        Optional<OrderDetailEntity> optionalOrder = this.orderDetailDao.findById(orderId);
        
        if (optionalOrder.isPresent()) {
            OrderDetailEntity orderDetailEntity = optionalOrder.get();
            orderDetailEntity.setOrderStatus(newStatus);
            this.orderDetailDao.save(orderDetailEntity);
            log.info("Order {} successfully marked as delivered.", orderId);
        } else {
            log.warn("Order {} not found. Unable to mark as delivered.", orderId);
        }
    }

}
