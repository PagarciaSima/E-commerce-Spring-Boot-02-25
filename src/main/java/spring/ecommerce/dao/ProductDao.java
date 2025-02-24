package spring.ecommerce.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.entity.ProductEntity;

@Repository
public interface ProductDao extends JpaRepository<ProductEntity, Integer> {


}