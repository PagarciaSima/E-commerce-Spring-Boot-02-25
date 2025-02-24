package spring.ecommerce.dto;

import lombok.Data;

@Data
public class OrderProductQuantityDto {
	private Integer productId;
	private Integer quantity;
}
