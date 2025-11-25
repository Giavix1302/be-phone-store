package fit.se.be_phone_store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Brand Entity - Matches database schema exactly with helper methods
 */
@Entity
@Table(name = "brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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