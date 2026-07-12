package edgareldy.springtutorial.web.controller;

import edgareldy.springtutorial.domain.Product;
import edgareldy.springtutorial.service.ProductService;
import edgareldy.springtutorial.web.dto.common.ApiResponse;
import edgareldy.springtutorial.web.dto.common.PageResponse;
import edgareldy.springtutorial.web.dto.product.ProductRequest;
import edgareldy.springtutorial.web.dto.product.ProductResponse;
import edgareldy.springtutorial.web.mapper.ProductMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Product, delegating only to ProductService. The category is
 * resolved server-side from categoryId: the client never sends a nested category object,
 * only its id, matching ProductRequest.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> findAll(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        List<ProductResponse> content = productService.findAll(categoryId, page, size).stream()
                .map(ProductMapper::toResponse)
                .toList();
        return ApiResponse.success(PageResponse.of(content, page, size, productService.count(categoryId)), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findById(@PathVariable("id") Long id) {
        return ApiResponse.success(ProductMapper.toResponse(productService.findById(id)), null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product created = productService.create(ProductMapper.toEntity(request), request.categoryId());
        return ApiResponse.success(ProductMapper.toResponse(created), "Product created");
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable("id") Long id, @Valid @RequestBody ProductRequest request) {
        Product updated = productService.update(id, ProductMapper.toEntity(request), request.categoryId());
        return ApiResponse.success(ProductMapper.toResponse(updated), "Product updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        productService.delete(id);
        return ApiResponse.success(null, "Product deleted");
    }
}
