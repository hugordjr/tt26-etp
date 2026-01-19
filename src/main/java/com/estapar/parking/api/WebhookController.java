package com.estapar.parking.api;

import com.estapar.parking.api.dto.request.WebhookEventRequest;
import com.estapar.parking.application.service.VehicleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@Validated
public class WebhookController {

  private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

  @Autowired private VehicleService vehicleService;

  @PostMapping
  public ResponseEntity<Void> handleEvent(@Valid @RequestBody WebhookEventRequest request) {
    log.info("webhook recebido no controller: eventType={}, licensePlate={}, entryTime={}, exitTime={}", 
        request.getEventType(), request.getLicensePlate(), request.getEntryTime(), request.getExitTime());
    vehicleService.handleWebhook(request);
    ResponseEntity<Void> response = ResponseEntity.ok().build();
    log.info("webhook processado com sucesso: eventType={}, licensePlate={}", 
        request.getEventType(), request.getLicensePlate());
    return response;
  }
}
