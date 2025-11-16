package com.ufps.prueba.controllers;

import com.ufps.prueba.entities.DetallePedido;
import com.ufps.prueba.services.DetallePedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalles")
public class DetallePedidoController {

    @Autowired
    private DetallePedidoService detallePedidoService;

    @GetMapping
    public List<DetallePedido> listarDetalles() {
        return detallePedidoService.listarDetalles();
    }

    @GetMapping("/pedido/{pedidoId}")
    public List<DetallePedido> listarPorPedido(@PathVariable Long pedidoId) {
        return detallePedidoService.listarPorPedido(pedidoId);
    }

    @PostMapping
    public DetallePedido crearDetalle(@RequestBody DetallePedido detalle) {
        return detallePedidoService.guardarDetalle(detalle);
    }

    @DeleteMapping("/{id}")
    public void eliminarDetalle(@PathVariable Long id) {
        detallePedidoService.eliminarDetalle(id);
    }
}
