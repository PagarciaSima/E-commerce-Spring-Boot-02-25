package spring.ecommerce.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import jakarta.transaction.Transactional;
import spring.ecommerce.entity.ImageEntity;

public interface ImageDao extends CrudRepository<ImageEntity, Long> {

	Optional<ImageEntity> findByName(String name);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM product_images WHERE image_id = ?1", nativeQuery = true)
	void deleteFromProductImagesByImageID(Long imageId);

}
