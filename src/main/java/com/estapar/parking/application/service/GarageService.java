package com.estapar.parking.application.service;

import com.estapar.parking.domain.entity.Garage;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.entity.Spot;
import com.estapar.parking.infrastructure.client.GarageClient;
import com.estapar.parking.infrastructure.client.dto.GarageConfigResponse;
import com.estapar.parking.infrastructure.repository.GarageRepository;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import com.estapar.parking.infrastructure.repository.SpotRepository;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GarageService {

  private static final Logger log = LoggerFactory.getLogger(GarageService.class);

  private final GarageClient garageClient;
  private final SectorRepository sectorRepository;
  private final SpotRepository spotRepository;
  private final GarageRepository garageRepository;

  public GarageService(
      GarageClient garageClient,
      SectorRepository sectorRepository,
      SpotRepository spotRepository,
      GarageRepository garageRepository) {
    this.garageClient = garageClient;
    this.sectorRepository = sectorRepository;
    this.spotRepository = spotRepository;
    this.garageRepository = garageRepository;
  }

  @Transactional
  public void bootstrapGarage() {
    if (sectorRepository.count() > 0 && spotRepository.count() > 0) {
      log.info("garage ja inicializada, ignorando bootstrap");
      return;
    }

    GarageConfigResponse response;
    try {
      response = garageClient.fetchConfig();
    } catch (org.springframework.web.client.ResourceAccessException e) {
      log.warn(
          "nao foi possivel conectar ao simulador. A aplicacao continuara sem dados iniciais. "
              + "Verifique: 1) Simulador rodando (docker ps | grep simulator), "
              + "2) Porta 8080 acessivel (curl http://localhost:8080/garage), "
              + "3) Docker network configurada corretamente. "
              + "Erro: {}",
          e.getMessage());
      return;
    } catch (Exception e) {
      log.warn(
          "erro ao buscar configuracao do simulador. Aplicacao continuara sem dados iniciais. "
              + "Erro: {}",
          e.getMessage());
      return;
    }

    if (response == null || response.getGarage() == null || response.getSpots() == null) {
      log.warn("resposta do simulador vazia, nao foi possivel inicializar garagem");
      return;
    }

    log.info("iniciando bootstrap da garagem");
    var sectorMap = new HashMap<String, Sector>();
    response
        .getGarage()
        .forEach(
            sectorConfig -> {
              Sector sector = new Sector();
              sector.setCode(sectorConfig.getSector());
              sector.setBasePrice(sectorConfig.getBasePrice());
              sector.setMaxCapacity(sectorConfig.getMaxCapacity());
              sectorRepository.save(sector);
              sectorMap.put(sector.getCode(), sector);
            });

    response
        .getSpots()
        .forEach(
            spotConfig -> {
              Sector sector = sectorMap.get(spotConfig.getSector());
              if (sector != null) {
                Spot spot = new Spot();
                spot.setSector(sector);
                spot.setLat(spotConfig.getLat());
                spot.setLng(spotConfig.getLng());
                spotRepository.save(spot);
              }
            });

    Garage garage = new Garage();
    garage.setName("Simulador");
    garageRepository.save(garage);
    log.info("bootstrap da garagem concluido");
  }
}
