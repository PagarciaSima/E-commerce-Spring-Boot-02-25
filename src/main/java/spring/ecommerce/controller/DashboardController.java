package spring.ecommerce.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dto.OrderAndProductDto;
import spring.ecommerce.dto.SalesDataDTO;
import spring.ecommerce.service.DashboardService;

/**
 * Controller for handling dashboard-related requests.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@AllArgsConstructor
@Slf4j
@Tag(name = "Dashboard charts", description = "API for managing dashboard charts")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves the total sales per month.
     */
    @Operation(
        summary = "Get sales per month",
        description = "Retrieves the orders per month data grouped by month.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Orders per month successfully retrieved",
                content = @Content(schema = @Schema(implementation = SalesDataDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/sales-per-month")
    public ResponseEntity<SalesDataDTO> getSalesPerMonth() {
        try {
            return ResponseEntity.ok(dashboardService.getOrdersPerMonth());
        } catch (Exception e) {
            log.error("Error retrieving sales data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the number of orders by status.
     */
    @Operation(
        summary = "Get orders by status",
        description = "Retrieves the count of orders categorized by status.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Orders by status successfully retrieved",
                content = @Content(schema = @Schema(implementation = SalesDataDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/orders-by-status")
    public ResponseEntity<SalesDataDTO> getOrdersByStatus() {
        try {
            return ResponseEntity.ok(dashboardService.getOrdersByStatus());
        } catch (Exception e) {
            log.error("Error retrieving orders by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the last four orders placed.
     */
    @Operation(
        summary = "Get last four orders",
        description = "Retrieves the last four orders placed in the system.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Last four orders successfully retrieved",
                content = @Content(schema = @Schema(implementation = OrderAndProductDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/last-four")
    public ResponseEntity<List<OrderAndProductDto>> getLastFourOrders() {
        try {
            return ResponseEntity.ok(dashboardService.getLastFourOrders());
        } catch (Exception e) {
            log.error("Error retrieving last four orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves sales data per month for admin users.
     */
    @Operation(
        summary = "Get sales per month (Admin)",
        description = "Retrieves sales data grouped by month for administrative purposes.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Orders per month successfully retrieved",
                content = @Content(schema = @Schema(implementation = SalesDataDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/sales-per-month-admin")
    public ResponseEntity<SalesDataDTO> getSalesPerMonthAdmin() {
        try {
            return ResponseEntity.ok(dashboardService.getSalesPerMonth());
        } catch (Exception e) {
            log.error("Error retrieving sales per month data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the top five best-selling products.
     */
    @Operation(
        summary = "Get top-selling products",
        description = "Retrieves the top five best-selling products.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Top selling products successfully retrieved",
                content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/top-selling")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingProducts() {
        try {
            List<Map<String, Object>> topProducts = dashboardService.getTop5BestSellingProducts();
            return ResponseEntity.ok(topProducts);
        } catch (Exception e) {
            log.error("Error retrieving top-selling products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
