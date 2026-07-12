package edgareldy.springtutorial.web.mapper;

import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.web.dto.category.CategoryRequest;
import edgareldy.springtutorial.web.dto.category.CategoryResponse;

/**
 * Manual entity/DTO conversion for Category, no MapStruct in this project.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getCategoryName());
    }

    public static Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.categoryName());
        return category;
    }
}
