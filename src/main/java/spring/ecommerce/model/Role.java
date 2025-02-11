package spring.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {

	@Id
	@NotBlank(message = "Role name can not be empty")
	private String roleName;
	@NotBlank(message = "Role description can not be empty")
	private String roleDescription;
	
}
