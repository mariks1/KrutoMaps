package krutomaps.backend.repository;

import krutomaps.backend.entity.Square;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquareRepository extends JpaRepository<Square, Long> {

    @Query("SELECT s.id FROM Square s")
    List<Long> findAllIds();

    List<Square> findByIBetweenAndJBetween(int minI, int maxI, int minJ, int maxJ);
}
