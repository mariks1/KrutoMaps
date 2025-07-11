package krutomaps.backend.repository;

import krutomaps.backend.entity.Realty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RealtyRepository extends JpaRepository<Realty, Long>, JpaSpecificationExecutor<Realty> {

    @Query("SELECT MIN(r.lease_price) FROM Realty r WHERE r.lease_price IS NOT NULL")
    Double findMinPrice();

    @Query("SELECT MAX(r.lease_price) FROM Realty r WHERE r.lease_price IS NOT NULL")
    Double findMaxPrice();

    @Query("SELECT MIN(r.total_area) FROM Realty r WHERE r.total_area IS NOT NULL")
    Double findMinArea();

    @Query("SELECT MAX(r.total_area) FROM Realty r WHERE r.total_area IS NOT NULL")
    Double findMaxArea();

    @Query(nativeQuery = true, value = """
        SELECT r.id AS realty_id, w.rubric, MIN(ST_Distance(r.geom, p.geom)) AS min_dist
        FROM realty r
        CROSS JOIN (SELECT unnest(CAST(:rubrics AS text[])) AS rubric) w
        CROSS JOIN LATERAL (
            SELECT p.geom
            FROM place p
            WHERE w.rubric = ANY(p.rubrics)
            ORDER BY r.geom <-> p.geom
            LIMIT 1
        ) p
        WHERE r.id IN (:candidateIds)
        GROUP BY r.id, w.rubric
        """)
    List<Object[]> findMinDistancesForRubrics(@Param("candidateIds") List<Long> candidateIds, @Param("rubrics") String rubrics);

}
