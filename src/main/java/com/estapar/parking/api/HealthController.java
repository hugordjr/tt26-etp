package com.estapar.parking.api;

import com.estapar.parking.api.dto.response.HealthResponse;
import com.estapar.parking.application.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Endpoint de health check da aplicacao")
public class HealthController {

  private final HealthService healthService;

  public HealthController(HealthService healthService) {
    this.healthService = healthService;
  }

  @GetMapping
  @Operation(
      summary = "Verifica status da aplicacao",
      description =
          "Retorna o status geral da aplicacao, incluindo status do banco de dados e do simulador")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status da aplicacao",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = HealthResponse.class))
            })
      })
  public ResponseEntity<HealthResponse> health() {
    return ResponseEntity.ok(healthService.checkHealth());
  }
}
