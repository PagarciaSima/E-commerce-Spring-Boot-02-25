package spring.ecommerce.entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import spring.ecommerce.validation.ValidDiscountPrice;

@Entity
@Data
@ValidDiscountPrice
@Table(name = "product")
public class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer productId;
	
	@NotBlank(message = "Product name cannot be blank")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
	private String productName;
	
    @Size(max = 255, message = "Product description must not exceed 255 characters")
	private String productDescription;
    
    @NotNull(message = "Discounted price cannot be null")
	private double productDiscountedPrice;
    
    @NotNull(message = "Discounted price cannot be null")
    @Positive(message = "Discounted price must be greater than zero")
	private double productActualPrice;
    
    @ManyToMany (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
		name = "product_images",
		joinColumns = {
			@JoinColumn(name = "product_id")
		},
		inverseJoinColumns = {
			@JoinColumn(name = "image_id")
		}
	)
    private Set<ImageEntity> productImages;
}
