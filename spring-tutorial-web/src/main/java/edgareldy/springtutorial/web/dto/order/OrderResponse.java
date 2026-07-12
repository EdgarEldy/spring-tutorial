package edgareldy.springtutorial.web.dto.order;

import edgareldy.springtutorial.web.dto.customer.CustomerResponse;
import edgareldy.springtutorial.web.dto.product.ProductResponse;

/**
 * Order representation exposed to API clients, with customer and product resolved
 * rather than just bare ids.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record OrderResponse(
        Long id, Integer quantity, Double total, CustomerResponse customer, ProductResponse product) {
}
