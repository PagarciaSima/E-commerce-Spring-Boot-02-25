package spring.ecommerce.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.entity.ProductEntity;

@Repository
public interface ProductDao extends CrudRepository<ProductEntity, Integer>{

	Page<ProductEntity> findAll(Pageable pageable);

	List<ProductEntity> findAll(Sort sort);
}
