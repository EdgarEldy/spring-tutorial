package edgareldy.springtutorial.web.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Bootstraps the application on a Servlet 5.0+ container without a web.xml. Detected and
 * invoked automatically at startup by Spring's SpringServletContainerInitializer, itself
 * discovered through the Servlet container's ServiceLoader mechanism (META-INF/services,
 * provided transitively by spring-web). Registers a single DispatcherServlet backed by
 * WebMvcConfig and mapped under /api/v1, matching the API prefix announced by the README.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebMvcConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher", dispatcherServlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/api/v1/*");
    }
}
