package com.estapar.parking.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.estapar.parking.api.dto.request.WebhookEventRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RevenueE2ETest extends TestContainersConfig {

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
  void deveRetornarReceitaPorDataESetor() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();
    Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();

    OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
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

    ResponseEntity<RevenueResponse> response =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + exitTime.toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getAmount()).isGreaterThan(BigDecimal.ZERO);
    assertThat(response.getBody().getCurrency()).isEqualTo("BRL");
  }

  @Test
  void deveRetornarReceitaTotalSemFiltros() {
    Sector sectorA = sectorRepository.findByCode("A").orElseThrow();
    Sector sectorB = sectorRepository.findByCode("B").orElseThrow();

    for (int i = 0; i < 2; i++) {
      Spot spot = i == 0
          ? spotRepository.findAll().stream()
              .filter(s -> s.getSector().getCode().equals("A"))
              .findFirst()
              .orElseThrow()
          : spotRepository.findAll().stream()
              .filter(s -> s.getSector().getCode().equals("B"))
              .findFirst()
              .orElseThrow();

      OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
      OffsetDateTime exitTime = OffsetDateTime.now();

      WebhookEventRequest entryRequest = new WebhookEventRequest();
      entryRequest.setLicensePlate("PLATE" + i);
      entryRequest.setEventType(EventType.ENTRY);
      entryRequest.setEntryTime(entryTime);

      HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
      restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

      WebhookEventRequest exitRequest = new WebhookEventRequest();
      exitRequest.setLicensePlate("PLATE" + i);
      exitRequest.setEventType(EventType.EXIT);
      exitRequest.setExitTime(exitTime);

      HttpEntity<WebhookEventRequest> exitEntity = new HttpEntity<>(exitRequest);
      restTemplate.postForEntity(baseUrl + "/webhook", exitEntity, Void.class);
    }

    ResponseEntity<RevenueResponse> response =
        restTemplate.getForEntity(baseUrl + "/revenue", RevenueResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getAmount()).isGreaterThan(BigDecimal.ZERO);
    assertThat(response.getBody().getCurrency()).isEqualTo("BRL");
  }

  @Test
  void deveRetornarZeroQuandoNaoHaReceita() {
    ResponseEntity<RevenueResponse> response =
        restTemplate.getForEntity(
            baseUrl + "/revenue?date=" + OffsetDateTime.now().toLocalDate() + "&sector=A",
            RevenueResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.getBody().getCurrency()).isEqualTo("BRL");
  }
}
