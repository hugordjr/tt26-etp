package com.estapar.parking.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.estapar.parking.api.dto.request.WebhookEventRequest;
import com.estapar.parking.api.dto.response.SpotResponse;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.entity.Spot;
import com.estapar.parking.domain.enums.EventType;
import com.estapar.parking.e2e.config.DatabaseCleanup;
import com.estapar.parking.e2e.config.TestContainersConfig;
import com.estapar.parking.e2e.config.TestDataSetup;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import com.estapar.parking.infrastructure.repository.SpotRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SpotE2ETest extends TestContainersConfig {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private DatabaseCleanup databaseCleanup;

  @Autowired private TestDataSetup testDataSetup;

  @Autowired private SectorRepository sectorRepository;

  @Autowired private SpotRepository spotRepository;

  private String baseUrl;

  @BeforeEach
  void setup() {
    databaseCleanup.cleanDatabase();
    testDataSetup.setupTestData();
    baseUrl = "http://localhost:" + port;
  }

  @Test
  void deveRetornarInformacoesDasVagas() {
    ResponseEntity<List<SpotResponse>> response =
        restTemplate.exchange(
            baseUrl + "/spots",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpotResponse>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isNotEmpty();
    assertThat(response.getBody().size()).isEqualTo(30);
  }

  @Test
  void deveMostrarVeiculoQuandoVagaOcupada() {
    WebhookEventRequest entryRequest = new WebhookEventRequest();
    entryRequest.setLicensePlate("ABC1234");
    entryRequest.setEventType(EventType.ENTRY);
    entryRequest.setEntryTime(OffsetDateTime.now());

    HttpEntity<WebhookEventRequest> entryEntity = new HttpEntity<>(entryRequest);
    restTemplate.postForEntity(baseUrl + "/webhook", entryEntity, Void.class);

    ResponseEntity<List<SpotResponse>> response =
        restTemplate.exchange(
            baseUrl + "/spots",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpotResponse>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    SpotResponse occupiedSpot =
        response.getBody().stream()
            .filter(SpotResponse::getOccupied)
            .findFirst()
            .orElse(null);

    assertThat(occupiedSpot).isNotNull();
    assertThat(occupiedSpot.getVehicle()).isNotNull();
    assertThat(occupiedSpot.getVehicle().getLicensePlate()).isEqualTo("ABC1234");
    assertThat(occupiedSpot.getVehicle().getStatus()).isEqualTo("ENTRY");
  }
}
