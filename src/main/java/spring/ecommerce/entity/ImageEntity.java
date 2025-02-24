package spring.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "image")
public class ImageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String shortName;
	private String type;
	@Lob
	@Column(length = 50000000)
	private byte [] picByte;
	
	public ImageEntity(String name, String shortName, String type, byte[] picByte) {
		super();
		this.shortName = shortName;
		this.name = name;
		this.type = type;
		this.picByte = picByte;
	}
	
	
}
