package spring.ecommerce.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.constants.ConstantsEcommerce;
import spring.ecommerce.dto.PageResponseDto;
import spring.ecommerce.entity.ImageEntity;
import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.files.CsvService;
import spring.ecommerce.files.ExcelService;
import spring.ecommerce.files.PdfService;
import spring.ecommerce.service.ProductService;

/**
 * Controller for managing product-related operations.
 */

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
@Slf4j
@Tag(name = "Products", description = "API for managing products")
public class ProductController {

	private final PdfService pdfService; // Servicio para generar el PDF
	private final ProductService productService;
	private final CsvService csvService;
	private final ExcelService excelService;

	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

	/**
	 * Creates a new product and optionally associates images with it.
	 * <p>
	 * This method receives a {@link ProductEntity} object and a list of image
	 * files, validates them, and associates the images with the product. It then
	 * calls the
	 * {@link productService#addNewProduct(ProductEntity, MultipartFile[])} method
	 * to save the product in the database. If the product and images are
	 * successfully created, it returns a {@link ResponseEntity} with the created
	 * product and a {@link HttpStatus#CREATED} status. If an error occurs during
	 * the process, an error message is logged, and a {@link ResponseEntity} with an
	 * {@link HttpStatus#INTERNAL_SERVER_ERROR} status is returned.
	 * </p>
	 *
	 * @param product the {@link ProductEntity} object to be created, including
	 *                details such as name, description, price, etc.
	 * @param files   an array of {@link MultipartFile} objects representing images
	 *                to be associated with the product. This parameter is optional.
	 * @return a {@link ResponseEntity} containing the created {@link ProductEntity}
	 *         object if successful, or an error response if an exception occurs.
	 * @throws Exception if an error occurs while creating the product or uploading
	 *                   the images.
	 */
	@Operation(
	    summary = "Add a new product",
	    description = "Creates a new product with optional image uploads. Requires multipart/form-data.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
	        description = "Product details and optional image files",
	        required = true,
	        content = @Content(
	            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
	            schema = @Schema(implementation = ProductEntity.class)
	        )
	    ),
	    responses = {
	        @ApiResponse(responseCode = "201", description = "Product successfully created",
	            content = @Content(schema = @Schema(implementation = ProductEntity.class))),
	        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input"),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@PostMapping(value = "/product", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<?> addNewProduct(@RequestPart("product") @Valid ProductEntity product,
			@RequestPart(value = "imageFile", required = false) MultipartFile[] files) {
		log.info("Attempting to create a new product: {}", product.getProductName());
		try {
			Set<ImageEntity> images = this.uploadImage(files);
			product.setProductImages(images);

			ProductEntity createdProduct = this.productService.addNewProduct(product, files);
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
	 * This method receives a {@link ProductEntity} object and updates the product
	 * with the specified {@code productId}. It can also update the associated
	 * images by receiving new images and/or the IDs of existing images that need to
	 * be retained. The method calls the
	 * {@link productService#updateProduct(Integer, ProductEntity, List, List)} to
	 * perform the update operation. If the product is successfully updated, a
	 * success message is returned with a {@link HttpStatus#OK} status. If any error
	 * occurs during the process, an error message is returned with a
	 * {@link HttpStatus#BAD_REQUEST} status.
	 * </p>
	 *
	 * @param product        the {@link ProductEntity} object containing the updated
	 *                       product details.
	 * @param productId      the ID of the product to be updated.
	 * @param newImages      a list of {@link MultipartFile} objects representing
	 *                       new images to be associated with the product
	 *                       (optional).
	 * @param existingImages a list of image names or IDs of existing images to be
	 *                       retained (optional).
	 * @return a {@link ResponseEntity} containing a success message if the product
	 *         is updated successfully, or an error message if an exception occurs.
	 * @throws Exception if an error occurs while updating the product or its
	 *                   images.
	 */
	@Operation(
	    summary = "Update an existing product",
	    description = "Updates a product by its ID, allowing modifications to details and images.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
	        description = "Updated product details with optional new and existing images",
	        required = true,
	        content = @Content(
	            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
	        )
	    ),
	    responses = {
	        @ApiResponse(responseCode = "200", description = "Product updated successfully",
	            content = @Content(schema = @Schema(implementation = Map.class))),
	        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input"),
	        @ApiResponse(responseCode = "404", description = "Product not found"),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@PutMapping(value = "/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateProduct(@RequestPart("product") @Valid ProductEntity product,
			@PathVariable("productId") Integer productId,
			@RequestPart(value = "imageFile", required = false) List<MultipartFile> newImages,
			@RequestPart(value = "existingImages", required = false) List<String> existingImages

	) {

		try {
			// Llama al servicio para actualizar el producto con el productId
			this.productService.updateProduct(productId, product, newImages, existingImages);
			return ResponseEntity.ok(Collections.singletonMap("message", "Product updated successfully."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating product.");
		}
	}

	/**
	 * Retrieves a list of all products.
	 * <p>
	 * This method attempts to fetch all products from the database. If the
	 * operation is successful, it returns the list of products with an HTTP 200 OK
	 * status. If an error occurs during the process, it logs the error and returns
	 * an HTTP 500 Internal Server Error response.
	 * </p>
	 *
	 * @return a {@link ResponseEntity} containing the list of all products if
	 *         successful, or an error response with an HTTP 500 status if an error
	 *         occurs.
	 */
	@Operation(
	    summary = "Retrieve all products",
	    description = "Fetches a list of all available products.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    responses = {
	        @ApiResponse(responseCode = "200", description = "Successfully retrieved the product list",
	            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductEntity.class)))),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@GetMapping("/all")
	public ResponseEntity<?> getAllProducts() {
		log.info("Attempting to get product list");
		try {
			return ResponseEntity.ok(this.productService.getAllProducts());
		} catch (Exception e) {
			log.error("Error occurred while retrieving product list", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retrieves a paginated list of products ordered by their name.
	 * 
	 * This method fetches a list of products from the service, ordered by the
	 * product name, and returns the result as a paginated response. It takes in the
	 * page number and the size of the page as query parameters. The default page is
	 * 0, and the default page size is 10.
	 * 
	 * @param page The page number to retrieve. Defaults to 0 if not provided.
	 * @param size The number of products per page. Defaults to 10 if not provided.
	 * @param the  search key.
	 * @return A ResponseEntity containing a paginated list of products, or an
	 *         internal server error if an exception occurs during the retrieval.
	 */
	@Operation(
	    summary = "Retrieve paginated and filtered product list",
	    description = "Fetches a paginated list of products filtered by a search key and ordered by name.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    parameters = {
	        @Parameter(name = "page", description = "Page number (default: 0)", example = "0"),
	        @Parameter(name = "size", description = "Number of items per page (default: 10)", example = "10"),
	        @Parameter(name = "searchKey", description = "Search keyword to filter products", example = "laptop")
	    },
	    responses = {
	        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated product list",
	            content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@GetMapping()
	public ResponseEntity<?> getAllProductsOrderedByNameWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String searchKey) {
		try {
			PageResponseDto<ProductEntity> pagedResponse = this.productService
					.getProductsBySearchKeyWithPagination(page, size, searchKey);
			return ResponseEntity.ok(pagedResponse);
		} catch (Exception e) {
			log.error("Error occurred while retrieving paginated product list", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retrieves the details of a product based on the provided product ID and
	 * checkout type.
	 * 
	 * @param isSingleProductCheckout A boolean flag indicating whether the checkout
	 *                                is for a single product.
	 * @param productId               The ID of the product whose details are to be
	 *                                retrieved.
	 * @return A {@link ResponseEntity} containing the product details, or an error
	 *         response in case of failure.
	 */
	@Operation(
	    summary = "Retrieve product details",
	    description = "Fetches the details of a product based on its ID. Can be used for single product checkout.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    parameters = {
	        @Parameter(name = "isSingleProductCheckout", description = "Indicates if the request is for a single product checkout", example = "true"),
	        @Parameter(name = "productId", description = "ID of the product to retrieve details for", required = true, example = "123")
	    },
	    responses = {
	        @ApiResponse(responseCode = "200", description = "Successfully retrieved product details",
	            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductEntity.class)))),
	        @ApiResponse(responseCode = "400", description = "Invalid product ID provided"),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@GetMapping("/getProductDetails")
	public ResponseEntity<?> getProductDetails(@RequestParam boolean isSingleProductCheckout,
			@RequestParam Integer productId) {
		try {
			log.info("Attempting to retrieve product details for product ID: {} with single product checkout: {}",
					productId, isSingleProductCheckout);
			// Retrieve product details by product ID
			List<ProductEntity> productDetails = this.productService.getProductDetailsById(isSingleProductCheckout,
					productId);
			return ResponseEntity.ok(productDetails);
		} catch (Exception e) {
			log.error("Error while retrieving product details");
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retrieves a product by its ID.
	 * <p>
	 * This method attempts to fetch a product from the database using the provided
	 * product ID. If the product is found, it returns the product with an HTTP 200
	 * OK status. If the product is not found, it returns an HTTP 404 Not Found
	 * response.
	 * </p>
	 *
	 * @param productId the ID of the product to be retrieved
	 * @return a {@link ResponseEntity} containing the {@link ProductEntity} if
	 *         found, or an HTTP 404 Not Found status if the product is not found
	 */
	@Operation(
	    summary = "Retrieve a product by ID",
	    description = "Fetches the details of a specific product using its ID.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    parameters = {
	        @Parameter(name = "productId", description = "ID of the product to retrieve", required = true, example = "123")
	    },
	    responses = {
	        @ApiResponse(responseCode = "200", description = "Successfully retrieved the product",
	            content = @Content(schema = @Schema(implementation = ProductEntity.class))),
	        @ApiResponse(responseCode = "404", description = "Product not found"),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@GetMapping("/product/{productId}")
	public ResponseEntity<ProductEntity> getProduct(@PathVariable("productId") Integer productId) {
		log.info("Attempting to get images for product ID: {}", productId);
		ProductEntity product = this.productService.getProductById(productId);
		if (product == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(product);
	}

	/**
	 * Deletes a product by its ID.
	 * <p>
	 * This method attempts to delete a product from the database using the provided
	 * product ID. If the deletion is successful, it returns an HTTP 204 No Content
	 * status. If an error occurs during the deletion process, it logs the error and
	 * returns an HTTP 500 Internal Server Error response.
	 * </p>
	 *
	 * @param productId the ID of the product to be deleted
	 * @return a {@link ResponseEntity} with an HTTP 204 No Content status if the
	 *         product is deleted successfully, or an HTTP 500 Internal Server Error
	 *         status if an error occurs
	 */
	@Operation(
	    summary = "Delete a product by ID",
	    description = "Deletes a product using its ID. If the product does not exist, no action is taken.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    parameters = {
	        @Parameter(name = "productId", description = "ID of the product to delete", required = true, example = "123")
	    },
	    responses = {
	        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    }
	)
	@DeleteMapping("/product/{productId}")
	public ResponseEntity<?> deleteProduct(@PathVariable(name = "productId") Integer productId) {
		log.info("Attempting to delete product with ID: {}", productId);
		try {
			this.productService.deleteById(productId);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			log.error("Error occurred while deleting product with ID: {}", productId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while deleting the product");
		}
	}

	/**
	 * Generates a PDF document containing a list of all products ordered by name.
	 * 
	 * This method retrieves all products from the database, orders them by name,
	 * and generates a PDF file that includes the list of products. The PDF is
	 * generated without pagination. The file is then returned as a downloadable
	 * response with the appropriate headers for a PDF file.
	 * 
	 * @return ResponseEntity<byte[]> A response entity containing the PDF file in
	 *         byte array format along with the necessary headers for download. If
	 *         an error occurs during the PDF generation, an internal server error
	 *         response is returned.
	 */
	@Operation(
	    summary = "Generate a product list PDF",
	    description = "Generates a PDF file containing a list of all products ordered by name.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    responses = {
	        @ApiResponse(
	            responseCode = "200", 
	            description = "PDF file generated successfully",
	            content = @Content(
	                mediaType = "application/pdf",
	                schema = @Schema(type = "string", format = "binary")
	            )
	        ),
	        @ApiResponse(responseCode = "500", description = "Internal server error while generating PDF")
	    }
	)
	@GetMapping("/pdf")
	public ResponseEntity<byte[]> generateProductListPdf() {
		try {
			// Obtener todos los productos ordenados por nombre
			List<ProductEntity> products = this.productService.getAllProductsOrderedByName();

			// Llamar al servicio para generar el PDF sin paginación
			byte[] pdfBytes = this.pdfService.generateProductListPdf(products);

			// Configurar encabezados para la descarga del archivo PDF
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", "attachment; filename=" + ConstantsEcommerce.FILE_NAME + ".pdf");
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
	 * @return A {@link ResponseEntity} containing the CSV file as a byte array. The
	 *         response will include appropriate headers for file download.
	 */
	@Operation(
	    summary = "Download product list as CSV",
	    description = "Generates a CSV file containing a list of all products ordered by name.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "CSV file generated successfully",
	            content = @Content(
	                mediaType = "text/csv",
	                schema = @Schema(type = "string", format = "binary")
	            )
	        ),
	        @ApiResponse(responseCode = "500", description = "Internal server error while generating CSV")
	    }
	)
	@GetMapping("/csv")
	public ResponseEntity<byte[]> downloadProductListCsv() {
		log.info("Request received to download product list as CSV.");

		try {
			List<ProductEntity> products = this.productService.getAllProductsOrderedByName();
			log.debug("Fetched {} products from the database.", products.size());

			byte[] csvData = this.csvService.generateProductListCsv(products);

			if (csvData == null || csvData.length == 0) {
				log.warn("CSV generation failed or returned empty data.");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}

			// Set HTTP headers for file download
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment",  ConstantsEcommerce.FILE_NAME + ".csv");

			log.info("CSV file generated successfully. Sending response to client.");

			return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

		} catch (Exception e) {
			log.error("Error occurred while generating the CSV file.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Handles the request to download the product list as an Excel file. This
	 * method generates an Excel file containing product details and sends it as a
	 * downloadable response to the client.
	 * 
	 * @return A {@link ResponseEntity} containing the generated Excel file as a
	 *         byte array, or an HTTP 500 Internal Server Error if there was an
	 *         issue during file generation.
	 */
	@Operation(
	    summary = "Download product list as Excel",
	    description = "Generates an Excel (.xlsx) file containing a list of all products ordered by name.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "Excel file generated successfully",
	            content = @Content(
	                mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
	                schema = @Schema(type = "string", format = "binary")
	            )
	        ),
	        @ApiResponse(responseCode = "500", description = "Internal server error while generating Excel file")
	    }
	)
	@GetMapping("/excel")
	public ResponseEntity<byte[]> downloadProductListExcel() {
		log.info("Request received to download product list as Excel.");

		try {
			List<ProductEntity> products = this.productService.getAllProductsOrderedByName();
			log.debug("Fetched {} products from the database.", products.size());

			byte[] excelData = this.excelService.generateProductListExcel(products);

			if (excelData == null || excelData.length == 0) {
				log.warn("Excel generation failed or returned empty data.");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}

			// Set HTTP headers for file download
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", ConstantsEcommerce.FILE_NAME + ".xlsx");

			log.info("Excel file generated successfully. Sending response to client.");

			return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

		} catch (Exception e) {
			log.error("Error occurred while generating the Excel file.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Processes and converts uploaded image files into a set of {@link Image}
	 * objects.
	 *
	 * @param multipartFiles The array of uploaded image files.
	 * @return A {@link Set} containing {@link Image} objects created from the
	 *         files.
	 */

	/**
	 * Uploads and processes multiple image files, returning a set of
	 * {@link ImageEntity} objects.
	 * <p>
	 * This method receives an array of {@link MultipartFile} objects representing
	 * image files to be uploaded. It checks each file for size and processes it to
	 * create {@link ImageEntity} objects. If any file exceeds the maximum allowed
	 * size, it logs a warning and skips that file. If there is an error while
	 * processing a file, it logs an error and skips the file. The method returns a
	 * {@link Set} containing all successfully processed {@link ImageEntity}
	 * objects.
	 * </p>
	 *
	 * @param multipartFiles an array of {@link MultipartFile} objects representing
	 *                       the image files to be uploaded.
	 * @return a {@link Set} containing the successfully uploaded and processed
	 *         {@link ImageEntity} objects. If no files are uploaded or processed,
	 *         an empty set is returned.
	 */
	public Set<ImageEntity> uploadImage(MultipartFile[] multipartFiles) {
		if (multipartFiles == null || multipartFiles.length == 0) {
			return Set.of();
		}

		return Arrays.stream(multipartFiles).map(file -> {
			try {
				if (file.getSize() > MAX_IMAGE_SIZE) {
					log.warn("File {} exceeds the maximum allowed size", file.getOriginalFilename());
					return null;
				}
				return new ImageEntity(file.getOriginalFilename(), file.getOriginalFilename(), file.getContentType(),
						file.getBytes());
			} catch (IOException e) {
				log.error("Error processing file: {}", file.getOriginalFilename(), e);
				return null;
			}
		}).filter(image -> image != null).collect(Collectors.toSet());
	}
	
	/**
	 * Imports products from an Excel file and saves them in the database.
	 * This method processes the uploaded file, extracts product information, 
	 * and returns the list of imported products.
	 *
	 * @param file The {@link MultipartFile} representing the uploaded Excel file.
	 * @return A {@link ResponseEntity} containing a list of {@link ProductEntity} objects 
	 *         if the import is successful, or a 500 Internal Server Error status if an exception occurs.
	 */
	@PostMapping("/import-products")
	public ResponseEntity<List<ProductEntity>> importProducts(@RequestParam("file") MultipartFile file) {
	    try {
	        List<ProductEntity> products = excelService.importProductsFromExcel(file);
	        return ResponseEntity.ok(products);
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}

}
