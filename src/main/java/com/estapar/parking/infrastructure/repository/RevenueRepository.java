package com.estapar.parking.infrastructure.repository;

import com.estapar.parking.domain.entity.Revenue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RevenueRepository extends JpaRepository<Revenue, Long> {

  @Query(
      "select coalesce(sum(r.amount), 0) from Revenue r "
          + "where (:sectorId is null or r.sector.id = :sectorId) "
          + "and (:date is null or r.date = :date)")
  BigDecimal sumByFilters(@Param("sectorId") Long sectorId, @Param("date") LocalDate date);

  Optional<Revenue> findFirstByOrderByTimestampDesc();
}
