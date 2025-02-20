package spring.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Image {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String shortName;
	private String type;
	@Lob
	@Column(length = 50000000)
	private byte [] picByte;
	
	public Image(String name, String shortName, String type, byte[] picByte) {
		super();
		this.shortName = shortName;
		this.name = name;
		this.type = type;
		this.picByte = picByte;
	}
	
	
}
