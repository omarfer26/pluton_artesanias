package com.ufps.prueba.controllers;

import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPedido(@PathVariable Long id) {
        return pedidoService.obtenerPedidoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Pedido crearPedido(@RequestBody Pedido pedido) {
        return pedidoService.guardarPedido(pedido);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> actualizarPedido(@PathVariable Long id, @RequestBody Pedido pedido) {
        return pedidoService.obtenerPedidoPorId(id)
                .map(p -> {
                    pedido.setId(id);
                    return ResponseEntity.ok(pedidoService.guardarPedido(pedido));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/pedidos/activos")
    public List<Pedido> listarPedidosActivos() {
        return pedidoService.obtenerPedidosActivos();
    }

    @DeleteMapping("/{id}")
    public void eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
    }
    
}
