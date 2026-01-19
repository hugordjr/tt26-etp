package com.estapar.parking.config;

import com.estapar.parking.application.service.GarageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapRunner implements ApplicationRunner {

  @Autowired private GarageService garageService;

  @Override
  public void run(ApplicationArguments args) {
    garageService.bootstrapGarage();
  }
}
