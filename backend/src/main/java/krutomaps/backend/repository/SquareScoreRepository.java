package krutomaps.backend.repository;

import krutomaps.backend.entity.SquareScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;


@Repository
@RepositoryRestResource(exported = false)
public interface SquareScoreRepository extends JpaRepository<SquareScoreEntity, Long> { }
