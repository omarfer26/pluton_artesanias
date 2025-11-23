package com.ufps.prueba.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ufps.prueba.dto.IAMensajeDTO;
import com.ufps.prueba.dto.PedidoDTO;
import com.ufps.prueba.entities.DetallePedido;
import com.ufps.prueba.entities.LogSistema;
import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.entities.Cliente;
import com.ufps.prueba.repositories.LogSistemaRepository;
import com.ufps.prueba.services.ClienteService;
import com.ufps.prueba.services.InventarioService;
import com.ufps.prueba.services.PedidoService;
import com.ufps.prueba.services.ProductoService;

@RestController
@RequestMapping("/webhook/ig")
public class IAWebhookController {

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

    private static final String TOKEN_SIMULADO = "TOKEN_IG_SIMULADO";

    @GetMapping
    public ResponseEntity<?> verificarWebhook(@RequestParam("hub.verify_token") String verifyToken,
                                              @RequestParam("hub.challenge") String challenge) {
        if (TOKEN_SIMULADO.equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(403).body("Token inválido");
        }
    }
    
    @PostMapping(
            value = "/mensaje",
            consumes = {"application/json", "application/json;charset=UTF-8"},
            produces = "application/json"
    )
    public ResponseEntity<?> recibirMensaje(
            @RequestHeader(value="X-Hub-Signature", required=false) String signature,
            @RequestBody IAMensajeDTO mensaje) {
        try {
            // Validar firma
            if (signature == null || !signature.equals(TOKEN_SIMULADO)) {
                return ResponseEntity.status(403).body("Firma inválida");
            }

            String textoOriginal = mensaje.getMensaje();
            String texto = textoOriginal.toLowerCase().trim();

            // Obtener entidades reales
            Cliente clienteEntidad = clienteService.obtenerClientePorId(mensaje.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Producto productoEntidad = null;
            if (mensaje.getProducto() != null) {
                productoEntidad = productoService.obtenerProductoPorId(mensaje.getProducto().getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            }
            int cantidad = mensaje.getCantidad();
            String respuesta = "";

            // Detectar intención
            String intencion;
            if (texto.matches(".*\\b(hola|buenos días|buenas tardes|saludos)\\b.*")) {
                intencion = "SALUDO";
            } else if (texto.matches(".*\\b(productos|disponibles|qué tienes)\\b.*")) {
                intencion = "CONSULTAR_PRODUCTOS";
            } else if (texto.matches(".*\\b(quiero|pedir|comprar)\\b.*") && productoEntidad != null) {
                intencion = "CREAR_PEDIDO";
            } else if (texto.matches(".*\\b(estado|pedido)\\b.*") && texto.matches(".*\\d+.*")) {
                intencion = "CONSULTAR_PEDIDO";
            } else if (texto.matches(".*\\b(cancelar|anular)\\b.*") && texto.matches(".*\\d+.*")) {
                intencion = "CANCELAR_PEDIDO";
            } else {
                intencion = "DESCONOCIDO";
            }

            // Procesar intención
            switch (intencion) {
                case "SALUDO":
                    respuesta = "¡Hola " + clienteEntidad.getNombre() + "! 😊 ¿En qué puedo ayudarte hoy?";
                    registrarLog("SALUDO_IA", "Cliente: " + clienteEntidad.getNombre() + " envió saludo");
                    break;

                case "CONSULTAR_PRODUCTOS":
                    List<Producto> productos = productoService.listarProductos();
                    String nombres = productos.stream()
                            .map(Producto::getNombre)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("No hay productos disponibles en este momento.");
                    respuesta = "Actualmente tenemos los siguientes productos disponibles: " + nombres;
                    registrarLog("CONSULTAR_PRODUCTOS", "Cliente: " + clienteEntidad.getNombre() + " consultó productos");
                    break;

                case "CREAR_PEDIDO":
                    if (!inventarioService.puedeFabricar(productoEntidad.getId(), cantidad)) {
                        respuesta = "Lo siento, no hay suficiente stock de " + productoEntidad.getNombre();
                        registrarLog("CREAR_PEDIDO_FALLIDO", "Cliente: " + clienteEntidad.getNombre() + ", producto: " + productoEntidad.getNombre() + " sin stock");
                        break;
                    }

                    Pedido pedido = new Pedido();
                    pedido.setCliente(clienteEntidad);
                    pedido.setEstado("CREATED");

                    DetallePedido det = new DetallePedido();
                    det.setProducto(productoEntidad);
                    det.setCantidad(cantidad);
                    det.setSubtotal(productoEntidad.getPrecio().multiply(BigDecimal.valueOf(cantidad)));

                    pedido.setDetalles(List.of(det));
                    Pedido pedidoCreado = pedidoService.guardarPedido(pedido);

                    respuesta = "Tu pedido #" + pedidoCreado.getId() + " ha sido creado correctamente. Total: $" + pedidoCreado.getTotal();
                    registrarLog("CREAR_PEDIDO", "Cliente: " + clienteEntidad.getNombre() + ", Pedido ID: " + pedidoCreado.getId());
                    break;

                case "CONSULTAR_PEDIDO":
                    Long pedidoIdConsulta = Arrays.stream(texto.split(" "))
                            .filter(s -> s.matches("\\d+"))
                            .map(Long::parseLong)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No se encontró ID de pedido en el mensaje"));
                    PedidoDTO pedidoDTO = pedidoService.obtenerPedidoCompleto(pedidoIdConsulta);
                    respuesta = "Pedido #" + pedidoIdConsulta + " - Estado: " + pedidoDTO.getEstado();
                    registrarLog("CONSULTAR_PEDIDO", "Cliente: " + clienteEntidad.getNombre() + ", Pedido ID: " + pedidoIdConsulta);
                    break;

                case "CANCELAR_PEDIDO":
                    Long pedidoIdCancelar = Arrays.stream(texto.split(" "))
                            .filter(s -> s.matches("\\d+"))
                            .map(Long::parseLong)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No se encontró ID de pedido en el mensaje"));
                    pedidoService.actualizarPedido(pedidoIdCancelar, "CANCELLED", "Cancelado por cliente vía IG");
                    respuesta = "Pedido #" + pedidoIdCancelar + " ha sido cancelado correctamente.";
                    registrarLog("CANCELAR_PEDIDO", "Cliente: " + clienteEntidad.getNombre() + ", Pedido ID: " + pedidoIdCancelar + " cancelado");
                    break;

                case "DESCONOCIDO":
                default:
                    respuesta = "Lo siento, no entendí tu mensaje. Puedes consultar productos, crear pedidos o consultar/cancelar tus pedidos.";
                    registrarLog("RESPUESTA_IA", "Mensaje no reconocido de cliente: " + clienteEntidad.getNombre() + ", texto: " + textoOriginal);
                    break;
            }

            return ResponseEntity.ok(Map.of("respuesta", respuesta));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    
    @GetMapping("/cliente/{clienteId}/pedidos")
    public ResponseEntity<?> obtenerPedidosPorCliente(@PathVariable Long clienteId) {
        try {
            List<Pedido> pedidos = pedidoService.listarPorCliente(clienteId);

            registrarLog("CONSULTAR_PEDIDOS_CLIENTE", "Cliente IG ID: " + clienteId + " consultó todos sus pedidos");

            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void registrarLog(String accion, String detalle) {
        LogSistema log = new LogSistema();
        log.setEmpleadoId(null);
        log.setAccion(accion);
        log.setDetalle(detalle);
        log.setCreadoEn(LocalDateTime.now());
        logSistemaRepository.save(log);
    }
}
