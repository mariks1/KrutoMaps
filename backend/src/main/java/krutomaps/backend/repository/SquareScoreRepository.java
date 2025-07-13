package krutomaps.backend.repository;

import krutomaps.backend.entity.SquareScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SquareScoreRepository extends JpaRepository<SquareScore, Long> {

}
