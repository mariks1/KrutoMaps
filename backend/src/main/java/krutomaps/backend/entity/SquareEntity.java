package krutomaps.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "square")
public class SquareEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer i;
    private Integer j;

    @Column(name = "top_left_lat", nullable = false)
    private Double topLeftLat;

    @Column(name = "top_left_lon", nullable = false)
    private Double topLeftLon;

    @Column(name = "bottom_right_lat", nullable = false)
    private Double bottomRightLat;

    @Column(name = "bottom_right_lon", nullable = false)
    private Double bottomRightLon;

    @Column(name = "center_lat", nullable = false)
    private Double centerLat;

    @Column(name = "center_lon", nullable = false)
    private Double centerLon;

    @OneToOne(mappedBy = "squareEntity", cascade = CascadeType.ALL)
    private SquareScoreEntity squareScoreEntity;

}
