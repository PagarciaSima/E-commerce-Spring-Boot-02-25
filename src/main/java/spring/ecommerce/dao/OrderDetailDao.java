package spring.ecommerce.dao;

import org.springframework.data.repository.CrudRepository;

import spring.ecommerce.entity.OrderDetailEntity;

public interface OrderDetailDao extends CrudRepository<OrderDetailEntity, Integer>{

}
