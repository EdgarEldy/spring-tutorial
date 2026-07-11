package edgareldy.springtutorial.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves the page size to use when a caller does not specify one. The default itself
 * is computed via a SpEL expression rather than a literal, to demonstrate SpEL evaluation
 * in a plain @Value annotation.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Component
public class PaginationDefaults {

    @Value("#{10 * 2}")
    private int defaultPageSize;

    public int pageSize(Integer requested) {
        return (requested == null || requested <= 0) ? defaultPageSize : requested;
    }
}
