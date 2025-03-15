package spring.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtRequestDto {

    @NotBlank(message = "Username is required")
	private String userName;
    @NotBlank(message = "Password is required")
	private String userPassword;
}
