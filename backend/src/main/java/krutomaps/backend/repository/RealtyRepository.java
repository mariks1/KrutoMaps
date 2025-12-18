package krutomaps.backend.repository;

import krutomaps.backend.entity.RealtyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RealtyRepository extends JpaRepository<RealtyEntity, Long>, JpaSpecificationExecutor<RealtyEntity> {

    @Query(value = """
        WITH candidates AS (
          SELECT r.*
          FROM realty r
          WHERE (:priceFrom IS NULL OR r.lease_price >= :priceFrom)
            AND (:priceTo   IS NULL OR r.lease_price <= :priceTo)
            AND (:areaFrom  IS NULL OR r.total_area  >= :areaFrom)
            AND (:areaTo    IS NULL OR r.total_area  <= :areaTo)
            AND r.geom IS NOT NULL
            AND (
                CAST(:segmentTypes AS text[]) IS NULL
                OR r.segment_type = ANY(CAST(:segmentTypes AS text[]))
            )
        )
        SELECT c.id as id,
               COALESCE(s.score, 0) as score
        FROM candidates c
        LEFT JOIN LATERAL (
          SELECT SUM(
              (
                CASE WHEN CAST(:want AS text[])  IS NOT NULL AND p.rubrics && CAST(:want  AS text[]) THEN 1 ELSE 0 END
              - CASE WHEN CAST(:avoid AS text[]) IS NOT NULL AND p.rubrics && CAST(:avoid AS text[]) THEN 1 ELSE 0 END
              )
              * (1.0 / (1.0 + ST_Distance(c.geom, p.geom)))
          ) AS score
          FROM place p
          WHERE ST_DWithin(c.geom, p.geom, :radiusMeters)
            AND (
                 (CAST(:want  AS text[]) IS NOT NULL AND p.rubrics && CAST(:want  AS text[]))
              OR (CAST(:avoid AS text[]) IS NOT NULL AND p.rubrics && CAST(:avoid AS text[]))
            )
        ) s ON TRUE
        ORDER BY score DESC
        LIMIT 5
        """, nativeQuery = true)
    List<RealtyScoreRow> findTop5Scored(
            @Param("priceFrom") Double priceFrom,
            @Param("priceTo") Double priceTo,
            @Param("areaFrom") Double areaFrom,
            @Param("areaTo") Double areaTo,
            @Param("segmentTypes") String[] segmentTypes,
            @Param("want") String[] want,
            @Param("avoid") String[] avoid,
            @Param("radiusMeters") double radiusMeters
    );

    @Query("SELECT MIN(r.leasePrice) FROM RealtyEntity r WHERE r.leasePrice IS NOT NULL")
    Double findMinPrice();

    @Query("SELECT MAX(r.leasePrice) FROM RealtyEntity r WHERE r.leasePrice IS NOT NULL")
    Double findMaxPrice();

    @Query("SELECT MIN(r.totalArea) FROM RealtyEntity r WHERE r.totalArea IS NOT NULL")
    Double findMinArea();

    @Query("SELECT MAX(r.totalArea) FROM RealtyEntity r WHERE r.totalArea IS NOT NULL")
    Double findMaxArea();
}

