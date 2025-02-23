package spring.ecommerce.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.model.Product;

@Repository
public interface ProductDao extends CrudRepository<Product, Integer>{

	Page<Product> findAll(Pageable pageable);

	List<Product> findAll(Sort sort);
}
