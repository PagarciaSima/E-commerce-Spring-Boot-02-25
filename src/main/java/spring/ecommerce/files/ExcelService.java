package spring.ecommerce.files;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.entity.ProductEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExcelService {

    /**
     * Generates an Excel file containing a list of products.
     *
     * @param products A list of {@link ProductEntity} objects to include in the Excel file.
     * @return A byte array representing the generated Excel file.
     */
    public byte[] generateProductListExcel(List<ProductEntity> products) {
        log.info("Starting Excel generation for {} products.", products.size());

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Product List");

            CellStyle headerStyle = workbook.createCellStyle();
            generateHeader(workbook, sheet, headerStyle);
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

    private void generateHeader(XSSFWorkbook workbook, Sheet sheet, CellStyle headerStyle) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        String[] headers = { "Product ID", "Product Name", "Description", "Original Price", "Discounted Price" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        log.debug("Excel headers written successfully.");
    }

    private void loadProductsInExcel(List<ProductEntity> products, Sheet sheet) {
        int rowNum = 1;
        for (ProductEntity product : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getProductId());
            row.createCell(1).setCellValue(product.getProductName());
            row.createCell(2).setCellValue(product.getProductDescription());
            row.createCell(3).setCellValue(product.getProductActualPrice() + " €");
            row.createCell(4).setCellValue(product.getProductDiscountedPrice() + " €");
        }
        log.debug("{} products have been written into the Excel file.", products.size());
    }

    /**
     * Imports products from an Excel file and converts them into a list of ProductEntity objects.
     *
     * @param file the Excel file containing product data
     * @return a list of ProductEntity objects extracted from the file
     * @throws IOException if an error occurs while reading the file
     */
    public List<ProductEntity> importProductsFromExcel(MultipartFile file) throws IOException {
        log.info("Starting import process for Excel file: {}", file.getOriginalFilename());
        List<ProductEntity> productList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                log.error("Excel file is empty or missing sheets.");
                return productList;
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    ProductEntity product = new ProductEntity();
                    product.setProductId((int) getNumericCellValue(row.getCell(0)));
                    product.setProductName(getStringCellValue(row.getCell(1)));
                    product.setProductDescription(getStringCellValue(row.getCell(2)));
                    product.setProductDiscountedPrice(getNumericCellValue(row.getCell(3)));
                    product.setProductActualPrice(getNumericCellValue(row.getCell(4)));

                    productList.add(product);
                } catch (Exception e) {
                    log.error("Error processing row {} in Excel file: {}", rowIndex, e.getMessage());
                }
            }
        }

        log.info("Successfully imported {} products from Excel.", productList.size());
        return productList;
    }

    /**
     * Retrieves the numeric value of a cell safely.
     * If the cell is null or not of type NUMERIC, it returns 0.
     *
     * @param cell the Excel cell to extract the numeric value from
     * @return the numeric value of the cell, or 0 if the cell is null or not numeric
     */
    private double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : 0;
    }

    /**
     * Retrieves the string value of a cell safely.
     * If the cell is null or not of type STRING, it returns an empty string.
     *
     * @param cell the Excel cell to extract the string value from
     * @return the string value of the cell, or an empty string if the cell is null or not a string
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : "";
    }


}
