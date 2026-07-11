# Spring Tutorial

A hands-on tutorial on **Spring Framework** (without Spring Boot), organized into Git branches, whose goal is to cover almost every notion of the **Spring Core** foundation (IoC container, dependency injection, bean lifecycle, AOP, SpEL, validation, resources, i18n, application events...) through a real REST API structured as a **Maven multi-module** project (see [Approach](#approach) for how the two combine).

The data model follows the same schema as `spring-boot-tutorial`: `categories` → `products` → `orders` ← `customers`.

All API routes are prefixed with **`/api/v1`**.

This document is the **complete specification** of the project: it is meant to be followed step by step to implement each branch.

## Table of contents

- [Approach](#approach)
- [Spring Core fundamentals](#spring-core-fundamentals)
- [Tech stack](#tech-stack)
- [Data model](#data-model)
- [Module structure](#module-structure)
- [Branching strategy](#branching-strategy)
- [Standard response format](#standard-response-format)
- [Spring AOP](#spring-aop)
- [feature/core-architecture](#featurecore-architecture)
- [feature/dao](#featuredao)
- [feature/service](#featureservice)
- [feature/web](#featureweb)
- [Order of work](#order-of-work)
- [Code conventions](#code-conventions)
- [Spring Core concepts covered](#spring-core-concepts-covered)
- [How to follow this tutorial](#how-to-follow-this-tutorial)

## Approach

The project is built on Spring Framework 6.2.x alone, without Spring Boot, so the mechanisms of the "raw" Spring container can be exercised and taught explicitly:

- **Manual context assembly** (Java Config and XML, in two different modules, to compare both styles)
- **Bean lifecycle, scopes, and container extension points** (`BeanPostProcessor`, `FactoryBean`) wired by hand
- **AOP, SpEL, validation, i18n, application events** implemented explicitly
- A **classic WAR deployment on an external Tomcat**, via `WebApplicationInitializer`
- **Manually configured declarative transaction management** (explicit `PlatformTransactionManager`)

The **Maven multi-module** architecture (a parent POM + 5 modules with real compilation dependencies between them, breaking `dao` breaks compilation of `service`) is a valid destination on its own if that is what you are looking for. It is also the vehicle chosen here to teach Spring Core: each module is organized so that the Spring Core mechanisms it demonstrates are easy to point to and follow.

The project only produces a REST API (module `web`), to stay focused on API development.

## Spring Core fundamentals

Explaining how the Spring container actually works, not just how to annotate a class, is the actual goal of this tutorial.

- **Inversion of Control (IoC)**: instead of an object constructing or looking up its own dependencies, it declares what it needs (through a constructor, in this project) and something external supplies them. That "something external" is the **container**: it builds the entire object graph of the application (DAOs, services, controllers, infrastructure beans) and wires it together, so no class ever calls `new` on a collaborator it depends on.
- **`BeanFactory` vs `ApplicationContext`**: `BeanFactory` is the minimal container contract (register bean definitions, get beans, wire dependencies). `ApplicationContext` is the interface actually used everywhere in this project: it is a `BeanFactory` plus extra services a real application needs - publishing/consuming application events, resolving messages through `MessageSource` (see `feature/web`), loading `Resource`s uniformly regardless of where they live (classpath, filesystem, URL - see `feature/dao`), and reading configuration through the `Environment` abstraction.
- **Configuration metadata is just data, not code**: whether a bean is declared in XML (`service-context.xml` in `feature/service`), in Java (`@Configuration`/`@Bean` in `PersistenceConfig`, `feature/dao`), or discovered via `@Component` and classpath scanning, the container turns all three into the same internal representation (a `BeanDefinition` graph) before wiring anything. This project deliberately mixes all three styles across modules specifically to make that equivalence visible, instead of picking one and hiding the others.
- **Bean lifecycle, step by step**: for each singleton bean the container (1) instantiates it, (2) injects its declared dependencies, (3) runs any `BeanPostProcessor.postProcessBeforeInitialization`, (4) runs initialization callbacks (`@PostConstruct`, `InitializingBean.afterPropertiesSet()`), (5) runs `BeanPostProcessor.postProcessAfterInitialization` (this is the step that lets Spring AOP wrap a bean in a proxy transparently, see [Spring AOP](#spring-aop)), and finally (6) on container shutdown, runs destruction callbacks (`@PreDestroy`, `DisposableBean.destroy()`). `feature/dao` makes steps 2-4 and the `BeanPostProcessor` extension point observable on purpose (see its task list), since Spring Boot normally runs this whole sequence silently.
- **Bean scopes control identity, not just configuration**: `singleton` (the default) means the container hands out the exact same instance to every injection point; `prototype` means a brand-new instance is created every time one is requested, so it cannot safely hold container-managed dependencies the way a singleton does without care; `request` (used in `feature/web`) means one instance per HTTP request, made possible by a scoped proxy that forwards each call to the instance for the currently active request.
- **The `Environment` abstraction and profiles**: application code should ask the container "what is the value of this property," not care whether the value came from a `.properties` file, an environment variable, or a system property - `Environment`/`@Value` unify all of those sources. `@Profile("dev")`/`@Profile("prod")` (in `feature/dao`) let the same codebase register a *different* bean (e.g. a different `DataSource`) depending on which profiles are active, without an `if` statement anywhere in application code.
- **Aspect-oriented programming as a container extension, not a language feature**: cross-cutting concerns (logging, timing) are woven in by having the container substitute a bean with a dynamically generated proxy around it, at the `BeanPostProcessor` step above; see [Spring AOP](#spring-aop) for how this project makes that proxying visible instead of leaving it implicit.

## Tech stack

| Component | Choice |
|---|---|
| Framework | Spring Framework 6.2.x (classic, without Spring Boot) |
| Language | Java 17 |
| Build | Maven multi-module (parent `pom.xml` + module `pom.xml` files) |
| Database | PostgreSQL 16 |
| ORM | Hibernate / JPA (manual configuration, `LocalContainerEntityManagerFactoryBean`) |
| Migrations | Flyway (run at startup, configured in the `dao` module) |
| API | Annotated Spring MVC (`@RestController`), JSON via Jackson |
| AOP | Spring AOP (proxy) + AspectJ (`aspectjweaver`), aspects annotated `@Aspect` |
| Validation | Bean Validation 3.0 (Hibernate Validator), `@Valid` on DTOs |
| I18n | `MessageSource` (`ReloadableResourceBundleMessageSource`), `Locale` resolved from `Accept-Language` |
| Server | External Tomcat 10 (WAR packaging) |
| Tests | JUnit 5, Mockito, `spring-test`, MockMvc |
| CI/CD | GitHub Actions (multi-module build, `mvn -pl ... -am`) |
| Containerization | Docker (multi-stage build), `docker-compose` for local runs |

## Data model

```
categories (id, category_name)
    │ 1
    │
    │ N
products (id, category_id, product_name, unit_price)
    │ 1
    │
    │ N
orders (id, customer_id, product_id, quantity, total)
    │ N
    │
    │ 1
customers (id, first_name, last_name, telephone, email, address)
```

Columns and constraints are identical to `spring-boot-tutorial` (see its README for details).

## Module structure

```
spring-tutorial/                          (parent pom, packaging=pom)
├── pom.xml                               (dependencyManagement, modules, common plugins)
│
├── spring-tutorial-common/               (jar - shared exceptions)
│   └── src/main/java/edgareldy/springtutorial/common/
│       └── exception/
│           ├── ResourceNotFoundException.java
│           └── BusinessRuleException.java
│
├── spring-tutorial-domain/               (jar - JPA entities, no Spring dependency)
│   └── src/main/java/edgareldy/springtutorial/domain/
│       ├── Category.java
│       ├── Product.java
│       ├── Customer.java
│       └── Order.java
│
├── spring-tutorial-dao/                  (jar - data access, depends on domain + common)
│   └── src/main/java/edgareldy/springtutorial/dao/
│       ├── config/
│       │   └── PersistenceConfig.java    (JavaConfig: DataSource, EntityManagerFactory, TransactionManager)
│       ├── CategoryDao.java / CategoryDaoImpl.java
│       ├── ProductDao.java / ProductDaoImpl.java
│       ├── CustomerDao.java / CustomerDaoImpl.java
│       └── OrderDao.java / OrderDaoImpl.java
│
├── spring-tutorial-service/               (jar - business logic, depends on dao)
│   └── src/main/
│       ├── java/edgareldy/springtutorial/service/
│       │   ├── CategoryService.java / impl/CategoryServiceImpl.java
│       │   ├── ProductService.java / impl/ProductServiceImpl.java
│       │   ├── CustomerService.java / impl/CustomerServiceImpl.java
│       │   ├── OrderService.java / impl/OrderServiceImpl.java
│       │   ├── event/
│       │   │   ├── OrderCreatedEvent.java
│       │   │   └── OrderCreatedEventListener.java
│       │   └── aspect/
│       │       └── LoggingAspect.java
│       └── resources/
│           └── spring/service-context.xml   (XML config: <tx:annotation-driven/>, <aop:aspectj-autoproxy/>, <context:component-scan/>)
│
└── spring-tutorial-web/                 (war - JSON REST API, depends on service)
    └── src/main/java/edgareldy/springtutorial/web/
        ├── config/
        │   ├── WebAppInitializer.java   (WebApplicationInitializer, replaces web.xml)
        │   └── WebMvcConfig.java           (@EnableWebMvc, JSON message converters, imports service-context.xml and PersistenceConfig)
        ├── dto/
        │   ├── common/ (ApiResponse.java, PageResponse.java)
        │   ├── category/ (CategoryRequest, CategoryResponse)
        │   ├── product/ (ProductRequest, ProductResponse)
        │   ├── customer/ (CustomerRequest, CustomerResponse)
        │   └── order/ (OrderRequest, OrderResponse)
        ├── mapper/ (CategoryMapper, ProductMapper, CustomerMapper, OrderMapper)
        ├── controller/
        │   ├── CategoryController.java
        │   ├── ProductController.java
        │   ├── CustomerController.java
        │   └── OrderController.java
        └── exception/
            └── WebExceptionHandler.java  (@ControllerAdvice, ApiResponse<Void> responses)
```

## Branching strategy

| Branch | Role |
|---|---|
| `master` | Stable code. No direct commits, only merges from `develop`. |
| `develop` | Integration branch. |
| `feature/core-architecture` | Parent POM + `common` and `domain` modules. |
| `feature/dao` | `spring-tutorial-dao` module (data access, Java Config). |
| `feature/service` | `spring-tutorial-service` module (business logic, XML config). |
| `feature/web` | `spring-tutorial-web` module (JSON API, `@RestController`, `/api/v1` prefix, WAR). |

Each branch corresponds almost one-to-one with a Maven module: this is deliberate, so the Git history illustrates the progressive construction of the module dependency tree.

## Standard response format

Every API response (success and error alike) is wrapped in a generic `ApiResponse<T>` DTO, defined in `dto/common/ApiResponse.java`, to keep a consistent contract across all endpoints.

```java
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}
```

- On list endpoints, `data` holds a `PageResponse<T>` (paginated content) instead of a plain `List<T>`.
- On error, `WebExceptionHandler` (`@ControllerAdvice`, in `web/exception/`) returns an `ApiResponse<Void>` with `success=false` and an explicit `message`, mapping each exception to an HTTP status:
  - `ResourceNotFoundException` (from `spring-tutorial-common`) → 404
  - `MethodArgumentNotValidException` (Bean Validation failure on a `@Valid` request body) → 400, message resolved through `MessageSource`
  - `BusinessRuleException` (from `spring-tutorial-common`) → 422
  - Any other uncaught `Exception` → 500
- Error response example:

```json
{
  "success": false,
  "message": "Category not found",
  "data": null,
  "timestamp": "2026-07-05T10:16:05Z"
}
```

## Spring AOP

The project uses **Spring AOP** to illustrate aspect-oriented programming, kept out of any business logic in services/controllers.

- Dependency: `aspectjweaver`, enabled via `<aop:aspectj-autoproxy/>` in `service-context.xml`
- `LoggingAspect` (`service/aspect/LoggingAspect.java`): `@Around` advice on all public methods of `service.impl` (pointcut `execution(* edgareldy.springtutorial.service.impl..*(..))`), logs method entry/exit, arguments, execution time, and thrown exceptions
- Also serves as a teaching base for the other advice types (`@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`) alongside `@Around`
- Illustrates the proxy-based nature of Spring AOP: since services are Spring-managed beans injected by interface (`CategoryService`, `ProductService`, ...), a JDK dynamic proxy is used rather than a CGLIB subclass proxy

## feature/core-architecture

Technical foundation shared by the whole project, to be merged first into `develop`.
Originally named `feature/config`; renamed to better reflect that it lays out the parent POM
and the two dependency-free foundation modules (`common`, `domain`), not just configuration
files.

### Tasks

- [x] Parent `pom.xml`: `packaging=pom`, `<modules>` section listing the 5 modules, `<dependencyManagement>` centralizing versions (Spring Framework, Hibernate, PostgreSQL driver, JUnit, etc.), `maven-compiler-plugin` (Java 17) declared once
- [x] `spring-tutorial-common` module: shared exceptions (`ResourceNotFoundException`, `BusinessRuleException`), no Spring dependency (deliberately a "pure Java" module)
- [x] `spring-tutorial-domain` module: JPA entities `Category`, `Product`, `Customer`, `Order` (`jakarta.persistence.*` annotations only, no Spring dependency either)
- [x] Flyway script `V1__init_schema.sql` placed under `spring-tutorial-dao/src/main/resources/db/migration`
- [x] `.github/workflows/ci.yml`: multi-module build (`mvn -T 1C clean verify` at the root, compiling all modules in the correct order thanks to the Maven dependency graph)
- [x] Multi-stage `Dockerfile` (Maven build stage + Tomcat 10 image, deploys the WAR to `webapps/`)
- [x] `docker-compose.yml`: `web` service built from the `Dockerfile`, joining an externally managed PostgreSQL container instead of declaring its own `db` service (see Configuration notes)
- [ ] Branch README explaining the role of the parent POM

### Configuration notes

- **No Spring Boot BOM here**: unlike the Boot tutorials in this series, there is no
  `spring-boot-dependencies` import to guarantee that Spring Framework, Hibernate, and the
  Jakarta Persistence API versions are mutually compatible. Verify the version matrix
  manually before locking versions in the parent `<dependencyManagement>` (Spring Framework
  6.2.x requires Hibernate 6.x built against `jakarta.persistence` 3.1, and a Jakarta EE 9+
  Tomcat, i.e. Tomcat 10, not 9).
- **H2 vs. Testcontainers for `dao` integration tests: H2 was chosen**, once `feature/dao`
  needed to decide. It is not only a test convenience: the `@Profile("dev")` `DataSource` in
  `PersistenceConfig` embeds H2 directly, so the dev profile and the DAO integration tests
  share the exact same database engine instead of introducing a second one just for tests.
  `feature/service` and `feature/web` tests should stay consistent with this choice.
- **`docker-compose.yml` does not declare its own PostgreSQL service.** In this development
  environment, a single long-lived `postgres_main` container (PostgreSQL 16) is already
  shared across several local projects, on an external Docker network (`pg_net`); the `web`
  service here joins that network and connects to a dedicated `spring_tutorial` database
  created once inside it (`docker exec postgres_main psql -U admin -d maindb -c "CREATE
  DATABASE spring_tutorial;"`), instead of starting a redundant, conflicting PostgreSQL
  container on the same host port. A reader without an equivalent shared container should
  add their own `db: image: postgres:16` service (with a matching `JDBC_URL`) rather than
  rely on `pg_net` existing.

## feature/dao

Depends on `common` and `domain`.

### Tasks

- [x] `PersistenceConfig` (Java Config): `DataSource` (connection pool), `LocalContainerEntityManagerFactoryBean` (illustrates the `FactoryBean` pattern), `JpaTransactionManager`, exposed as Spring beans
- [x] Spring profiles (`@Profile("dev")` / `@Profile("prod")`) for two different `DataSource` beans (embedded H2 in dev, PostgreSQL in prod), activated via `spring.profiles.active`, to illustrate the `Environment` abstraction
- [x] `@PropertySource("classpath:jdbc.properties")` + `Environment`/`@Value` to inject the connection URL and credentials, instead of hard-coding them in `PersistenceConfig`
- [x] Bean lifecycle made explicit: `@PostConstruct`/`@PreDestroy` methods (e.g. logging connection pool startup/shutdown)
- [x] A teaching `BeanPostProcessor` (e.g. `BeanCreationLoggerPostProcessor`) that logs the name of every bean instantiated in the `dao` context, to make a normally invisible container extension point visible
- [x] `CategoryDao`, `ProductDao`, `CustomerDao`, `OrderDao` interfaces (contracts) + `*DaoImpl` implementations using `EntityManager` directly (no Spring Data here, manual DAO to properly show the JPA mechanics)
- [x] Explicit JPQL queries for relations (`Product → Category`, `Order → Customer/Product`)
- [x] Loading the Flyway script through Spring's `Resource` abstraction (`ClassPathResource`), to illustrate the resource abstraction independently of the file system
- [x] Integration tests for the `dao` module with an H2 database, Spring context loaded via the `SpringExtension`/`@ContextConfiguration` JUnit 5 integration (the same `AnnotationConfigApplicationContext`-based mechanism, just not instantiated by hand)

## feature/service

Depends on `dao`. Uses **XML configuration** (deliberately different from the Java Config used in the `dao` module, to compare both approaches within the same project).

### Tasks

- [ ] `service-context.xml`: `<context:component-scan base-package="edgareldy.springtutorial.service"/>`, `<tx:annotation-driven/>`, `<aop:aspectj-autoproxy/>`
- [ ] `CategoryService`, `ProductService`, `CustomerService`, `OrderService` interfaces at the root of `service/`, implementations in `service/impl/`, annotated `@Transactional`
- [ ] Business rules shared with the other tutorials: deleting a non-empty category is forbidden, `total` computed on orders
- [ ] Constructor injection with `@Qualifier`: at least two beans of the same type (e.g. two `Clock` beans, `Clock.systemDefaultZone()` in production and a fixed `Clock` injectable in tests) to illustrate disambiguation by qualifier
- [ ] A `prototype`-scoped bean (e.g. an order reference generator), contrasted with the `singleton` services used by default
- [ ] `OrderCreatedEvent` application event published via `ApplicationEventPublisher` from `OrderServiceImpl`, consumed by an `@EventListener` (e.g. audit logging) in another bean, to illustrate producer/consumer decoupling within the same context
- [ ] `LoggingAspect` (`service/aspect/`) implementing the mechanism described in [Spring AOP](#spring-aop)
- [ ] Use of SpEL in a `@Value` annotation (e.g. a computed default page size) and/or in the XML (`#{...}`)
- [ ] Business validation with Bean Validation (`@Valid`, `jakarta.validation.constraints.*`) on objects passed to services, and/or a custom `org.springframework.validation.Validator` for a rule that cannot be expressed with annotations
- [ ] Unit tests (Mockito) and integration tests loading `service-context.xml` via `ClassPathXmlApplicationContext`

## feature/web

Depends on `service`. The only entry point of the project, exclusively a JSON API.

### Endpoints

All routes are prefixed with `/api/v1`.

| Method | URL | Description |
|---|---|---|
| GET | `/api/v1/categories` | Paginated list |
| GET | `/api/v1/categories/{id}` | Detail |
| POST | `/api/v1/categories` | Create |
| PUT | `/api/v1/categories/{id}` | Update |
| DELETE | `/api/v1/categories/{id}` | Delete |
| GET | `/api/v1/products` | Paginated list, filterable by `categoryId` |
| GET | `/api/v1/products/{id}` | Detail |
| POST | `/api/v1/products` | Create |
| PUT | `/api/v1/products/{id}` | Update |
| DELETE | `/api/v1/products/{id}` | Delete |
| GET | `/api/v1/customers` | Paginated list, search by name |
| GET | `/api/v1/customers/{id}` | Detail |
| POST | `/api/v1/customers` | Create |
| PUT | `/api/v1/customers/{id}` | Update |
| DELETE | `/api/v1/customers/{id}` | Delete |
| GET | `/api/v1/orders` | Paginated list, filterable by `customerId`/`productId` |
| GET | `/api/v1/orders/{id}` | Detail |
| POST | `/api/v1/orders` | Create (computes `total`) |
| PUT | `/api/v1/orders/{id}` | Update |
| DELETE | `/api/v1/orders/{id}` | Delete |

### Tasks

- [ ] `WebAppInitializer` (`WebApplicationInitializer`): registers the `DispatcherServlet`, replaces `web.xml`
- [ ] `WebMvcConfig` (`@EnableWebMvc`): maps the `DispatcherServlet` to `/api/v1/*`, configures Jackson `HttpMessageConverter`s, imports the `service-context.xml` context and `PersistenceConfig`
- [ ] Request/Response DTOs + mappers (same conventions as `spring-boot-tutorial`: `record` DTOs, never a JPA entity exposed directly), annotated `@Valid`/`@NotNull`/`@Size` to validate incoming requests
- [ ] Generic `ApiResponse<T>` and `PageResponse<T>` DTOs (`dto/common/`), wrapping every response
- [ ] `WebExceptionHandler` (`@ControllerAdvice`, in `web/exception/`) implementing the exception-to-status mapping described in [Standard response format](#standard-response-format) (404/400/422/500)
- [ ] `MessageSource` (`ReloadableResourceBundleMessageSource`) + `messages_fr.properties`/`messages_en.properties` files, locale resolved from the `Accept-Language` header, to illustrate internationalization
- [ ] A `HandlerInterceptor` (e.g. `RequestLoggingInterceptor`) registered via `WebMvcConfigurer#addInterceptors`, to illustrate an MVC extension point outside of Servlet filters
- [ ] A `request`-scoped bean (scoped proxy, e.g. a request correlation context) injected into a controller, to illustrate web-related scopes
- [ ] `@RestController` controllers per resource (`@RequestMapping("/api/v1/...")`), delegating only to the `service` layer
- [ ] `MockMvc` tests for the `web` module

## Order of work

1. `feature/core-architecture` → Pull Request to `develop`
2. `feature/dao` (depends on `config`) → Pull Request to `develop`
3. `feature/service` (depends on `dao`) → Pull Request to `develop`
4. `feature/web` (depends on `service`) → Pull Request to `develop`
5. `develop` → `master`

## Code conventions

- Root package: `edgareldy.springtutorial`, with a sub-package per module (`.common`, `.domain`, `.dao`, `.service`, `.web`)
- **Contract/implementation services** (like the other tutorials in the series): interface at the root of `service/`, implementation in `service/impl/`
- Every exposed route is prefixed with `/api/v1`
- The `web` module always returns a generic `ApiResponse<T>` DTO (or `ApiResponse<PageResponse<T>>` for lists)
- No `domain` entity is ever exposed directly in an HTTP response: DTOs only
- Each module has its own `pom.xml` and declares only the dependencies it actually needs (no abusive inheritance of unused transitive dependencies)

## Spring Core concepts covered

The goal is to cover almost the entire "Core Technologies" chapter of the Spring Framework reference documentation. Non-exhaustive list, with the branch where each notion is demonstrated.

### IoC container and dependency injection

- `BeanFactory` vs `ApplicationContext` (`feature/dao`, `feature/web`)
- Annotation-based configuration (`@Component`, `@Service`, `@Repository`, `@Autowired`, `@Qualifier`, `@Value`) (`feature/service`, `feature/web`)
- Java configuration (`@Configuration`, `@Bean`) (`feature/dao`)
- XML configuration (`<bean>`, `<context:component-scan>`, `<tx:annotation-driven/>`, `<aop:aspectj-autoproxy/>`) (`feature/service`)
- Bean scopes: `singleton`, `prototype`, `request` (`feature/service`, `feature/web`)
- Bean lifecycle: `@PostConstruct`/`@PreDestroy`, `InitializingBean`/`DisposableBean` (`feature/dao`)
- Container extension points: `BeanPostProcessor`, `FactoryBean` (`feature/dao`)
- `Environment` and `PropertySource` abstraction, profiles (`@Profile`) (`feature/dao`)
- Application events (`ApplicationEventPublisher`, `@EventListener`) (`feature/service`)
- Internationalization via `MessageSource` (`feature/web`)
- `Resource` abstraction (`ClassPathResource`) (`feature/dao`)

### AOP (aspect-oriented programming)

- `@Aspect`, pointcuts, `@Around` advice (`feature/service`)
- JDK dynamic proxy vs CGLIB proxy, explained in [Spring AOP](#spring-aop)

### Validation and data binding

- Bean Validation (`@Valid`, `jakarta.validation.constraints.*`) (`feature/service`, `feature/web`)
- Custom `org.springframework.validation.Validator` (`feature/service`)

### Spring Expression Language (SpEL)

- Used in `@Value` and/or in XML (`feature/service`)

### Spring MVC (web layer, needed to expose an API on top of the container)

- `DispatcherServlet`, `WebApplicationInitializer`, WAR packaging (`feature/web`)
- `@RestController`, `@RequestMapping`, Jackson `HttpMessageConverter` (`feature/web`)
- `HandlerInterceptor` (`feature/web`)
- `@ControllerAdvice` / centralized exception handling (`feature/web`)

### Data access and transactions

- Manual `EntityManager` (no Spring Data) (`feature/dao`)
- Declarative transaction management (`@Transactional`, `PlatformTransactionManager`, `<tx:annotation-driven/>`) (`feature/dao`, `feature/service`)

### Architecture and tooling

- Maven multi-module (parent POM, `dependencyManagement`, module dependency graph)
- Contract/implementation pattern for services and DAOs
- Generic `ApiResponse<T>` DTO
- Multi-module tests (`AnnotationConfigApplicationContext`, `ClassPathXmlApplicationContext`, Mockito, `MockMvc`)
- Continuous integration on a multi-module Maven build

## How to follow this tutorial

1. Clone the repository and check out `develop`
2. Follow the branches in order: `feature/core-architecture` → `feature/dao` → `feature/service` → `feature/web`
3. Build all modules from the root: `mvn clean install`
4. Either run `docker-compose up` (builds the WAR and deploys it on Tomcat 10, see Configuration notes for the PostgreSQL connection), or deploy `spring-tutorial-web/target/spring-tutorial-web.war` manually on a local Tomcat 10
5. Call the API at `http://localhost:8080/spring-tutorial-web/api/v1/...`
