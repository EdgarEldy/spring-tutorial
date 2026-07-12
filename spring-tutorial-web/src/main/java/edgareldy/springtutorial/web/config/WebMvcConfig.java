package edgareldy.springtutorial.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edgareldy.springtutorial.dao.config.PersistenceConfig;
import edgareldy.springtutorial.web.interceptor.RequestLoggingInterceptor;
import java.util.List;
import java.util.Locale;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Single application context assembling the whole application: Spring MVC
 * (@EnableWebMvc), the service layer (service-context.xml, imported via
 * &#64;ImportResource), and the persistence layer (PersistenceConfig, imported via
 * &#64;Import), plus &#64;ComponentScan over the web package for controllers, the
 * exception handler, the request-scoped bean, and the request correlation context.
 * Registered as the DispatcherServlet's own context by WebAppInitializer, with no
 * separate root context: a pure REST API has no view layer to isolate MVC beans from.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Configuration
@EnableWebMvc
@Import(PersistenceConfig.class)
@ImportResource("classpath:spring/service-context.xml")
@ComponentScan(basePackages = "edgareldy.springtutorial.web")
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLoggingInterceptor());
    }

    /**
     * Overrides the validator @EnableWebMvc would otherwise create automatically for
     * &#64;Valid on &#64;RequestBody DTOs, for the same reason as service-context.xml's
     * validator bean: Hibernate Validator's default message interpolator needs a
     * jakarta.el implementation this project does not depend on (HV000183).
     */
    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        return validator;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
}
