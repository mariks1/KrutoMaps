package krutomaps.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Table(name = "square_score")
public class SquareScoreEntity implements Serializable {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private SquareEntity squareEntity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String rubricScore;

    @Version
    private Long version;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdate;

    @PrePersist @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

}
