package edgareldy.springtutorial.web.dto.common;

import java.util.List;

/**
 * Generic paginated content DTO used as the {@code data} payload of ApiResponse on every
 * list endpoint, instead of a plain list. Built manually from a content list, page,
 * size, and total element count, since this project has no Spring Data Page&lt;T&gt; to
 * build it from.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
