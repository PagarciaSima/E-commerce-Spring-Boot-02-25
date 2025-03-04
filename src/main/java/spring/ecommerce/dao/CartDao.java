package spring.ecommerce.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.entity.CartEntity;
import spring.ecommerce.entity.UserEntity;

@Repository
public interface CartDao extends CrudRepository<CartEntity, Integer>{

    List<CartEntity> findByUserEntity(UserEntity userEntity);

	Page<CartEntity> findByUserEntity(UserEntity user, Pageable pageable);

	Page<CartEntity> findByUserEntityAndProductEntityProductNameContainingIgnoreCase(UserEntity user, String searchKey,
			Pageable pageable);

}
