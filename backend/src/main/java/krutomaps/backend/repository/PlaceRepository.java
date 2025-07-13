package krutomaps.backend.repository;

import krutomaps.backend.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query(value = "SELECT * FROM place WHERE rubrics && CAST(:rubrics AS text[])", nativeQuery = true)
    List<Place> findByRubrics(@Param("rubrics") String rubrics);

    @Query(value = "SELECT * FROM place WHERE :rubric = ANY(rubrics)", nativeQuery = true)
    List<Place> findByRubric(@Param("rubric") String rubric);

    @Query(value = "SELECT DISTINCT unnest(rubrics) AS rubric FROM place", nativeQuery = true)
    List<String> findAllUniqueRubrics();


}
