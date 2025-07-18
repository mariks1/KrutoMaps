package krutomaps.backend.repository;

import krutomaps.backend.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(exported = false)
public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {

    @Query(
            value      = "SELECT * FROM place WHERE rubrics && CAST(?1 AS text[])",
            nativeQuery= true
    )
    List<PlaceEntity> findByRubrics(@Param("rubrics") String[] rubrics);

    @Query(value = "SELECT * FROM place WHERE :rubric = ANY(rubrics)", nativeQuery = true)
    List<PlaceEntity> findByRubric(@Param("rubric") String rubric);

    @Query(value = "SELECT DISTINCT unnest(rubrics) AS rubric FROM place", nativeQuery = true)
    List<String> findAllUniqueRubrics();


}
