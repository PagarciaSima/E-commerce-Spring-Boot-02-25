package spring.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cart")
@Data
public class CartEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer cartId;
	
	@ManyToOne
	private ProductEntity productEntity;
	@ManyToOne
	private UserEntity userEntity;
	
	public CartEntity(ProductEntity productEntity, UserEntity userEntity) {
		super();
		this.productEntity = productEntity;
		this.userEntity = userEntity;
	}
	
	
}
