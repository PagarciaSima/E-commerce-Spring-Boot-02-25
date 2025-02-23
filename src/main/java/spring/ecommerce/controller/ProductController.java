package spring.ecommerce.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.files.CsvService;
import spring.ecommerce.files.PdfService;
import spring.ecommerce.model.Image;
import spring.ecommerce.model.Product;
import spring.ecommerce.service.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@Slf4j
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")

/**
 * Controller for managing product-related operations.
 */
public class ProductController {

    private final PdfService pdfService; // Servicio para generar el PDF
	private final ProductService productService;
	private final CsvService csvService;
	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

	/**
	 * Creates a new product and optionally associates images with it.
	 * <p>
	 * This method receives a {@link Product} object and a list of image files, validates them, and associates the images with the product.
	 * It then calls the {@link productService#addNewProduct(Product, MultipartFile[])} method to save the product in the database.
	 * If the product and images are successfully created, it returns a {@link ResponseEntity} with the created product and a {@link HttpStatus#CREATED} status.
	 * If an error occurs during the process, an error message is logged, and a {@link ResponseEntity} with an {@link HttpStatus#INTERNAL_SERVER_ERROR} status is returned.
	 * </p>
	 *
	 * @param product the {@link Product} object to be created, including details such as name, description, price, etc.
	 * @param files an array of {@link MultipartFile} objects representing images to be associated with the product. This parameter is optional.
	 * @return a {@link ResponseEntity} containing the created {@link Product} object if successful, or an error response if an exception occurs.
	 * @throws Exception if an error occurs while creating the product or uploading the images.
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
        	
        	Product createdProduct = productService.addNewProduct(product, files);
            log.info("Product created successfully, ID: {}", createdProduct.getProductId());
    		return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while creating product: {}", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	/**
	 * Updates an existing product and its associated images.
	 * <p>
	 * This method receives a {@link Product} object and updates the product with the specified {@code productId}.
	 * It can also update the associated images by receiving new images and/or the IDs of existing images that need to be retained.
	 * The method calls the {@link productService#updateProduct(Integer, Product, List, List)} to perform the update operation.
	 * If the product is successfully updated, a success message is returned with a {@link HttpStatus#OK} status.
	 * If any error occurs during the process, an error message is returned with a {@link HttpStatus#BAD_REQUEST} status.
	 * </p>
	 *
	 * @param product the {@link Product} object containing the updated product details.
	 * @param productId the ID of the product to be updated.
	 * @param newImages a list of {@link MultipartFile} objects representing new images to be associated with the product (optional).
	 * @param existingImages a list of image names or IDs of existing images to be retained (optional).
	 * @return a {@link ResponseEntity} containing a success message if the product is updated successfully, or an error message if an exception occurs.
	 * @throws Exception if an error occurs while updating the product or its images.
	 */
	@PutMapping(value = "/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateProduct(
		@RequestPart("product") @Valid Product product,
		@PathVariable("productId") Integer productId,
		@RequestPart(value = "imageFile", required = false) List<MultipartFile> newImages,
	    @RequestPart(value = "existingImages", required = false) List<String> existingImages

	    ) {

	    try {
	        // Llama al servicio para actualizar el producto con el productId
	    	productService.updateProduct(productId, product, newImages, existingImages);
	    	return ResponseEntity.ok(Collections.singletonMap("message", "Product updated successfully."));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating product.");
	    }
	}

	/**
     * Processes and converts uploaded image files into a set of {@link Image} objects.
     *
     * @param multipartFiles The array of uploaded image files.
     * @return A {@link Set} containing {@link Image} objects created from the files.
     */

