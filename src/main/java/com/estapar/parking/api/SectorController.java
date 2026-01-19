package com.estapar.parking.api;

import com.estapar.parking.api.dto.response.SectorResponse;
import com.estapar.parking.api.dto.response.SectorsSummaryResponse;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.enums.VehicleStatus;
import com.estapar.parking.infrastructure.repository.RevenueRepository;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import com.estapar.parking.infrastructure.repository.VehicleRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sectors")
public class SectorController {

  @Autowired private SectorRepository sectorRepository;
  @Autowired private RevenueRepository revenueRepository;
  @Autowired private VehicleRepository vehicleRepository;

  @GetMapping
  public ResponseEntity<SectorsSummaryResponse> getAllSectors() {
    List<Sector> sectors = sectorRepository.findAll();

    List<SectorResponse> sectorsResponse =
        sectors.stream()
            .map(
                sector -> {
                  BigDecimal totalRevenue =
                      revenueRepository.sumByFilters(sector.getId(), null);
                  Long vehicleCount =
                      vehicleRepository.countBySectorIdAndStatusNot(
                          sector.getId(), VehicleStatus.EXITED);
                  Double occupancyRate =
                      sector.getMaxCapacity() == 0 || sector.getMaxCapacity() == null
                          ? 0.0
                          : (double) vehicleCount / sector.getMaxCapacity();

                  return new SectorResponse(
                      sector.getCode(),
                      sector.getBasePrice(),
                      sector.getMaxCapacity(),
                      totalRevenue,
                      vehicleCount,
                      occupancyRate);
                })
            .collect(Collectors.toList());

    BigDecimal totalRevenue =
        sectorsResponse.stream()
            .map(SectorResponse::getTotalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Long totalVehicles =
        sectorsResponse.stream()
            .map(SectorResponse::getVehicleCount)
            .mapToLong(Long::longValue)
            .sum();

    Integer totalCapacity =
        sectors.stream().mapToInt(Sector::getMaxCapacity).sum();

    Double overallOccupancyRate =
        totalCapacity == 0 ? 0.0 : (double) totalVehicles / totalCapacity;

    SectorsSummaryResponse summary =
        new SectorsSummaryResponse(
            sectorsResponse, totalRevenue, totalVehicles, totalCapacity, overallOccupancyRate);

    return ResponseEntity.ok(summary);
  }
}
