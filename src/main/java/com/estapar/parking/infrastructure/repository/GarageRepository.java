package com.estapar.parking.infrastructure.repository;

import com.estapar.parking.domain.entity.Garage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GarageRepository extends JpaRepository<Garage, Long> {}
