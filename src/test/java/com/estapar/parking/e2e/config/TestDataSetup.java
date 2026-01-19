package com.estapar.parking.e2e.config;

import com.estapar.parking.domain.entity.Garage;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.entity.Spot;
import com.estapar.parking.infrastructure.repository.GarageRepository;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import com.estapar.parking.infrastructure.repository.SpotRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDataSetup {

  @Autowired private SectorRepository sectorRepository;
  @Autowired private SpotRepository spotRepository;
  @Autowired private GarageRepository garageRepository;

  @Transactional
  public void setupTestData() {
    Sector sectorA = new Sector();
    sectorA.setCode("A");
    sectorA.setBasePrice(new BigDecimal("10.00"));
    sectorA.setMaxCapacity(10);
    sectorRepository.save(sectorA);

    Sector sectorB = new Sector();
    sectorB.setCode("B");
    sectorB.setBasePrice(new BigDecimal("20.00"));
    sectorB.setMaxCapacity(20);
    sectorRepository.save(sectorB);

    for (int i = 1; i <= 10; i++) {
      Spot spot = new Spot();
      spot.setSector(sectorA);
      spot.setLat(-23.561684 + (i * 0.0001));
      spot.setLng(-46.655981 + (i * 0.0001));
      spot.setOccupied(false);
      spotRepository.save(spot);
    }

    for (int i = 1; i <= 20; i++) {
      Spot spot = new Spot();
      spot.setSector(sectorB);
      spot.setLat(-23.561484 + (i * 0.0001));
      spot.setLng(-46.655781 + (i * 0.0001));
      spot.setOccupied(false);
      spotRepository.save(spot);
    }

    Garage garage = new Garage();
    garage.setName("Teste");
    garageRepository.save(garage);
  }
}
