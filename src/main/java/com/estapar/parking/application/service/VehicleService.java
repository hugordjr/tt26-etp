package com.estapar.parking.application.service;

import com.estapar.parking.api.dto.request.WebhookEventRequest;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.entity.Spot;
import com.estapar.parking.domain.entity.Vehicle;
import com.estapar.parking.domain.enums.EventType;
import com.estapar.parking.domain.enums.VehicleStatus;
import com.estapar.parking.domain.exception.BusinessException;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import com.estapar.parking.infrastructure.repository.SpotRepository;
import com.estapar.parking.infrastructure.repository.VehicleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

  private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

  private final VehicleRepository vehicleRepository;
  private final SpotRepository spotRepository;
  private final SectorRepository sectorRepository;
  private final PricingService pricingService;
  private final RevenueService revenueService;

  public VehicleService(
      VehicleRepository vehicleRepository,
      SpotRepository spotRepository,
      SectorRepository sectorRepository,
      PricingService pricingService,
      RevenueService revenueService) {
    this.vehicleRepository = vehicleRepository;
    this.spotRepository = spotRepository;
    this.sectorRepository = sectorRepository;
    this.pricingService = pricingService;
    this.revenueService = revenueService;
  }

  @Transactional
  public void handleWebhook(WebhookEventRequest request) {
    log.info(
        "processando evento {} para placa {}", request.getEventType(), request.getLicensePlate());

    if (request.getEventType() == EventType.ENTRY) {
      processEntry(request);
    } else if (request.getEventType() == EventType.PARKED) {
      processParked(request);
    } else if (request.getEventType() == EventType.EXIT) {
      processExit(request);
    }
  }

  private void processEntry(WebhookEventRequest request) {
    if (request.getEntryTime() == null) {
      throw new BusinessException("entry_time obrigatorio");
    }

    vehicleRepository
        .findFirstByLicensePlateAndStatusNot(request.getLicensePlate(), VehicleStatus.EXITED)
        .ifPresent(
            v -> {
              throw new BusinessException("veiculo ja esta na garagem");
            });

    Spot spot =
        spotRepository
            .findFirstByOccupiedFalseOrderByIdAsc()
            .orElseThrow(() -> new BusinessException("estacionamento lotado"));

    Sector sector = spot.getSector();
    long occupied = vehicleRepository.countBySectorIdAndStatusNot(sector.getId(), VehicleStatus.EXITED);
    double occupancyRate =
        sector.getMaxCapacity() == 0
            ? 1.0
            : (double) occupied / sector.getMaxCapacity();
    BigDecimal adjustedPrice = pricingService.applyDynamicPrice(sector.getBasePrice(), occupancyRate);

    Vehicle vehicle = new Vehicle();
    vehicle.setLicensePlate(request.getLicensePlate());
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setEntryTime(request.getEntryTime());
    vehicle.setAdjustedPrice(adjustedPrice);
    vehicle.setStatus(VehicleStatus.ENTRY);

    spot.setOccupied(true);
    spotRepository.save(spot);
    vehicleRepository.save(vehicle);

    log.info(
        "entrada registrada para placa {} no setor {} com preco ajustado {}",
        vehicle.getLicensePlate(),
        sector.getCode(),
        adjustedPrice);
  }

  private void processParked(WebhookEventRequest request) {
    Vehicle vehicle = getActiveVehicle(request.getLicensePlate());
    vehicle.setStatus(VehicleStatus.PARKED);
    vehicle.setParkedTime(OffsetDateTime.now());
    vehicleRepository.save(vehicle);
    log.info("veiculo {} marcado como PARKED", vehicle.getLicensePlate());
  }

  private void processExit(WebhookEventRequest request) {
    if (request.getExitTime() == null) {
      throw new BusinessException("exit_time obrigatorio");
    }

    Vehicle vehicle = getActiveVehicle(request.getLicensePlate());
    OffsetDateTime exitTime = request.getExitTime();

    long minutes = Duration.between(vehicle.getEntryTime(), exitTime).toMinutes();
    long minutesOverFree = Math.max(0, minutes - 30);
    long hoursCharged = minutesOverFree == 0 ? 0 : (long) Math.ceil(minutesOverFree / 60.0);

    BigDecimal amount =
        vehicle
            .getAdjustedPrice()
            .multiply(BigDecimal.valueOf(hoursCharged))
            .setScale(2, RoundingMode.HALF_UP);

    vehicle.setExitTime(exitTime);
    vehicle.setStatus(VehicleStatus.EXITED);

    Spot spot = vehicle.getSpot();
    spot.setOccupied(false);
    spotRepository.save(spot);
    vehicleRepository.save(vehicle);

    revenueService.registerRevenue(vehicle.getSector(), exitTime.toLocalDate(), amount);

    log.info(
        "saida registrada para placa {} no setor {} valor {}",
        vehicle.getLicensePlate(),
        vehicle.getSector().getCode(),
        amount);
  }

  private Vehicle getActiveVehicle(String licensePlate) {
    return vehicleRepository
        .findFirstByLicensePlateAndStatusNot(licensePlate, VehicleStatus.EXITED)
        .orElseThrow(() -> new BusinessException("veiculo nao encontrado na garagem"));
  }
}
