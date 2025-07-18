package krutomaps.backend.repository;

import krutomaps.backend.entity.RealtyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(exported = false)
public interface RealtyRepository extends JpaRepository<RealtyEntity, Long>, JpaSpecificationExecutor<RealtyEntity> {

    @Query("SELECT MIN(r.leasePrice) FROM RealtyEntity r WHERE r.leasePrice IS NOT NULL")
    Double findMinPrice();

    @Query("SELECT MAX(r.leasePrice) FROM RealtyEntity r WHERE r.leasePrice IS NOT NULL")
    Double findMaxPrice();

    @Query("SELECT MIN(r.totalArea) FROM RealtyEntity r WHERE r.totalArea IS NOT NULL")
    Double findMinArea();

    @Query("SELECT MAX(r.totalArea) FROM RealtyEntity r WHERE r.totalArea IS NOT NULL")
    Double findMaxArea();

}
