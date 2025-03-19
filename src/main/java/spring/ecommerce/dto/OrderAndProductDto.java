package spring.ecommerce.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderAndProductDto {

	private Integer orderId;
	private LocalDateTime orderDate;
    private String orderStatus;
    private String productName;
	private double productActualPrice;
	private double productDiscountedPrice;

}
