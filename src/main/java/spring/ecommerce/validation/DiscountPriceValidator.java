package spring.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import spring.ecommerce.entity.ProductEntity;

public class DiscountPriceValidator implements ConstraintValidator<ValidDiscountPrice, ProductEntity> {

    @Override
    public boolean isValid(ProductEntity product, ConstraintValidatorContext context) {
        // Verificar que el precio con descuento no sea mayor que el precio actual
        if (product.getProductDiscountedPrice() > product.getProductActualPrice()) {
            // Aquí puedes agregar un mensaje personalizado o un error
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Discounted price cannot be greater than the actual price")
                   .addConstraintViolation();
            return false;
        }
        return true;
    }

}
