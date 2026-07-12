package edgareldy.springtutorial.web.mapper;

import edgareldy.springtutorial.domain.Product;
import edgareldy.springtutorial.web.dto.product.ProductRequest;
import edgareldy.springtutorial.web.dto.product.ProductResponse;

/**
 * Manual entity/DTO conversion for Product, no MapStruct in this project.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getUnitPrice(),
                CategoryMapper.toResponse(product.getCategory()));
    }

    public static Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setProductName(request.productName());
        product.setUnitPrice(request.unitPrice());
        return product;
    }
}
