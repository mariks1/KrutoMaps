package krutomaps.backend.repository;

import krutomaps.backend.entity.Realty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface RealtyRepository extends JpaRepository<Realty, Long>, JpaSpecificationExecutor<Realty> {

    @Query("SELECT MIN(r.lease_price) FROM Realty r WHERE r.lease_price IS NOT NULL")
    Double findMinPrice();

    @Query("SELECT MAX(r.lease_price) FROM Realty r WHERE r.lease_price IS NOT NULL")
    Double findMaxPrice();

    @Query("SELECT MIN(r.total_area) FROM Realty r WHERE r.total_area IS NOT NULL")
    Double findMinArea();

    @Query("SELECT MAX(r.total_area) FROM Realty r WHERE r.total_area IS NOT NULL")
    Double findMaxArea();

}
