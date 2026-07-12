package edgareldy.springtutorial.web.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Incoming payload to create or update a product.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record ProductRequest(
        @NotBlank String productName,
        @NotNull @Positive Double unitPrice,
        @NotNull Long categoryId) {
}
