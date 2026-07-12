package edgareldy.springtutorial.dao.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edgareldy.springtutorial.dao.CategoryDao;
import edgareldy.springtutorial.dao.CategoryDaoImpl;
import edgareldy.springtutorial.dao.CustomerDao;
import edgareldy.springtutorial.dao.CustomerDaoImpl;
import edgareldy.springtutorial.dao.OrderDao;
import edgareldy.springtutorial.dao.OrderDaoImpl;
import edgareldy.springtutorial.dao.ProductDao;
import edgareldy.springtutorial.dao.ProductDaoImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.Flyway;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Java Config assembling the persistence infrastructure of the dao module: DataSource
 * (profile-dependent), JPA EntityManagerFactory, transaction manager, Flyway migration,
 * and the DAO beans themselves. Kept as a single explicit configuration class with no
 * component-scanning, to contrast with the XML-plus-scanning style used in the service
 * module.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:jdbc.properties")
public class PersistenceConfig {

    private static final Log log = LogFactory.getLog(PersistenceConfig.class);

    @PostConstruct
    public void logStartup() {
        log.info("PersistenceConfig initialized: JPA context ready to open connections");
    }

    @PreDestroy
    public void logShutdown() {
        log.info("PersistenceConfig shutting down: releasing JPA resources");
    }

    /**
     * Resolves ${...} placeholders in @Value expressions against jdbc.properties and the
     * environment (system properties, environment variables). Needed explicitly here
     * because this module has no component-scanning to register it automatically.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Declared as a static bean method: BeanPostProcessor beans must be instantiated
     * before the other @Bean methods on this class are proxied by the container, and a
     * static factory method lets Spring create it without instantiating the enclosing
     * configuration class first.
     */
    @Bean
    public static BeanCreationLoggerPostProcessor beanCreationLoggerPostProcessor() {
        return new BeanCreationLoggerPostProcessor();
    }

    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:spring_tutorial;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        return new HikariDataSource(config);
    }

    @Bean
    @Profile("prod")
    public DataSource prodDataSource(
            @Value("${jdbc.url}") String url,
            @Value("${jdbc.username}") String username,
            @Value("${jdbc.password}") String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        Resource migrationScript = new ClassPathResource("db/migration/V1__init_schema.sql");
        if (!migrationScript.exists()) {
            throw new IllegalStateException("Flyway migration script not found on the classpath: " + migrationScript);
        }
        log.info("Loading Flyway migrations from " + migrationScript.getDescription());
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("edgareldy.springtutorial.domain");
        emf.setPersistenceProvider(new HibernatePersistenceProvider());

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
        jpaProperties.put("hibernate.show_sql", "false");
        emf.setJpaProperties(jpaProperties);

        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * Programmatic equivalent of @PersistenceContext field injection, needed because
     * this module registers every bean explicitly instead of relying on
     * PersistenceAnnotationBeanPostProcessor via component-scanning.
     */
    @Bean
    public EntityManager sharedEntityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    public CategoryDao categoryDao(EntityManager entityManager) {
        return new CategoryDaoImpl(entityManager);
    }

    @Bean
    public ProductDao productDao(EntityManager entityManager) {
        return new ProductDaoImpl(entityManager);
    }

    @Bean
    public CustomerDao customerDao(EntityManager entityManager) {
        return new CustomerDaoImpl(entityManager);
    }

    @Bean
    public OrderDao orderDao(EntityManager entityManager) {
        return new OrderDaoImpl(entityManager);
    }
}
