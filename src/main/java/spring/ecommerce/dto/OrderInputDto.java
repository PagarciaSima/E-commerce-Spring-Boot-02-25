package spring.ecommerce.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderInputDto {

	private String fullName;
	private String fullAddress;
	private String contactNumber;
	private String alternateContactNumber;
	private List<OrderProductQuantityDto> orderProductQuantityList;
}
