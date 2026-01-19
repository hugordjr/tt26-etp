package com.estapar.parking.api;

import com.estapar.parking.api.dto.response.SpotResponse;
import com.estapar.parking.domain.entity.Spot;
import com.estapar.parking.domain.entity.Vehicle;
import com.estapar.parking.domain.enums.VehicleStatus;
import com.estapar.parking.infrastructure.repository.SpotRepository;
import com.estapar.parking.infrastructure.repository.VehicleRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spots")
public class SpotController {

  @Autowired private SpotRepository spotRepository;
  @Autowired private VehicleRepository vehicleRepository;

  @GetMapping
  public ResponseEntity<List<SpotResponse>> getAllSpots() {
    List<Spot> spots = spotRepository.findAll();

    List<SpotResponse> response =
        spots.stream()
            .map(
                spot -> {
                  SpotResponse.VehicleInfo vehicleInfo = null;
                  if (spot.isOccupied()) {
                    Optional<Vehicle> vehicle =
                        vehicleRepository.findBySpotIdAndStatusNot(spot.getId(), VehicleStatus.EXITED);
                    if (vehicle.isPresent()) {
                      Vehicle v = vehicle.get();
                      vehicleInfo =
                          new SpotResponse.VehicleInfo(
                              v.getLicensePlate(),
                              v.getStatus().name(),
                              v.getEntryTime(),
                              v.getParkedTime());
                    }
                  }

                  return new SpotResponse(
                      spot.getId(),
                      spot.getSector().getCode(),
                      spot.getLat(),
                      spot.getLng(),
                      spot.isOccupied(),
                      vehicleInfo);
                })
            .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }
}
