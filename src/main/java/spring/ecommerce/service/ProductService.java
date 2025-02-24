package spring.ecommerce.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.ImageDao;
import spring.ecommerce.dao.ProductDao;
import spring.ecommerce.exception.ImageUploadException; // Nueva excepción personalizada
import spring.ecommerce.exception.ProductNotFoundException;
import spring.ecommerce.model.Image;
import spring.ecommerce.model.PageResponse;
import spring.ecommerce.model.Product;

@Service
@Slf4j
@AllArgsConstructor
public class ProductService {
	
	private final ProductDao productDao;
	private final ImageDao imageDao;

	/**
     * Creates a new product in the system.
     *
     * @param product the product to be saved
     * @return the saved product
     */
	public Product addNewProduct(Product product, MultipartFile[] files) {
	    log.info("Creating a new product: {}", product.getProductName());

	    if (files != null && files.length > 0) {
	        try {
	            Set<Image> images = uploadImages(files);
	            product.setProductImages(images);
	        } catch (ImageUploadException e) {
	            log.error("Error uploading images", e);
	            throw e; // Re-lanza la excepción específica
	        }
	    }

	    Product savedProduct = productDao.save(product);
	    log.info("Product created successfully: {}", savedProduct.getProductId());
	    return savedProduct;
	}

	/**
	 * Retrieves all products stored in the database.
	 *
	 * This method fetches all products from the {@code productDao} repository.
	 * If no products are found, a warning message is logged.
	 *
	 * @return A {@link List} of {@link Product} objects representing all stored products.
	 */
	public List<Product> getAllProducts() {
	    List<Product> products = (List<Product>) productDao.findAll();
	    if (products.isEmpty()) {
	        log.warn("No products found.");
	    }
	    return products;
	}
	
	public PageResponse<Product> getAllProductsOrderedByNameWithPagination(int page, int size) {
	    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("productName")));
	    Page<Product> productPage = productDao.findAll(pageable);

	    return new PageResponse<>(
	        productPage.getContent(),
	        productPage.getTotalPages(),
	        productPage.getTotalElements(),
	        productPage.getSize(),
	        productPage.getNumber()
	    );
	}

	
	public List<Product> getAllProductsOrderedByName() {
	    return productDao.findAll(Sort.by(Sort.Order.asc("productName")));
	}

	/**
	 * Deletes a product by its ID.
	 *
	 * This method checks if the product with the given {@code productId} exists in the database.
	 * If the product does not exist, it logs a warning and throws a {@link ProductNotFoundException}.
	 * Otherwise, it deletes the product and logs a confirmation message.
	 *
	 * @param productId The ID of the product to be deleted.
	 * @throws ProductNotFoundException if the product with the given ID does not exist.
	 */
	public void deleteById(Integer productId) {
	    if (!productDao.existsById(productId)) {
	        log.warn("Product with ID {} not found. Cannot delete.", productId);
	        throw new ProductNotFoundException("Product not found with ID: " + productId);  
	    }
	    productDao.deleteById(productId);
	    log.info("Product with ID {} deleted successfully.", productId);
	}

	/**
	 * Retrieves a product by its ID.
	 *
	 * This method attempts to find a product in the database using the provided {@code productId}.
	 * If the product is found, it is returned. Otherwise, a {@link ProductNotFoundException} is thrown.
	 *
	 * @param productId The ID of the product to retrieve.
	 * @return The {@link Product} associated with the given ID.
	 * @throws ProductNotFoundException if no product is found with the given ID.
	 */
	public Product getProductById(Integer productId) {
	    log.info("Attempting to retrieve product with ID: {}", productId);
	    return productDao.findById(productId)
	        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
	}

	/**
	 * Updates an existing product with the given details, including new images and image previews.
	 *
	 * This method retrieves the existing product by its ID, updates its attributes,
	 * handles the removal of preview images, adds new images if provided, and saves
	 * the updated product in the database.
	 *
	 * @param id            The ID of the product to be updated.
	 * @param product       The updated product details.
	 * @param newImages     A list of new images to be added to the product (optional).
	 * @param previewImages A list of existing image names to be removed from the product (optional).
	 * @throws ProductNotFoundException if no product is found with the given ID.
	 * @throws ImageUploadException if an error occurs while uploading new images.
	 */
	public void updateProduct(Integer id, Product product, List<MultipartFile> newImages, List<String> previewImages) {
	    log.info("Updating product with ID: {}", id);

	    Product existingProduct = this.getProductById(id);

	    // Actualizar los atributos del producto
	    existingProduct.setProductName(product.getProductName());
	    existingProduct.setProductDescription(product.getProductDescription());
	    existingProduct.setProductActualPrice(product.getProductActualPrice());
	    existingProduct.setProductDiscountedPrice(product.getProductDiscountedPrice());

	    // Obtener todas las imágenes actuales del producto
	    Set<Image> savedImages = new HashSet<>(existingProduct.getProductImages());
	    Set<Image> finalImages = new HashSet<>();


	    if (previewImages != null && !previewImages.isEmpty()) {
	        for (Image image : savedImages) {
	            if (previewImages.contains(image.getShortName())) {
	                // Si la imagen es parte de las imágenes previas, la eliminamos de la base de datos
	                log.info("Removing preview image: {}", image.getShortName());
	                imageDao.delete(image);  
	            } else {
	                // Si no está en la lista de preview, la mantenemos
	                finalImages.add(image);
	            }
	        }
	    }

	    // Agregar nuevas imágenes si existen
	    if (newImages != null && !newImages.isEmpty()) {
	        try {
	            finalImages.addAll(uploadImages(newImages.toArray(new MultipartFile[0])));
	        } catch (ImageUploadException e) {
	            log.error("Error uploading new images for product ID: {}", id, e);
	            throw e;
	        }
	    }

	    // Asignar las imágenes finales al producto
	    existingProduct.setProductImages(finalImages);

	    // Guardar el producto actualizado
	    productDao.save(existingProduct);
	    log.info("Product with ID {} updated successfully.", id);
	}

	/**
	 * Uploads images and assigns them unique names.
	 *
	 * @param files the array of image files
	 * @return a set of images
	 * @throws ImageUploadException if an error occurs while reading the file
	 */
	private Set<Image> uploadImages(MultipartFile[] files) {
	    Set<Image> images = new HashSet<>();

	    if (files != null) {
	        for (MultipartFile file : files) {
	            try {
	            	String shortName = file.getOriginalFilename();
	                String fileName = UUID.randomUUID().toString() + "_" + shortName;
	                Image image = new Image(fileName, shortName, file.getContentType(), file.getBytes());
	                images.add(image);
	            } catch (IOException e) {
	                log.error("Failed to process image file: {}", file.getOriginalFilename(), e);
	                throw new ImageUploadException("Error processing image file: " + file.getOriginalFilename(), e);
	            }
	        }
	    }
	    return images;
	}

}
