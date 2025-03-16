package spring.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import spring.ecommerce.entity.ProductEntity;
import spring.ecommerce.files.CsvService;
import spring.ecommerce.files.ExcelService;
import spring.ecommerce.files.PdfService;
import spring.ecommerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private PdfService pdfService;

    @Mock
    private CsvService csvService;

    @Mock
    private ExcelService excelService;

    @InjectMocks
    private ProductController productController;

    private ProductEntity sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new ProductEntity();
        sampleProduct.setProductId(1);
        sampleProduct.setProductName("Test Product");
    }

    @Test
    void testGetAllProducts() {
        when(productService.getAllProducts()).thenReturn(Collections.singletonList(sampleProduct));

        ResponseEntity<?> response = productController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetProductById() {
        when(productService.getProductById(1)).thenReturn(sampleProduct);

        ResponseEntity<ProductEntity> response = productController.getProduct(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getProductName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productService.getProductById(1)).thenReturn(null);

        ResponseEntity<ProductEntity> response = productController.getProduct(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productService).deleteById(1);

        ResponseEntity<?> response = productController.deleteProduct(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGenerateProductListPdf() throws IOException {
        when(productService.getAllProductsOrderedByName()).thenReturn(Collections.singletonList(sampleProduct));
        when(pdfService.generateProductListPdf(anyList())).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<byte[]> response = productController.generateProductListPdf();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDownloadProductListCsv() {
        when(productService.getAllProductsOrderedByName()).thenReturn(Collections.singletonList(sampleProduct));
        when(csvService.generateProductListCsv(anyList())).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<byte[]> response = productController.downloadProductListCsv();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDownloadProductListExcel() {
        when(productService.getAllProductsOrderedByName()).thenReturn(Collections.singletonList(sampleProduct));
        when(excelService.generateProductListExcel(anyList())).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<byte[]> response = productController.downloadProductListExcel();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
