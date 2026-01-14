package com.estapar.parking.infrastructure.client;

import com.estapar.parking.infrastructure.client.dto.GarageConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GarageClient {

  private static final Logger log = LoggerFactory.getLogger(GarageClient.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public GarageClient(RestTemplate restTemplate, @Value("${simulator.base-url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public GarageConfigResponse fetchConfig() {
    var url = baseUrl + "/garage";
    log.info("buscando configuracao da garagem em {}", url);
    return restTemplate.getForObject(url, GarageConfigResponse.class);
  }
}
