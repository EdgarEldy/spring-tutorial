package edgareldy.springtutorial.web.controller;

import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.service.CategoryService;
import edgareldy.springtutorial.web.context.RequestCorrelationContext;
import edgareldy.springtutorial.web.dto.category.CategoryRequest;
import edgareldy.springtutorial.web.dto.category.CategoryResponse;
import edgareldy.springtutorial.web.dto.common.ApiResponse;
import edgareldy.springtutorial.web.dto.common.PageResponse;
import edgareldy.springtutorial.web.mapper.CategoryMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * REST endpoints for Category, delegating only to CategoryService. The injected
 * RequestCorrelationContext is a request-scoped bean resolved through a CGLIB scoped
 * proxy: this controller is a singleton created once at startup, yet each call below
 * transparently reaches the correlation id generated for the request currently being
 * handled.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private static final Log log = LogFactory.getLog(CategoryController.class);

    private final CategoryService categoryService;
    private final RequestCorrelationContext correlationContext;

    public CategoryController(CategoryService categoryService, RequestCorrelationContext correlationContext) {
        this.categoryService = categoryService;
        this.correlationContext = correlationContext;
    }

    @GetMapping
    public ApiResponse<PageResponse<CategoryResponse>> findAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.debug("Handling " + correlationContext.getCorrelationId());
        List<CategoryResponse> content =
                categoryService.findAll(page, size).stream().map(CategoryMapper::toResponse).toList();
        return ApiResponse.success(PageResponse.of(content, page, size, categoryService.count()), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> findById(@PathVariable("id") Long id) {
        return ApiResponse.success(CategoryMapper.toResponse(categoryService.findById(id)), null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        Category created = categoryService.create(CategoryMapper.toEntity(request));
        return ApiResponse.success(CategoryMapper.toResponse(created), "Category created");
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable("id") Long id, @Valid @RequestBody CategoryRequest request) {
        Category updated = categoryService.update(id, CategoryMapper.toEntity(request));
        return ApiResponse.success(CategoryMapper.toResponse(updated), "Category updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ApiResponse.success(null, "Category deleted");
    }
}
