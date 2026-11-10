package com.trampolim.api.controller;

import com.trampolim.api.dto.MetricasDTO;
import com.trampolim.api.service.MetricasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metricas")
public class MetricasController {

    private final MetricasService metricasService;

    public MetricasController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    // Rota PÚBLICA: alimenta o painel de métricas
    @GetMapping
    public ResponseEntity<MetricasDTO> buscarMetricas() {
        return ResponseEntity.ok(metricasService.calcular());
    }
}
