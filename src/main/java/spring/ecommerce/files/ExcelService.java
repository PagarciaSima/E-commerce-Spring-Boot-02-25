package spring.ecommerce.files;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.model.Product;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ExcelService {

    /**
     * Generates an Excel file containing a list of products.
     *
     * @param products A list of {@link Product} objects to include in the Excel file.
     * @return A byte array representing the generated Excel file.
     */
    public byte[] generateProductListExcel(List<Product> products) {
        log.info("Starting Excel generation for {} products.", products.size());

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // Crea un libro de trabajo de Excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Product List");

            // Define un estilo para las celdas (puedes personalizarlo)
            CellStyle headerStyle = workbook.createCellStyle();
            generateHeader(workbook, sheet, headerStyle);

            // Carga los productos en el archivo Excel
            loadProductsInExcel(products, sheet);

            workbook.write(byteArrayOutputStream);
            workbook.close();
            log.info("Excel generation completed successfully.");
            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            log.error("An error occurred while generating the Excel file.", e);
            return null;
        }
    }
    
    /**
     * Generates the header row in the provided Excel sheet with bold font and predefined column titles.
     * The header row includes the columns: "Product ID", "Product Name", "Description", "Original Price", and "Discounted Price".
     * It also applies a specified cell style to the header cells.
     * 
     * @param workbook The {@link XSSFWorkbook} used to create the Excel file.
     * @param sheet The {@link Sheet} where the header row will be created.
     * @param headerStyle The {@link CellStyle} to be applied to the header cells, making the font bold.
     */
	private void generateHeader(XSSFWorkbook workbook, Sheet sheet, CellStyle headerStyle) {
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerStyle.setFont(headerFont);

		// Crea la fila de cabeceras
		Row headerRow = sheet.createRow(0);
		String[] headers = { "Product ID", "Product Name", "Description", "Original Price", "Discounted Price" };
		for (int i = 0; i < headers.length; i++) {
		    Cell cell = headerRow.createCell(i);
		    cell.setCellValue(headers[i]);
		    cell.setCellStyle(headerStyle); // Aplica el estilo de cabecera
		}
        log.debug("Excel headers written successfully.");

	}

    /**
     * Loads a list of products into the provided Excel sheet.
     * Each product's details (ID, name, description, actual price, and discounted price)
     * are written into a new row in the Excel sheet.
     * 
     * @param products The list of {@link Product} objects to be written to the Excel sheet.
     * @param sheet The {@link Sheet} object where the product data will be written.
     */
    private void loadProductsInExcel(List<Product> products, Sheet sheet) {
        int rowNum = 1; // Comienza a escribir desde la segunda fila (después de la cabecera)
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getProductId());
            row.createCell(1).setCellValue(product.getProductName());
            row.createCell(2).setCellValue(product.getProductDescription());
            row.createCell(3).setCellValue(product.getProductActualPrice() + " €");
            row.createCell(4).setCellValue(product.getProductDiscountedPrice() + " €");
        }
        log.debug("{} products have been written into the Excel file.", products.size());
    }
}
