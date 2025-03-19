package spring.ecommerce.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalesDataDTO {
    private List<String> labels; // Fechas, categorías, etc.
    private List<Double> values; // Monto de ventas, cantidad de pedidos, etc.
}
