package fit.se.be_phone_store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Entity - Updated to use VARCHAR for name field
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to get product count
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }

    // Helper method to get active product count
    public long getActiveProductCount() {
        if (products == null) return 0;
        return products.stream()
                .filter(product -> product.getIsActive() != null && product.getIsActive())
                .count();
    }

    // Helper method to get inactive product count
    public long getInactiveProductCount() {
        if (products == null) return 0;
        return products.stream()
                .filter(product -> product.getIsActive() == null || !product.getIsActive())
                .count();
    }
}