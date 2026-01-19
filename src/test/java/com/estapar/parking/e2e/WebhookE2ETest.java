package com.estapar.parking.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.estapar.parking.api.dto.request.WebhookEventRequest;
import com.estapar.parking.api.dto.response.ErrorResponse;
import com.estapar.parking.api.dto.response.RevenueResponse;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.entity.Spot;
import com.estapar.parking.domain.entity.Vehicle;
import com.estapar.parking.domain.enums.EventType;
import com.estapar.parking.domain.enums.VehicleStatus;
import com.estapar.parking.e2e.config.DatabaseCleanup;
import com.estapar.parking.e2e.config.TestContainersConfig;
import com.estapar.parking.e2e.config.TestDataSetup;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import com.estapar.parking.infrastructure.repository.SpotRepository;
import com.estapar.parking.infrastructure.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebhookE2ETest extends TestContainersConfig {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private DatabaseCleanup databaseCleanup;

  @Autowired private TestDataSetup testDataSetup;

  @Autowired private SectorRepository sectorRepository;

  @Autowired private SpotRepository spotRepository;

  @Autowired private VehicleRepository vehicleRepository;

  private String baseUrl;

  @BeforeEach
  void setup() {
    databaseCleanup.cleanDatabase();
    testDataSetup.setupTestData();
    baseUrl = "http://localhost:" + port;
  }

  @Test
  void deveRetornarErroQuandoEntryTimeNulo() {
    WebhookEventRequest request = new WebhookEventRequest();
    request.setLicensePlate("ABC1234");
    request.setEventType(EventType.ENTRY);
    request.setEntryTime(null);

    HttpEntity<WebhookEventRequest> entity = new HttpEntity<>(request);
    ResponseEntity<ErrorResponse> response =
        restTemplate.exchange(
            baseUrl + "/webhook", HttpMethod.POST, entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).contains("entry_time");
  }

  @Test
  void deveRetornarErroQuandoExitTimeNulo() {
    WebhookEventRequest request = new WebhookEventRequest();
    request.setLicensePlate("ABC1234");
    request.setEventType(EventType.EXIT);
    request.setExitTime(null);

    HttpEntity<WebhookEventRequest> entity = new HttpEntity<>(request);
    ResponseEntity<ErrorResponse> response =
        restTemplate.exchange(
            baseUrl + "/webhook", HttpMethod.POST, entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).contains("exit_time");
  }

  @Test
  void deveRetornarErroQuandoVeiculoJaEstaNaGaragem() {
    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest duplicateRequest = new WebhookEventRequest();
    duplicateRequest.setLicensePlate("ABC1234");
    duplicateRequest.setEventType(EventType.ENTRY);
    duplicateRequest.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> duplicateEntity = new HttpEntity<>(duplicateRequest);
    ResponseEntity<ErrorResponse> response =
        restTemplate.exchange(
            baseUrl + "/webhook", HttpMethod.POST, duplicateEntity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).contains("ja esta na garagem");
  }

  @Test
  void deveRetornarErroQuandoVeiculoNaoEstaNaGaragemParaExit() {
    WebhookEventRequest request = new WebhookEventRequest();
    request.setLicensePlate("ABC1234");
    request.setEventType(EventType.EXIT);
    request.setExitTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entity = new HttpEntity<>(request);
    ResponseEntity<ErrorResponse> response =
        restTemplate.exchange(
            baseUrl + "/webhook", HttpMethod.POST, entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).contains("nao encontrado na garagem");
  }

  @Test
  void deveRegistrarEntradaComSucesso() {
    WebhookEventRequest request = new WebhookEventRequest();
    request.setLicensePlate("ABC1234");
    request.setEventType(EventType.ENTRY);
    request.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entity = new HttpEntity<>(request);
    ResponseEntity<Void> response =
        restTemplate.postForEntity(baseUrl + "/webhook", entity, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void deveRegistrarSaidaComSucesso() {
    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(OffsetDateTime.now().minusHours(1));

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    ResponseEntity<Void> response =
        restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void deveRegistrarEntradaESaidaCompleto() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    ResponseEntity<Void> entryResponse =
        restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);
    assertThat(entryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    ResponseEntity<Void> exitResponse =
        restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);
    assertThat(exitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);
    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isGreaterThan(BigDecimal.ZERO);
  }

  @Test
  void deveNaoCobrarParaPermaneciadeMenorQue30Minutos() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(25);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void deveNaoCobrarParaPermaneciadeExatamente30Minutos() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(30);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void deveCobrarUmaHoraParaPermaneciade31Minutos() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(31);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
  }

  @Test
  void deveCobrarUmaHoraParaPermaneciade60Minutos() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(60);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
  }

  @Test
  void deveCobrarUmaHoraParaPermaneciade90Minutos() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(90);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
  }

  @Test
  void deveCobrarDuasHorasParaPermaneciade91Minutos() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(91);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
  }

  @Test
  void deveCobrarTresHorasParaPermaneciade151Minutos() {
    OffsetDateTime entryTime = OffsetDateTime.now().minusMinutes(151);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("ABC1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
  }

  @Test
  void deveAplicarDesconto10PorcentoQuandoLotacaoMenorQue25Porcento() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();
    Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();

    for (int i = 0; i < 2; i++) {
      Vehicle vehicle = new Vehicle();
      vehicle.setLicensePlate("PLATE" + i);
      vehicle.setSector(sector);
      vehicle.setSpot(spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow());
      vehicle.setEntryTime(OffsetDateTime.now().minusHours(1));
      vehicle.setAdjustedPrice(new BigDecimal("10.00"));
      vehicle.setStatus(VehicleStatus.PARKED);
      vehicleRepository.save(vehicle);
      vehicle.getSpot().setOccupied(true);
      spotRepository.save(vehicle.getSpot());
    }

    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("TEST1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("TEST1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("18.00"));
  }

  @Test
  void deveAplicarPrecoNormalQuandoLotacaoEntre25E50Porcento() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();

    for (int i = 0; i < 3; i++) {
      Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();
      Vehicle vehicle = new Vehicle();
      vehicle.setLicensePlate("PLATE" + i);
      vehicle.setSector(sector);
      vehicle.setSpot(spot);
      vehicle.setEntryTime(OffsetDateTime.now().minusHours(1));
      vehicle.setAdjustedPrice(new BigDecimal("10.00"));
      vehicle.setStatus(VehicleStatus.PARKED);
      vehicleRepository.save(vehicle);
      spot.setOccupied(true);
      spotRepository.save(spot);
    }

    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("TEST1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("TEST1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
  }

  @Test
  void deveAplicarAumento10PorcentoQuandoLotacaoEntre50E75Porcento() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();

    for (int i = 0; i < 6; i++) {
      Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();
      Vehicle vehicle = new Vehicle();
      vehicle.setLicensePlate("PLATE" + i);
      vehicle.setSector(sector);
      vehicle.setSpot(spot);
      vehicle.setEntryTime(OffsetDateTime.now().minusHours(1));
      vehicle.setAdjustedPrice(new BigDecimal("10.00"));
      vehicle.setStatus(VehicleStatus.PARKED);
      vehicleRepository.save(vehicle);
      spot.setOccupied(true);
      spotRepository.save(spot);
    }

    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("TEST1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("TEST1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("22.00"));
  }

  @Test
  void deveAplicarAumento25PorcentoQuandoLotacaoMaiorQue75Porcento() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();

    for (int i = 0; i < 8; i++) {
      Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();
      Vehicle vehicle = new Vehicle();
      vehicle.setLicensePlate("PLATE" + i);
      vehicle.setSector(sector);
      vehicle.setSpot(spot);
      vehicle.setEntryTime(OffsetDateTime.now().minusHours(1));
      vehicle.setAdjustedPrice(new BigDecimal("10.00"));
      vehicle.setStatus(VehicleStatus.PARKED);
      vehicleRepository.save(vehicle);
      spot.setOccupied(true);
      spotRepository.save(spot);
    }

    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
    OffsetDateTime exitTime = OffsetDateTime.now();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("TEST1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(entryTime);

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("TEST1234");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(exitTime);

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    ResponseEntity<RevenueResponse> revenueResponse =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(revenueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(revenueResponse.getBody()).isNotNull();
    assertThat(revenueResponse.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
  }

  @Test
  void deveBloquearEntradaQuandoEstacionamento100PorcentoLotado() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();

    for (int i = 0; i < 10; i++) {
      Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();
      Vehicle vehicle = new Vehicle();
      vehicle.setLicensePlate("PLATE" + i);
      vehicle.setSector(sector);
      vehicle.setSpot(spot);
      vehicle.setEntryTime(OffsetDateTime.now().minusHours(1));
      vehicle.setAdjustedPrice(new BigDecimal("10.00"));
      vehicle.setStatus(VehicleStatus.PARKED);
      vehicleRepository.save(vehicle);
      spot.setOccupied(true);
      spotRepository.save(spot);
    }

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("TEST1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    ResponseEntity<ErrorResponse> response =
        restTemplate.exchange(
            baseUrl + "/webhook", HttpMethod.POST, entryEntity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).contains("lotado");
  }

  @Test
  void devePermitirEntradaAposSaidaQuandoEstavaLotado() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();

    for (int i = 0; i < 10; i++) {
      Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();
      Vehicle vehicle = new Vehicle();
      vehicle.setLicensePlate("PLATE" + i);
      vehicle.setSector(sector);
      vehicle.setSpot(spot);
      vehicle.setEntryTime(OffsetDateTime.now().minusHours(1));
      vehicle.setAdjustedPrice(new BigDecimal("10.00"));
      vehicle.setStatus(VehicleStatus.PARKED);
      vehicleRepository.save(vehicle);
      spot.setOccupied(true);
      spotRepository.save(spot);
    }

    WebhookEventRequest exitRequest = new WebhookEventRequest();
    exitRequest.setLicensePlate("PLATE0");
    exitRequest.setEventType(EventType.EXIT);
    exitRequest.setExitTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("TEST1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    ResponseEntity<Void> response =
        restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void deveSincronizarEstadoDasVagas() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();
    Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();

    spot.setOccupied(true);
    spotRepository.save(spot);

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("TEST1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    ResponseEntity<Void> response =
        restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
