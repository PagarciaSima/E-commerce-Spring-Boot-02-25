package spring.ecommerce.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.constants.ConstantsEcommerce;
import spring.ecommerce.dao.OrderDetailDao;
import spring.ecommerce.dto.OrderAndProductDto;
import spring.ecommerce.dto.SalesDataDTO;
import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.entity.UserEntity;

@Service
@AllArgsConstructor
@Slf4j
public class DashboardService {
    
    private OrderDetailDao orderDetailRepository;
    private CommonService commonService;

    /**
     * Retrieves the number of orders per month.
     * 
     * @return SalesDataDTO containing month labels and order counts.
     */
    public SalesDataDTO getOrdersPerMonth() {
        log.info("Fetching orders per month");
        List<OrderDetailEntity> orders = orderDetailRepository.findByUser(commonService.getAuthenticatedUser());

        Map<YearMonth, Long> ordersByMonth = orders.stream()
            .collect(Collectors.groupingBy(
                order -> YearMonth.from(order.getOrderDate()),
                Collectors.counting()
            ));
        
        List<YearMonth> sortedMonths = new ArrayList<>(ordersByMonth.keySet());
        Collections.sort(sortedMonths);
        
        List<String> labels = sortedMonths.stream()
            .map(YearMonth::toString)
            .collect(Collectors.toList());
        
        List<Double> values = sortedMonths.stream()
            .map(month -> ordersByMonth.get(month).doubleValue())
            .collect(Collectors.toList());
        
        log.info("Orders per month fetched successfully");
        return new SalesDataDTO(labels, values);
    }

    /**
     * Retrieves the number of orders by status.
     * 
     * @return SalesDataDTO containing order statuses and counts.
     */
    public SalesDataDTO getOrdersByStatus() {
        log.info("Fetching orders by status");
        UserEntity authenticatedUser = commonService.getAuthenticatedUser();

        List<OrderDetailEntity> orders;
        if (authenticatedUser.getRole().stream().anyMatch(role -> ConstantsEcommerce.ADMIN_ROLE.equals(role.getRoleName()))) {
            log.info("User is admin, fetching all orders");
            orders = (List<OrderDetailEntity>) orderDetailRepository.findAll();
        } else {
            log.info("User is not admin, fetching only user-specific orders");
            orders = orderDetailRepository.findByUser(authenticatedUser);
        }

        Map<String, Long> ordersByStatus = orders.stream()
            .filter(order -> "Placed".equals(order.getOrderStatus()) || "Delivered".equals(order.getOrderStatus()))
            .collect(Collectors.groupingBy(OrderDetailEntity::getOrderStatus, Collectors.counting()));

        List<String> labels = new ArrayList<>(ordersByStatus.keySet());
        List<Double> values = labels.stream()
            .map(status -> ordersByStatus.get(status).doubleValue())
            .collect(Collectors.toList());
        
        log.info("Orders by status fetched successfully");
        return new SalesDataDTO(labels, values);
    }

    /**
     * Retrieves the last four orders.
     * 
     * @return List of OrderAndProductDto.
     */
    public List<OrderAndProductDto> getLastFourOrders() {
        log.info("Fetching last four orders");
        return orderDetailRepository.findLastFourOrders(PageRequest.of(0, 4));
    }

    /**
     * Retrieves sales data per month.
     * 
     * @return SalesDataDTO containing month labels and sales amounts.
     */
    public SalesDataDTO getSalesPerMonth() {
        log.info("Fetching sales per month");
        List<Object[]> results = orderDetailRepository.getSalesPerMonth();
        
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        
        for (Object[] row : results) {
            String month = row[0].toString();
            Double totalSales = ((Number) row[1]).doubleValue();
            
            labels.add(month);
            values.add(totalSales);
        }
        
        log.info("Sales per month fetched successfully");
        return new SalesDataDTO(labels, values);
    }

    /**
     * Retrieves the top 5 best-selling products in the last month.
     * 
     * @return List of maps containing product details and total sales.
     */
    public List<Map<String, Object>> getTop5BestSellingProducts() {
        log.info("Fetching top 5 best-selling products");
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        Pageable topFive = PageRequest.of(0, 5);

        List<Object[]> results = orderDetailRepository.findTopSellingProducts(lastMonth, topFive);
        List<Map<String, Object>> bestSellers = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", row[0]);
            productData.put("productName", row[1]);
            productData.put("totalSales", row[2]);
            bestSellers.add(productData);
        }
        
        log.info("Top 5 best-selling products fetched successfully");
        return bestSellers;
    }
}