package spring.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import spring.ecommerce.entity.UserEntity;

@AllArgsConstructor
@Data
public class JwtResponseDto {

	private UserEntity user;
	private String jwtToken;
}
