package krutomaps.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "realty")
public class Realty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_x", nullable = false)
    private Double point_x;

    @Column(name = "point_y", nullable = false)
    private Double point_y;

    @Column(name = "main_type", nullable = false)
    private String main_type;

    @Column(name = "segment_type", nullable = false)
    private String segment_type;

    @Column(name = "entity_name", nullable = false)
    private String entity_name;

    @Column(name = "total_area", nullable = false)
    private Double total_area;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "lease_price", nullable = false)
    private Double lease_price;

    @Column(name = "additional_info", nullable = false)
    private String additional_info;

    @Column(name = "source_info", nullable = false)
    private String source_info;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "update_date", nullable = false)
    private LocalDate update_date;

}