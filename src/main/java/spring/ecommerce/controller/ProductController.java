package spring.ecommerce.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.exception.ProductNotFoundException;
import spring.ecommerce.model.Image;
import spring.ecommerce.model.Product;
import spring.ecommerce.service.ProductService;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@AllArgsConstructor
/**
 * Controller for managing product-related operations.
 */
public class ProductController {

	private final ProductService productService;
	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

	
	/**
     * Adds a new product to the system.
     *
     * @param product The product details sent in the request.
     * @param files   An array of image files associated with the product.
     * @return A {@link ResponseEntity} containing the created product or an error response.
     */
	@PostMapping(value = "/product", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<?> addNewProduct(
			@RequestPart("product") @Valid Product product,
			@RequestPart(value = "imageFile", required = false) MultipartFile[] files
	) {
        log.info("Attempting to create a new product: {}", product.getProductName());
        try {
        	Set<Image> images = uploadImage(files);
        	product.setProductImages(images);
        	
        	Product createdProduct = productService.addNewProduct(product);
            log.info("Product created successfully, ID: {}", createdProduct.getProductId());
    		return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while creating product: {}", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@GetMapping("/products")
	public ResponseEntity<?> getAllProducts() {
        log.info("Attempting to get product list");
		try {
			return ResponseEntity.ok(productService.getAllProducts());
		} catch(Exception e) {
            log.error("Error occurred while retrieving product list", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/product/{productId}")
	public ResponseEntity<Product> getProduct(@PathVariable("productId") Integer productId) {
	    log.info("Attempting to get images for product ID: {}", productId);
	    try {
	        Product product = productService.getProductById(productId);  
	        if (product == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  
	        }
	        return ResponseEntity.ok(product);  
	    } catch (Exception e) {
	        log.error("Error occurred while retrieving product with ID: {}", productId, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); 
	    }
	}
	
	@GetMapping("/product/{productId}/images")
	public ResponseEntity<List<String>> getProductImages(@PathVariable("productId") Integer productId) {
	    log.info("Attempting to get images for product ID: {}", productId);
	    try {
	        Product product = productService.getProductById(productId);  
	        if (product == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  
	        }

	        // Convertir los byte[] de las imágenes a base64
	        List<String> imageBase64 = product.getProductImages().stream()
	                .map(image -> "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(image.getPicByte()))  // Convertir byte[] a base64
	                .collect(Collectors.toList());

	        return ResponseEntity.ok(imageBase64);  
	    } catch (Exception e) {
	        log.error("Error occurred while retrieving images for product with ID: {}", productId, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); 
	    }
	}

	
	@DeleteMapping("/product/{productId}")
	public ResponseEntity<?> deleteProduct(@PathVariable (name = "productId") Integer productId) {
	    log.info("Attempting to delete product with ID: {}", productId);
	    try {
	    	 productService.deleteById(productId);
	         return ResponseEntity.noContent().build(); 
	    } catch(ProductNotFoundException e) { 
	        log.warn("Product with ID {} not found", productId);
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found with ID: " + productId);
	    } catch(Exception e) {
	        log.error("Error occurred while deleting product with ID: {}", productId, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the product");
	    }
	}

	/**
     * Processes and converts uploaded image files into a set of {@link Image} objects.
     *
     * @param multipartFiles The array of uploaded image files.
     * @return A {@link Set} containing {@link Image} objects created from the files.
     */

	public Set<Image> uploadImage(MultipartFile[] multipartFiles) {
	    if (multipartFiles == null || multipartFiles.length == 0) {
	        return Set.of();
	    }

	    return Arrays.stream(multipartFiles)
	        .map(file -> {
	            try {
	                if (file.getSize() > MAX_IMAGE_SIZE) {
	                    log.warn("File {} exceeds the maximum allowed size", file.getOriginalFilename());
	                    return null;
	                }
	                return new Image(
	                    file.getOriginalFilename(),
	                    file.getContentType(),
	                    file.getBytes()
	                );
	            } catch (IOException e) {
	                log.error("Error processing file: {}", file.getOriginalFilename(), e);
	                return null;
	            }
	        })
	        .filter(image -> image != null)
	        .collect(Collectors.toSet());
	}


}
