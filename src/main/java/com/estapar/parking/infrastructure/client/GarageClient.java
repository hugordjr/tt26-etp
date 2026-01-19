package com.estapar.parking.infrastructure.client;

import com.estapar.parking.infrastructure.client.dto.GarageConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GarageClient {

  private static final Logger log = LoggerFactory.getLogger(GarageClient.class);

  @Autowired private RestTemplate restTemplate;
  @Value("${simulator.base-url}") private String baseUrl;

  public GarageConfigResponse fetchConfig() {
    var url = baseUrl + "/garage";
    log.info("buscando configuracao da garagem em {}", url);
    try {
      return restTemplate.getForObject(url, GarageConfigResponse.class);
    } catch (org.springframework.web.client.ResourceAccessException e) {
      log.error(
          "erro de conexao com simulador em {}. Verifique se o simulador esta rodando e acessivel. Erro: {}",
          url,
          e.getMessage());
      throw e;
    } catch (org.springframework.web.client.HttpClientErrorException e) {
      log.error(
          "erro HTTP ao buscar configuracao do simulador. Status: {}, Response: {}",
          e.getStatusCode(),
          e.getResponseBodyAsString());
      throw e;
    }
  }

  public boolean isAvailable() {
    try {
      var url = baseUrl + "/garage";
      var response = restTemplate.getForEntity(url, Object.class);
      return response.getStatusCode().is2xxSuccessful();
    } catch (org.springframework.web.client.ResourceAccessException e) {
      log.debug("simulador nao acessivel em {}: {}", baseUrl, e.getMessage());
      return false;
    } catch (Exception e) {
      log.debug("erro ao verificar disponibilidade do simulador: {}", e.getMessage());
      return false;
    }
  }
}
