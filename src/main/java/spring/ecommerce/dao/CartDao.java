package spring.ecommerce.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.entity.CartEntity;

@Repository
public interface CartDao extends CrudRepository<CartEntity, Integer>{

}
