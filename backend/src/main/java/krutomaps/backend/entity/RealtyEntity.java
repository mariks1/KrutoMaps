package krutomaps.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "realty")
@EqualsAndHashCode(of = "id")
public class RealtyEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_x", nullable = false)
    private Double pointX;

    @Column(name = "point_y", nullable = false)
    private Double pointY;

    @Column(name = "main_type", nullable = false)
    private String mainType;

    @Column(name = "segment_type", nullable = false)
    private String segmentType;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "total_area", nullable = false)
    private Double totalArea;

    private Integer floor;

    @Column(name = "lease_price", nullable = false)
    private Double leasePrice;

    @Column(name = "additional_info", nullable = false)
    private String additionalInfo;

    @Column(name = "source_info", nullable = false)
    private String sourceInfo;

    @Column(nullable = false)
    private String address;

    @Column(name = "update_date", nullable = false)
    private LocalDate updateDate;

    @Column(name = "square_num", nullable = false)
    private Long squareNum;
}