package com.pdev.fitnessMono.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /* recommended for @ManyToOne relationships to improve performance
    by loading the related entity only when it's accessed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_activity_user"))
    @JsonIgnore
    private  User user;

   /* otherwise the index of the enum will be stored in the database,
    which is not human-readable and can lead to issues if the order
    of the enum values changes
    */
    @Enumerated(EnumType.STRING)
    private ActivityType type;

    // Essential fields
    private Integer duration; // Duration in minutes
    private Integer caloriesBurned; // Optional - can be calculated or estimated
    private LocalDateTime startTime;
    
    // Optional advanced fields (can be filled later)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private ActivityMetrics metrics; // Optional: detailed metrics for advanced users

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private ActivityContext context; // Optional: weather, equipment, intensity, etc.

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Recommendations> recommendations = new ArrayList<>();

}