	/**
	 * Uploads and processes multiple image files, returning a set of {@link Image} objects.
	 * <p>
	 * This method receives an array of {@link MultipartFile} objects representing image files to be uploaded.
	 * It checks each file for size and processes it to create {@link Image} objects. If any file exceeds the maximum allowed size,
	 * it logs a warning and skips that file. If there is an error while processing a file, it logs an error and skips the file.
	 * The method returns a {@link Set} containing all successfully processed {@link Image} objects.
	 * </p>
	 *
	 * @param multipartFiles an array of {@link MultipartFile} objects representing the image files to be uploaded.
	 * @return a {@link Set} containing the successfully uploaded and processed {@link Image} objects. If no files are uploaded or processed, an empty set is returned.
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
	
	/**
	 * Retrieves a list of all products.
	 * <p>
	 * This method attempts to fetch all products from the database. If the operation is successful,
	 * it returns the list of products with an HTTP 200 OK status. If an error occurs during the process,
	 * it logs the error and returns an HTTP 500 Internal Server Error response.
	 * </p>
	 *
	 * @return a {@link ResponseEntity} containing the list of all products if successful, or an error response with an HTTP 500 status if an error occurs.
	 */
	@GetMapping()
	public ResponseEntity<?> getAllProducts() {
        log.info("Attempting to get product list");
		try {
			return ResponseEntity.ok(productService.getAllProducts());
		} catch(Exception e) {
            log.error("Error occurred while retrieving product list", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Retrieves a product by its ID.
	 * <p>
	 * This method attempts to fetch a product from the database using the provided product ID. 
	 * If the product is found, it returns the product with an HTTP 200 OK status. If the product is not found,
	 * it returns an HTTP 404 Not Found response.
	 * </p>
	 *
	 * @param productId the ID of the product to be retrieved
	 * @return a {@link ResponseEntity} containing the {@link Product} if found, or an HTTP 404 Not Found status if the product is not found
	 */
	@GetMapping("/product/{productId}")
	public ResponseEntity<Product> getProduct(@PathVariable("productId") Integer productId) {
	    log.info("Attempting to get images for product ID: {}", productId);
        Product product = productService.getProductById(productId);  
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  
        }
        return ResponseEntity.ok(product);  
	}

	/**
	 * Deletes a product by its ID.
	 * <p>
	 * This method attempts to delete a product from the database using the provided product ID. 
	 * If the deletion is successful, it returns an HTTP 204 No Content status. If an error occurs during the deletion process,
	 * it logs the error and returns an HTTP 500 Internal Server Error response.
	 * </p>
	 *
	 * @param productId the ID of the product to be deleted
	 * @return a {@link ResponseEntity} with an HTTP 204 No Content status if the product is deleted successfully, 
	 *         or an HTTP 500 Internal Server Error status if an error occurs
	 */
	@DeleteMapping("/product/{productId}")
	public ResponseEntity<?> deleteProduct(@PathVariable (name = "productId") Integer productId) {
	    log.info("Attempting to delete product with ID: {}", productId);
	    try {
	    	 productService.deleteById(productId);
	         return ResponseEntity.noContent().build(); 
	    } catch(Exception e) {
	        log.error("Error occurred while deleting product with ID: {}", productId, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the product");
	    }
	}
	
	/**
	 * Generates a PDF document containing a list of all products ordered by name.
	 * 
	 * This method retrieves all products from the database, orders them by name, and generates
	 * a PDF file that includes the list of products. The PDF is generated without pagination.
	 * The file is then returned as a downloadable response with the appropriate headers for a PDF file.
	 * 
	 * @return ResponseEntity<byte[]> A response entity containing the PDF file in byte array format
	 *         along with the necessary headers for download. If an error occurs during the PDF generation,
	 *         an internal server error response is returned.
	 */
	@GetMapping("/pdf")
	public ResponseEntity<byte[]> generateProductListPdf() {
	    try {
	        // Obtener todos los productos ordenados por nombre
	        List<Product> products = productService.getAllProductsOrderedByName();

	        // Llamar al servicio para generar el PDF sin paginación
	        byte[] pdfBytes = pdfService.generateProductListPdf(products);

	        // Configurar encabezados para la descarga del archivo PDF
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Content-Disposition", "attachment; filename=product_list.pdf");
	        headers.add("Content-Type", "application/pdf");

	        // Devolver el PDF como respuesta
	        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
    /**
     * Endpoint to generate and download a CSV file containing all products.
     *
     * @return A {@link ResponseEntity} containing the CSV file as a byte array.
     *         The response will include appropriate headers for file download.
     */
    @GetMapping("/csv")
    public ResponseEntity<byte[]> downloadProductListCsv() {
        log.info("Request received to download product list as CSV.");

        try {
            List<Product> products = productService.getAllProductsOrderedByName();
            log.debug("Fetched {} products from the database.", products.size());

            byte[] csvData = csvService.generateProductListCsv(products);

            if (csvData == null || csvData.length == 0) {
                log.warn("CSV generation failed or returned empty data.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Set HTTP headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "product_list.csv");

            log.info("CSV file generated successfully. Sending response to client.");

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while generating the CSV file.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
