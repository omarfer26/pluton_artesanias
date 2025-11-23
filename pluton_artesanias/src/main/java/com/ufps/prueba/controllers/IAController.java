package com.ufps.prueba.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufps.prueba.dto.IAMensajeDTO;
import com.ufps.prueba.dto.PedidoDTO;
import com.ufps.prueba.entities.Cliente;
import com.ufps.prueba.entities.DetallePedido;
import com.ufps.prueba.entities.LogSistema;
import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.repositories.LogSistemaRepository;
import com.ufps.prueba.services.ClienteService;
import com.ufps.prueba.services.InventarioService;
import com.ufps.prueba.services.PedidoService;
import com.ufps.prueba.services.ProductoService;

@RestController
@RequestMapping("/ia")
public class IAController {

    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private InventarioService inventarioService;
    
    @Autowired
    private LogSistemaRepository logSistemaRepository;

    @PostMapping("/mensaje")
    public ResponseEntity<?> recibirMensaje(@RequestBody IAMensajeDTO mensaje) {
        try {
            // Obtener las entidades reales desde la base de datos
            Cliente clienteEntidad = clienteService.obtenerClientePorId(mensaje.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            Producto productoEntidad = productoService.obtenerProductoPorId(mensaje.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            int cantidad = mensaje.getCantidad();

            // Validar stock
            if (!inventarioService.puedeFabricar(productoEntidad.getId(), cantidad)) {
                return ResponseEntity.badRequest().body("No hay suficiente stock para el producto: " + productoEntidad.getNombre());
            }

            // Crear pedido
            Pedido pedido = new Pedido();
            pedido.setCliente(clienteEntidad);
            pedido.setEstado("CREATED");

            DetallePedido det = new DetallePedido();
            det.setProducto(productoEntidad);
            det.setCantidad(cantidad);
            det.setSubtotal(productoEntidad.getPrecio().multiply(BigDecimal.valueOf(cantidad)));

            pedido.setDetalles(List.of(det));

            Pedido pedidoCreado = pedidoService.guardarPedido(pedido);

            // Guardar log
            LogSistema log = new LogSistema();
            log.setEmpleadoId(null);
            log.setAccion("CREAR_PEDIDO");
            log.setDetalle("Pedido ID: " + pedidoCreado.getId() + " creado por cliente: " + clienteEntidad.getNombre());
            log.setCreadoEn(LocalDateTime.now());
            logSistemaRepository.save(log);

            return ResponseEntity.ok(pedidoCreado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    @GetMapping("/pedido/{id}")
    public ResponseEntity<?> obtenerPedido(@PathVariable Long id) {
        try {
            PedidoDTO pedidoDTO = pedidoService.obtenerPedidoCompleto(id);
            
            // Guardar log de consulta
            LogSistema log = new LogSistema();
            log.setEmpleadoId(null);
            log.setAccion("CONSULTAR_PEDIDO");
            log.setDetalle("Pedido ID: " + id);
            log.setCreadoEn(LocalDateTime.now());
            logSistemaRepository.save(log);

            return ResponseEntity.ok(pedidoDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/pedido/{id}")
    public ResponseEntity<?> actualizarPedidoIA(@PathVariable Long id, @RequestBody Map<String,String> cambios) throws IllegalArgumentException {
        try {
            String estado = cambios.get("estado");
            String notas = cambios.get("notas");
            pedidoService.actualizarPedido(id, estado, notas);

            LogSistema log = new LogSistema();
            log.setEmpleadoId(null);
            log.setAccion("ACTUALIZAR_PEDIDO");
            log.setDetalle("Pedido ID: " + id + ", nuevo estado: " + estado);
            log.setCreadoEn(LocalDateTime.now());
            logSistemaRepository.save(log);

            return ResponseEntity.ok("Pedido actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}
