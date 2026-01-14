package com.estapar.parking.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

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
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private SpotRepository spotRepository;
  @Mock private SectorRepository sectorRepository;
  @Mock private RevenueService revenueService;

  private PricingService pricingService;

  @InjectMocks private VehicleService vehicleService;

  @Captor private ArgumentCaptor<Vehicle> vehicleCaptor;

  @BeforeEach
  void setup() {
    pricingService = new PricingService();
    vehicleService =
        new VehicleService(
            vehicleRepository, spotRepository, sectorRepository, pricingService, revenueService);
  }

  @Test
  void deveRegistrarEntradaQuandoHaVaga() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);

    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.empty());
    when(spotRepository.findFirstByOccupiedFalseOrderByIdAsc()).thenReturn(java.util.Optional.of(spot));
    when(vehicleRepository.countBySectorIdAndStatusNot(any(), any())).thenReturn(0L);
    when(spotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(vehicleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ABC1234");
    request.setEntryTime(OffsetDateTime.now());

    vehicleService.handleWebhook(request);

    verify(vehicleRepository).save(vehicleCaptor.capture());
    Vehicle saved = vehicleCaptor.getValue();
    verify(spotRepository).save(spot);
    assertThat(saved.getStatus()).isEqualTo(VehicleStatus.ENTRY);
    assertThat(spot.isOccupied()).isTrue();
  }

  @Test
  void deveCalcularSaidaELiberarVaga() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(OffsetDateTime.now().minusMinutes(90));
    vehicle.setAdjustedPrice(BigDecimal.TEN);
    vehicle.setStatus(VehicleStatus.PARKED);

    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.of(vehicle));
    when(spotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(vehicleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.EXIT);
    request.setLicensePlate("ABC1234");
    request.setExitTime(OffsetDateTime.now());

    vehicleService.handleWebhook(request);

    verify(revenueService).registerRevenue(eq(sector), any(), eq(new BigDecimal("10.00")));
    verify(spotRepository).save(spot);
    verify(vehicleRepository).save(vehicle);
    assertThat(spot.isOccupied()).isFalse();
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.EXITED);
  }

  @Test
  void deveRetornarErroQuandoEntradaSemVaga() {
    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.empty());
    when(spotRepository.findFirstByOccupiedFalseOrderByIdAsc()).thenReturn(java.util.Optional.empty());

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ZZZ9999");
    request.setEntryTime(OffsetDateTime.now());

    assertThatThrownBy(() -> vehicleService.handleWebhook(request))
        .isInstanceOf(BusinessException.class);
  }
}
