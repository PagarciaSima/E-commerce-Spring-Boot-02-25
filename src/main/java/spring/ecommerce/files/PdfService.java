package spring.ecommerce.files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.model.Product;

@Service
@Slf4j
public class PdfService {
	
	private static final int PDF_PAGE_SIZE = 10;
	
	/**
	 * Generates a PDF file containing a product list displayed in a table format. 
	 * The PDF includes a logo in the top-left corner, a centered title, and a table with product information.
	 *
	 * @param products A list of {@link Product} objects containing the data to be displayed in the PDF.
	 * @return A byte array representing the generated PDF file.
	 * @throws IOException If an error occurs while creating or writing the PDF.
	 */
	public byte[] generateProductListPdf(List<Product> products) throws IOException {
		log.debug("Generating PDF List Products file");
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    PdfWriter writer = new PdfWriter(byteArrayOutputStream);
	    PdfDocument pdfDocument = new PdfDocument(writer);
	    Document document = new Document(pdfDocument);
	    document.setMargins(50, 50, 50, 50); // Top, right, bottom, left

	    // Añadir el logo en la esquina superior izquierda por encima del título
	    generateLogo(pdfDocument, document);

	    // Añadir un espacio después del logo antes de poner el título (4 saltos)
	    generateSpace(document, 4);

	    // Título centrado debajo del logo
	    generateTitle(document);
	    generateSpace(document, 2);

	    // Dividir la lista de productos en grupos de 8
	    int pageSize = PDF_PAGE_SIZE;
	    int totalProducts = products.size();
	    int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

	    for (int i = 0; i < totalPages; i++) {
	        int fromIndex = i * pageSize;
	        int toIndex = Math.min(fromIndex + pageSize, totalProducts);
	        List<Product> subList = products.subList(fromIndex, toIndex);

	        // Generar la tabla con los productos de la sublista
	        Table table = generateTable(subList);
	        document.add(table);

	        // Si no es la última página, agregar un salto de página
	        if (i < totalPages - 1) {
	            document.add(new AreaBreak());
	        }
	    }

	    document.close();
	    return byteArrayOutputStream.toByteArray();
	}
    
    /**
     * Generates a table containing product information with predefined column widths.
     * The table includes headers with a green background and white text, and it is filled with product data.
     *
     * @param products A list of {@link Product} objects containing the data to populate the table.
     * @return A {@link Table} object populated with the product details.
     */
	private Table generateTable(List<Product> products) {
		log.debug("Generating List Products table");
		float[] columnWidths = {2, 2, 3, 2, 2};
        Table table = new Table(columnWidths).useAllAvailableWidth();
        // Encabezado en cada página
        table.setSkipFirstHeader(false);

        DeviceRgb customGreen = new DeviceRgb(34, 193, 68);

        // Encabezados con fondo verde
        table.addHeaderCell(new Cell().add(new Paragraph("Product ID")).setBackgroundColor(customGreen).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Product Name")).setBackgroundColor(customGreen).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Description")).setBackgroundColor(customGreen).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Original Price")).setBackgroundColor(customGreen).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Discounted Price")).setBackgroundColor(customGreen).setFontColor(DeviceRgb.WHITE).setBold());

        // Rellenar la tabla con productos
        for (Product product : products) {
        	table.addCell(String.valueOf(product.getProductId()));
            table.addCell(product.getProductName());
            table.addCell(product.getProductDescription());
            table.addCell("€" + product.getProductActualPrice());
            table.addCell("€" + product.getProductDiscountedPrice());
        }
		return table;
	}

	/**
	 * Adds a centered title to the PDF document with bold styling and a font size of 18.
	 *
	 * @param document The {@link Document} object where the title will be added.
	 */
	private void generateTitle(Document document) {
		Paragraph title = new Paragraph("Product List")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
	}

	/**
	 * Adds a logo to the top left corner of the PDF document.
	 * The logo is scaled to 50x50 pixels and positioned at a fixed position
	 * near the top left of the page.
	 *
	 * @param pdfDocument The {@link PdfDocument} instance that represents the PDF document.
	 * @param document The {@link Document} instance to which the logo will be added.
	 * @throws MalformedURLException If the logo path is invalid or the file cannot be accessed.
	 */
	private void generateLogo(PdfDocument pdfDocument, Document document) throws MalformedURLException {
		String logoPath = "src/main/resources/static/logo.png";
        Image logo = new Image(ImageDataFactory.create(logoPath));
        logo.scaleAbsolute(50, 40); 
        logo.setFixedPosition(50, pdfDocument.getDefaultPageSize().getHeight() - 70); 
        document.add(logo);
	}
	
	/**
	 * Adds a specified number of blank lines (new lines) as a space to the document.
	 * The space is added by appending the specified number of newline characters
	 * to create vertical gaps in the PDF.
	 *
	 * @param document The {@link Document} instance where the space will be added.
	 * @param lines The number of new lines (blank lines) to add to the document.
	 */
	private void generateSpace(Document document, int lines) {
	    StringBuilder space = new StringBuilder();
	    for (int i = 0; i < lines; i++) {
	        space.append("\n");
	    }
	    document.add(new Paragraph(space.toString()));
	}

}
