package edgareldy.springtutorial.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the categories table. Plain jakarta.persistence.* annotations only,
 * no Spring dependency, consistent with this module being pure Java.
 * <p>
 * Created edgar.muhamyangabo on 7/5/26
 * Author : edgar.muhamyangabo
 * Date : 7/5/26
 * Project : spring-tutorial
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
