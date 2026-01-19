package com.estapar.parking.infrastructure.repository;

import com.estapar.parking.domain.entity.Vehicle;
import com.estapar.parking.domain.enums.VehicleStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
  Optional<Vehicle> findFirstByLicensePlateAndStatusNot(String licensePlate, VehicleStatus status);

  long countBySectorIdAndStatusNot(Long sectorId, VehicleStatus status);

  Optional<Vehicle> findBySpotIdAndStatusNot(Long spotId, VehicleStatus status);
}
