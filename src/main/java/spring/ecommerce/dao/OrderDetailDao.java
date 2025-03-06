package spring.ecommerce.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.entity.UserEntity;

public interface OrderDetailDao extends CrudRepository<OrderDetailEntity, Integer>{

	public List<OrderDetailEntity> findByUser(UserEntity userEntity);
}
