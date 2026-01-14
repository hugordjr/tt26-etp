package com.estapar.parking.config;

import com.estapar.parking.application.service.GarageService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapRunner implements ApplicationRunner {

  private final GarageService garageService;

  public BootstrapRunner(GarageService garageService) {
    this.garageService = garageService;
  }

  @Override
  public void run(ApplicationArguments args) {
    garageService.bootstrapGarage();
  }
}
