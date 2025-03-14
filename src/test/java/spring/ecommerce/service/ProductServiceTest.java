package spring.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.exception.ProductNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductService productService;

    private ProductEntity product;

    @BeforeEach
    void setUp() {
        product = new ProductEntity();
        product.setProductId(1);
        product.setProductName("Test Product");
    }

    @Test
    void testGetAllProducts() {
        when(productDao.findAll()).thenReturn(Arrays.asList(product));

        List<ProductEntity> products = productService.getAllProducts();
        assertFalse(products.isEmpty());
        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getProductName());
    }

    @Test
    void testGetProductById() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));

        ProductEntity foundProduct = productService.getProductById(1);

        assertNotNull(foundProduct);
        assertEquals("Test Product", foundProduct.getProductName());
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productDao.findById(1)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1));
    }

    @Test
    void testDeleteById() {
        when(productDao.existsById(1)).thenReturn(true);
        doNothing().when(productDao).deleteById(1);

        assertDoesNotThrow(() -> productService.deleteById(1));
    }

    @Test
    void testDeleteByIdNotFound() {
        when(productDao.existsById(1)).thenReturn(false);
        assertThrows(ProductNotFoundException.class, () -> productService.deleteById(1));
    }

    @Test
    void testGetAllProductsOrderedByName() {
        when(productDao.findAll(Sort.by(Sort.Order.asc("productName")))).thenReturn(Arrays.asList(product));

        List<ProductEntity> products = productService.getAllProductsOrderedByName();

        assertFalse(products.isEmpty());
        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getProductName());
    }

    @Test
    void testGetProductsBySearchKeyWithPagination() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("productName")));
        Page<ProductEntity> page = new PageImpl<>(Arrays.asList(product), pageable, 1);

        when(productDao.findByProductNameContainingIgnoreCase("Test", pageable)).thenReturn(page);

        var response = productService.getProductsBySearchKeyWithPagination(0, 10, "Test");

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Test Product", response.getContent().get(0).getProductName());
    }
}
