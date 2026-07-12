package edgareldy.springtutorial.web.dto.product;

import edgareldy.springtutorial.web.dto.category.CategoryResponse;

/**
 * Product representation exposed to API clients, with its category resolved rather than
 * just a bare categoryId.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record ProductResponse(Long id, String productName, Double unitPrice, CategoryResponse category) {
}
