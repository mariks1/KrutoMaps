package krutomaps.backend.repository;

import krutomaps.backend.entity.SquareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(exported = false)
public interface SquareRepository extends JpaRepository<SquareEntity, Long> {

    @Query("SELECT s.id FROM SquareEntity s")
    List<Long> findAllIds();

    List<SquareEntity> findByIBetweenAndJBetween(int minI, int maxI, int minJ, int maxJ);
}
