package spring.ecommerce.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.entity.UserEntity;

public interface OrderDetailDao extends CrudRepository<OrderDetailEntity, Integer>{

	public List<OrderDetailEntity> findByUser(UserEntity userEntity);

	public Page<OrderDetailEntity> findAll(Pageable pageable);

	public Page<OrderDetailEntity> findByOrderFullNameContainingIgnoreCase(String searchKey, Pageable pageable);

	public Page<OrderDetailEntity> findByUserAndOrderFullNameContainingIgnoreCase(UserEntity userEntity,
			String searchKey, Pageable pageable);

	public Page<OrderDetailEntity> findByUser(UserEntity userEntity, Pageable pageable);

	public Page<OrderDetailEntity> findByOrderFullNameContainingIgnoreCaseAndOrderStatus(String searchKey,
			String status, Pageable pageable);

	public Page<OrderDetailEntity> findByOrderStatus(String status, Pageable pageable);

}
