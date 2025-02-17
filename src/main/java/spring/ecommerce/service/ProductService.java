package spring.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.model.Product;

@Service
@Slf4j
@AllArgsConstructor
public class ProductService {
	
	private final ProductDao productDao;

	/**
     * Creates a new product in the system.
     *
     * @param product the product to be saved
     * @return the saved product
     */
	public Product addNewProduct(Product product) {
		log.info("Creating a new product: {}", product.getProductName());
        Product savedProduct = productDao.save(product);
        log.info("User created successfully: {}", savedProduct.getProductId());
        return savedProduct;
	}
	
	public List<Product> getAllProducts() {
		return (List<Product>) productDao.findAll();
	}
}
