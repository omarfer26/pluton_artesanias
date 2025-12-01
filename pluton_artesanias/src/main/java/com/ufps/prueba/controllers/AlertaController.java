package com.ufps.prueba.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufps.prueba.services.AlertaInventarioService;

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    @Autowired
    private AlertaInventarioService alertaService;

    @GetMapping("/api/alertas/pendientes")
    public List<String> obtenerAlertas() {
        return alertaService.obtenerAlertasRecientes();
    }
}
