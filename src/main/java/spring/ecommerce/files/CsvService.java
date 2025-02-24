package spring.ecommerce.files;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.entity.ProductEntity;

@Service
@Slf4j
public class CsvService {

    /**
     * Generates a CSV file containing a list of products. The CSV includes headers
     * and product details.
     *
     * @param products A list of {@link ProductEntity} objects to include in the CSV.
     * @return A byte array representing the generated CSV file.
     */
	public byte[] generateProductListCsv(List<ProductEntity> products) {
	    log.info("Starting CSV generation for {} products.", products.size());

	    try (
	            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	            Writer writer = new OutputStreamWriter(byteArrayOutputStream, "UTF-8");
	            // Configurar CSVWriter para manejar comas y saltos de línea en las celdas, asegurando que estén entre comillas
	            CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, 
	                CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, 
	                CSVWriter.DEFAULT_LINE_END)) 
	    {	
	        // Agregar BOM (Byte Order Mark) para UTF-8
	        byteArrayOutputStream.write(0xEF);
	        byteArrayOutputStream.write(0xBB);
	        byteArrayOutputStream.write(0xBF);
	        
	        String[] header = { "Product ID", "Product Name", "Description", "Original Price", "Discounted Price" };
	        csvWriter.writeNext(header);
	        log.debug("CSV headers written successfully.");

	        // Add product data
	        loadProductsInCsv(products, csvWriter);

	        csvWriter.flush();
	        log.info("CSV generation completed successfully.");
	        return byteArrayOutputStream.toByteArray();

	    } catch (Exception e) {
	        log.error("An error occurred while generating the CSV file.", e);
	        return null;
	    }
	}

	private void loadProductsInCsv(List<ProductEntity> products, CSVWriter csvWriter) {
	    int count = 0;
	    for (ProductEntity product : products) {
	        String[] data = { 
	                String.valueOf(product.getProductId()), 
	                product.getProductName(),
	                // Las descripciones con comas o saltos de línea se manejarán correctamente por CSVWriter
	                product.getProductDescription(),
	                String.valueOf(product.getProductActualPrice()) + " €",
	                String.valueOf(product.getProductDiscountedPrice()) + " €",
	        };
	        csvWriter.writeNext(data);
	        count++;
	    }
	    log.debug("{} products have been written into the CSV.", count);
	}

}
