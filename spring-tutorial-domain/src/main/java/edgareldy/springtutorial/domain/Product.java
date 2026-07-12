package edgareldy.springtutorial.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * JPA entity mapping the products table, linked to its category through a lazy
 * many-to-one relation resolved explicitly via JPQL in the dao module (no automatic
 * Spring Data fetching here).
 * <p>
 * Created edgar.muhamyangabo on 7/5/26
 * Author : edgar.muhamyangabo
 * Date : 7/5/26
 * Project : spring-tutorial
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // No @NotNull here on purpose: the service layer resolves and assigns the category
    // after Bean Validation already ran on the incoming Product (see ProductServiceImpl),
    // so it is still null at validation time. The database NOT NULL constraint is the
    // real guard for this field.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "product_name")
    @NotBlank
    private String productName;

    @Column(name = "unit_price")
    @NotNull
    @Positive
    private Double unitPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
