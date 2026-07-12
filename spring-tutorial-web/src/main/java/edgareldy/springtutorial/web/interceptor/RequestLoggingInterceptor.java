package edgareldy.springtutorial.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Logs every incoming request's method and URI. An MVC extension point distinct from a
 * Servlet Filter: it runs inside the DispatcherServlet's handler mapping machinery, with
 * access to the resolved handler, rather than at the raw Servlet container level.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Log log = LogFactory.getLog(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info(request.getMethod() + " " + request.getRequestURI());
        return true;
    }
}
