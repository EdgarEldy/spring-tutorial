package edgareldy.springtutorial.web.dto.category;

/**
 * Category representation exposed to API clients. Never the Category entity itself.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record CategoryResponse(Long id, String categoryName) {
}
