package com.estapar.parking.api;

import com.estapar.parking.api.dto.response.RevenueResponse;
import com.estapar.parking.application.service.RevenueService;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/revenue")
@Validated
public class RevenueController {

  private final RevenueService revenueService;

  public RevenueController(RevenueService revenueService) {
    this.revenueService = revenueService;
  }

  @GetMapping
  public ResponseEntity<RevenueResponse> getRevenue(
      @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @RequestParam(name = "sector", required = false)
          @Pattern(regexp = "^[A-Za-z0-9]*$", message = "sector invalido")
          String sector) {
    var response = revenueService.queryRevenue(date, sector);
    return ResponseEntity.ok(response);
  }
}
