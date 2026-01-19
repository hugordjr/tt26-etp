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
import org.springframework.test.util.ReflectionTestUtils;

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
    ReflectionTestUtils.setField(pricingService, "occupancyRateThresholdLow", 0.25);
    ReflectionTestUtils.setField(pricingService, "occupancyRateThresholdMedium", 0.5);
    ReflectionTestUtils.setField(pricingService, "occupancyRateThresholdHigh", 0.75);
    ReflectionTestUtils.setField(pricingService, "priceFactorLow", new BigDecimal("0.9"));
    ReflectionTestUtils.setField(pricingService, "priceFactorNormal", new BigDecimal("1.0"));
    ReflectionTestUtils.setField(pricingService, "priceFactorMedium", new BigDecimal("1.1"));
    ReflectionTestUtils.setField(pricingService, "priceFactorHigh", new BigDecimal("1.25"));
    ReflectionTestUtils.setField(pricingService, "decimalScale", 2);
    ReflectionTestUtils.setField(vehicleService, "pricingService", pricingService);
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

  @Test
  void deveNaoCobrarParaPermaneciadeMenorQue30Minutos() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(25);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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

    verify(revenueService).registerRevenue(eq(sector), any(), eq(new BigDecimal("0.00")));
    verify(spotRepository).save(spot);
    verify(vehicleRepository).save(vehicle);
    assertThat(spot.isOccupied()).isFalse();
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.EXITED);
  }

  @Test
  void deveNaoCobrarParaPermaneciadeExatamente30Minutos() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(30);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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

    verify(revenueService).registerRevenue(eq(sector), any(), eq(new BigDecimal("0.00")));
    verify(spotRepository).save(spot);
    verify(vehicleRepository).save(vehicle);
    assertThat(spot.isOccupied()).isFalse();
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.EXITED);
  }

  @Test
  void deveCobrarUmaHoraParaPermaneciade31Minutos() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(31);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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
  void deveCobrarUmaHoraParaPermaneciadeExatamenteUmaHora() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(1);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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
  void deveCobrarUmaHoraParaPermaneciade61Minutos() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(61);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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
  void deveCobrarUmaHoraParaPermaneciade90Minutos() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(90);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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
  void deveCobrarDuasHorasParaPermaneciade91Minutos() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(91);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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

    verify(revenueService).registerRevenue(eq(sector), any(), eq(new BigDecimal("20.00")));
    verify(spotRepository).save(spot);
    verify(vehicleRepository).save(vehicle);
    assertThat(spot.isOccupied()).isFalse();
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.EXITED);
  }

  @Test
  void deveCobrarTresHorasParaPermaneciade2HorasE31Minutos() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);
    spot.setOccupied(true);

    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2).minusMinutes(31);
    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setSpot(spot);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(entryTime);
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

    verify(revenueService).registerRevenue(eq(sector), any(), eq(new BigDecimal("30.00")));
    verify(spotRepository).save(spot);
    verify(vehicleRepository).save(vehicle);
    assertThat(spot.isOccupied()).isFalse();
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.EXITED);
  }

  @Test
  void deveRetornarErroQuandoEntryTimeNulo() {
    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ABC1234");
    request.setEntryTime(null);

    assertThatThrownBy(() -> vehicleService.handleWebhook(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("entry_time obrigatorio");
  }

  @Test
  void deveRetornarErroQuandoExitTimeNulo() {
    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.EXIT);
    request.setLicensePlate("ABC1234");
    request.setExitTime(null);

    assertThatThrownBy(() -> vehicleService.handleWebhook(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("exit_time obrigatorio");
  }

  @Test
  void deveRetornarErroQuandoVeiculoJaEstaNaGaragem() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Vehicle vehicle = new Vehicle();
    vehicle.setSector(sector);
    vehicle.setLicensePlate("ABC1234");
    vehicle.setEntryTime(OffsetDateTime.now().minusHours(1));
    vehicle.setStatus(VehicleStatus.PARKED);

    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.of(vehicle));

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ABC1234");
    request.setEntryTime(OffsetDateTime.now());

    assertThatThrownBy(() -> vehicleService.handleWebhook(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("veiculo ja esta na garagem");
  }

  @Test
  void deveAplicarDescontoDe10PorcentoQuandoLotacaoMenorQue25Porcento() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);

    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.empty());
    when(spotRepository.findFirstByOccupiedFalseOrderByIdAsc()).thenReturn(java.util.Optional.of(spot));
    when(vehicleRepository.countBySectorIdAndStatusNot(any(), any())).thenReturn(20L);
    when(spotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(vehicleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ABC1234");
    request.setEntryTime(OffsetDateTime.now());

    vehicleService.handleWebhook(request);

    verify(vehicleRepository).save(vehicleCaptor.capture());
    Vehicle saved = vehicleCaptor.getValue();
    assertThat(saved.getAdjustedPrice()).isEqualByComparingTo("9.00");
  }

  @Test
  void deveAplicarPrecoNormalQuandoLotacaoEntre25E50Porcento() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);

    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.empty());
    when(spotRepository.findFirstByOccupiedFalseOrderByIdAsc()).thenReturn(java.util.Optional.of(spot));
    when(vehicleRepository.countBySectorIdAndStatusNot(any(), any())).thenReturn(50L);
    when(spotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(vehicleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ABC1234");
    request.setEntryTime(OffsetDateTime.now());

    vehicleService.handleWebhook(request);

    verify(vehicleRepository).save(vehicleCaptor.capture());
    Vehicle saved = vehicleCaptor.getValue();
    assertThat(saved.getAdjustedPrice()).isEqualByComparingTo("10.00");
  }

  @Test
  void deveAplicarAumentoDe10PorcentoQuandoLotacaoEntre50E75Porcento() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);

    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.empty());
    when(spotRepository.findFirstByOccupiedFalseOrderByIdAsc()).thenReturn(java.util.Optional.of(spot));
    when(vehicleRepository.countBySectorIdAndStatusNot(any(), any())).thenReturn(75L);
    when(spotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(vehicleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ABC1234");
    request.setEntryTime(OffsetDateTime.now());

    vehicleService.handleWebhook(request);

    verify(vehicleRepository).save(vehicleCaptor.capture());
    Vehicle saved = vehicleCaptor.getValue();
    assertThat(saved.getAdjustedPrice()).isEqualByComparingTo("11.00");
  }

  @Test
  void deveAplicarAumentoDe25PorcentoQuandoLotacaoMaiorQue75Porcento() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setBasePrice(BigDecimal.TEN);
    sector.setMaxCapacity(100);

    Spot spot = new Spot();
    spot.setSector(sector);

    when(vehicleRepository.findFirstByLicensePlateAndStatusNot(any(), any()))
        .thenReturn(java.util.Optional.empty());
    when(spotRepository.findFirstByOccupiedFalseOrderByIdAsc()).thenReturn(java.util.Optional.of(spot));
    when(vehicleRepository.countBySectorIdAndStatusNot(any(), any())).thenReturn(99L);
    when(spotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(vehicleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    WebhookEventRequest request = new WebhookEventRequest();
    request.setEventType(EventType.ENTRY);
    request.setLicensePlate("ABC1234");
    request.setEntryTime(OffsetDateTime.now());

    vehicleService.handleWebhook(request);

    verify(vehicleRepository).save(vehicleCaptor.capture());
    Vehicle saved = vehicleCaptor.getValue();
    assertThat(saved.getAdjustedPrice()).isEqualByComparingTo("12.50");
  }
}
