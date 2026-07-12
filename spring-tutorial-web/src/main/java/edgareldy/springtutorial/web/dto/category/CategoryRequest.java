package edgareldy.springtutorial.web.dto.category;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming payload to create or update a category.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record CategoryRequest(@NotBlank String categoryName) {
}
