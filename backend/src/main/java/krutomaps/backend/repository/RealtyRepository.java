package krutomaps.backend.repository;

import krutomaps.backend.entity.Realty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RealtyRepository extends JpaRepository<Realty, Long>, JpaSpecificationExecutor<Realty> {

    @Query("SELECT MIN(r.leasePrice) FROM Realty r WHERE r.leasePrice IS NOT NULL")
    Double findMinPrice();

    @Query("SELECT MAX(r.leasePrice) FROM Realty r WHERE r.leasePrice IS NOT NULL")
    Double findMaxPrice();

    @Query("SELECT MIN(r.totalArea) FROM Realty r WHERE r.totalArea IS NOT NULL")
    Double findMinArea();

    @Query("SELECT MAX(r.leasePrice) FROM Realty r WHERE r.totalArea IS NOT NULL")
    Double findMaxArea();

}
