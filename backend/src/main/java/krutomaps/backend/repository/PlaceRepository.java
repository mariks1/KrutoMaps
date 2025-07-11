package krutomaps.backend.repository;

import krutomaps.backend.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query(value = "SELECT * FROM place WHERE rubrics && CAST(:rubrics AS text[])", nativeQuery = true)
    List<Place> findByRubrics(@Param("rubrics") String rubrics);}
