package com.estapar.parking.infrastructure.repository;

import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.entity.Spot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotRepository extends JpaRepository<Spot, Long> {
  Optional<Spot> findFirstByOccupiedFalseOrderByIdAsc();

  long countBySectorAndOccupiedTrue(Sector sector);
}
