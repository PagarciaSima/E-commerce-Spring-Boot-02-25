package spring.ecommerce.dto;

import lombok.Data;

@Data
public class DataPaymentDto {
	
	private String method;
	private String amount;
	private String currency;
	private String description;

}
