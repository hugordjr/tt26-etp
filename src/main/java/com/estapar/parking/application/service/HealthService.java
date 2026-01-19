package com.estapar.parking.application.service;

import com.estapar.parking.api.dto.response.HealthResponse;
import com.estapar.parking.infrastructure.client.GarageClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

  private static final Logger log = LoggerFactory.getLogger(HealthService.class);

  @PersistenceContext private EntityManager entityManager;

  @Autowired private GarageClient garageClient;

  public HealthResponse checkHealth() {
    var applicationStatus = checkApplication();
    var databaseStatus = checkDatabase();
    var simulatorStatus = checkSimulator();

    String overallStatus = "UP";
    if (!"UP".equals(applicationStatus.getStatus())
        || !"UP".equals(databaseStatus.getStatus())
        || !"UP".equals(simulatorStatus.getStatus())) {
      overallStatus = "DOWN";
    }

    return new HealthResponse(
        overallStatus,
        OffsetDateTime.now(),
        applicationStatus,
        databaseStatus,
        simulatorStatus);
  }

  private HealthResponse.ComponentStatus checkApplication() {
    return new HealthResponse.ComponentStatus("UP", "Aplicacao em execucao");
  }

  private HealthResponse.ComponentStatus checkDatabase() {
    try {
      entityManager.createNativeQuery("SELECT 1").getSingleResult();
      return new HealthResponse.ComponentStatus("UP", "Conexao com banco de dados OK");
    } catch (Exception e) {
      log.warn("erro ao verificar conexao com banco de dados: {}", e.getMessage());
      return new HealthResponse.ComponentStatus("DOWN", "Erro ao conectar: " + e.getMessage());
    }
  }

  private HealthResponse.ComponentStatus checkSimulator() {
    if (garageClient.isAvailable()) {
      return new HealthResponse.ComponentStatus("UP", "Simulador acessivel");
    } else {
      return new HealthResponse.ComponentStatus("DOWN", "Simulador nao acessivel");
    }
  }
}
