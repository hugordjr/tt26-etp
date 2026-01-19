package com.estapar.parking.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.estapar.parking.api.dto.request.WebhookEventRequest;
import com.estapar.parking.api.dto.response.SectorResponse;
import com.estapar.parking.api.dto.response.SectorsSummaryResponse;
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
class SectorE2ETest extends TestContainersConfig {

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
  void deveRetornarInformacoesDosSetores() {
    ResponseEntity<SectorsSummaryResponse> response =
        restTemplate.getForEntity(baseUrl + "/sectors", SectorsSummaryResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSectors()).isNotEmpty();
    assertThat(response.getBody().getTotalCapacity()).isEqualTo(30);
    assertThat(response.getBody().getTotalVehicles()).isEqualTo(0);
  }

  @Test
  void deveCalcularTaxaOcupacaoCorretamente() {
    Sector sector = sectorRepository.findByCode("A").orElseThrow();
    Spot spot = spotRepository.findFirstByOccupiedFalseOrderByIdAsc().orElseThrow();

    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    ResponseEntity<SectorsSummaryResponse> response =
        restTemplate.getForEntity(baseUrl + "/sectors", SectorsSummaryResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    SectorResponse sectorResponse =
        response.getBody().getSectors().stream()
            .filter(s -> s.getCode().equals("A"))
            .findFirst()
            .orElseThrow();
    assertThat(sectorResponse.getVehicleCount()).isEqualTo(1);
    assertThat(sectorResponse.getOccupancyRate()).isEqualTo(0.1);
    assertThat(response.getBody().getOverallOccupancyRate()).isEqualTo(1.0 / 30.0);
  }
}
