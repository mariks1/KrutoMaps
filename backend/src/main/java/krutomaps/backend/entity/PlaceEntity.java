package krutomaps.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.io.Serializable;
import java.util.List;


@Entity
@Setter @Getter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "place")
public class PlaceEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "address_comment")
    private String addressComment;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "TEXT[]")
    private List<String> rubrics;

}
