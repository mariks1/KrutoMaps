package krutomaps.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "square")
public class Square {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "i", nullable = false)
    private Integer i;

    @Column(name = "j", nullable = false)
    private Integer j;

    @Column(name = "top_left_lat", nullable = false)
    private Double top_left_lat;

    @Column(name = "top_left_lon", nullable = false)
    private Double top_left_lon;

    @Column(name = "bottom_right_lat", nullable = false)
    private Double bottom_right_lat;

    @Column(name = "bottom_right_lon", nullable = false)
    private Double bottom_right_lon;

    @Column(name = "center_lat", nullable = false)
    private Double center_lat;

    @Column(name = "center_lon", nullable = false)
    private Double center_lon;

    @OneToOne(mappedBy = "square", cascade = CascadeType.ALL)
    private SquareScore squareScore;

}
