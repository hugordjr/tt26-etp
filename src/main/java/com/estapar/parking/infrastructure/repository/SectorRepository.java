package com.estapar.parking.infrastructure.repository;

import com.estapar.parking.domain.entity.Sector;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {
  Optional<Sector> findByCode(String code);
}
