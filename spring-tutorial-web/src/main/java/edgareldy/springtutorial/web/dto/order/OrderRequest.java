package edgareldy.springtutorial.web.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Incoming payload to create or update an order. total is never accepted from the
 * client: it is always computed server-side from quantity and the product's unitPrice.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record OrderRequest(
        @NotNull @Positive Integer quantity,
        @NotNull Long customerId,
        @NotNull Long productId) {
}
