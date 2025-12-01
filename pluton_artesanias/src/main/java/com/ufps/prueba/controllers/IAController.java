package com.ufps.prueba.controllers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.text.Normalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ufps.prueba.dto.IAMensajeDTO;
import com.ufps.prueba.dto.MaterialDTO;
import com.ufps.prueba.dto.PedidoDTO;
import com.ufps.prueba.dto.ProductoCantidadDTO;
import com.ufps.prueba.entities.*;
import com.ufps.prueba.repositories.LogSistemaRepository;
import com.ufps.prueba.services.ClienteService;
import com.ufps.prueba.services.EmpleadoService;
import com.ufps.prueba.services.InventarioService;
import com.ufps.prueba.services.MaterialService;
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
    private EmpleadoService empleadoService;

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private MaterialService materialService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private LogSistemaRepository logSistemaRepository;

    private static final String TOKEN_SIMULADO = "TOKEN_IG_SIMULADO";

    @PostMapping(
            value = "/mensaje",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> recibirMensaje(@RequestHeader(value="Authorization", required=false) String auth,
                                            @RequestBody IAMensajeDTO mensaje) {
        try {
            if (auth != null && !auth.equals(TOKEN_SIMULADO)) {
                return ResponseEntity.status(403).body(Map.of("error", "Token inválido"));
            }

            String textoOriginal = mensaje.getMensaje();
            String texto = textoOriginal.toLowerCase().trim();
            String respuesta = "";
            
            Cliente clienteEntidad = null;
            Empleado empleadoEntidad = null;
            Producto productoEntidad = null;
            MaterialDTO materialEntidad = null;
            
            if ("CLIENTE".equalsIgnoreCase(mensaje.getRol())) {
                if (mensaje.getCliente() != null) {
                    clienteEntidad = clienteService.obtenerClientePorId(mensaje.getCliente().getId())
                            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
                }
            } else if ("EMPLEADO".equalsIgnoreCase(mensaje.getRol())) {
                if (mensaje.getEmpleado() != null && mensaje.getEmpleado().getId() != null) {
                    empleadoEntidad = empleadoService.obtenerEmpleadoPorId(
                            mensaje.getEmpleado().getId()
                    ).orElse(null);
                }
            }
            
            if (mensaje.getProducto() != null) {
                productoEntidad = productoService.obtenerProductoPorId(mensaje.getProducto().getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            }
            
            if (productoEntidad == null) {
                Optional<Long> idProd = Arrays.stream(texto.split(" "))
                                              .filter(s -> s.matches("\\d+"))
                                              .map(Long::parseLong)
                                              .findFirst();
                if (idProd.isPresent()) {
                    productoEntidad = productoService.obtenerProductoPorId(idProd.get()).orElse(null);
                }
            }
            
            if (productoEntidad == null) {
                productoEntidad = productoService.listarProductos().stream()
                        .filter(p -> texto.contains(p.getNombre().toLowerCase()))
                        .findFirst()
                        .orElse(null);
            }
            
            if (materialEntidad == null && mensaje.getMaterial() != null) {
                materialEntidad = mensaje.getMaterial();
            }

            int cantidad = mensaje.getCantidad();
            String intencion = detectarIntencion(texto, productoEntidad);

            switch (intencion) {
                case "SALUDO":
                    respuesta = (clienteEntidad != null ? clienteEntidad.getNombre() : "") + 
                                ", ¡hola! 😊 ¿En qué puedo ayudarte?";
                    registrarLog("SALUDO_IA", "Usuario saludó: " + textoOriginal);
                    break;

                case "CONSULTAR_PRODUCTOS":
                    List<Producto> productos = productoService.listarProductos();
                    String productosList = productos.stream()
                            .map(p -> "[" + p.getId() + "] " + p.getNombre())
                            .collect(Collectors.joining(", "));
                    respuesta = "Productos disponibles: " + (productosList.isEmpty() ? "ninguno por el momento" : productosList);
                    break;

                case "CONSULTAR_MATERIALES":
                    List<Material> materiales = materialService.listarMateriales();
                    String materialesList = materiales.stream()
                            .map(m -> "[" + m.getId() + "] " + m.getNombre())
                            .collect(Collectors.joining(", "));
                    if (!materialesList.isEmpty()) {
                        respuesta += "\nMateriales disponibles: " + materialesList;
                    }
                    registrarLog("CONSULTAR_PRODUCTOS", "Consulta de productos y materiales");
                    break;

                case "CONSULTAR_STOCK":
                    if (productoEntidad != null) {
                        int stockReal = inventarioService.obtenerStockProducto(productoEntidad.getId());
                        boolean enMinimo = inventarioService.estaEnMinimoProducto(productoEntidad.getId());
                        respuesta = "Producto [" + productoEntidad.getId() + "] " + productoEntidad.getNombre() +
                                    " - Stock actual: " + stockReal;
                        if (enMinimo) respuesta += " ⚠️ Atención: stock en nivel mínimo";
                    } else if (materialEntidad != null) {
                        int stockMaterial = inventarioService.obtenerStockMaterial(materialEntidad.getId());
                        boolean enMinimo = inventarioService.estaEnMinimoMaterial(materialEntidad.getId());
                        respuesta = "Material [" + materialEntidad.getId() + "] " + materialEntidad.getNombre() +
                                    " - Stock actual: " + stockMaterial;
                        if (enMinimo) respuesta += " ⚠️ Atención: stock en nivel mínimo";
                    } else {
                        respuesta = "Por favor indica el producto o material a consultar.";
                    }
                    registrarLog("CONSULTAR_STOCK", "Consulta de stock");
                    break;

                case "CREAR_PEDIDO":
                    if (clienteEntidad == null) {
                        respuesta = "Solo un cliente puede crear pedidos.";
                        break;
                    }

                    // Extraer productos del texto
                    List<ProductoCantidadDTO> listaProductos = extraerProductosMultiples(texto);
                    if ((listaProductos == null || listaProductos.isEmpty()) && productoEntidad != null) {
                        listaProductos = List.of(new ProductoCantidadDTO(productoEntidad.getId(), (cantidad == 0 ? 1 : cantidad)));
                    }
                    if (listaProductos == null || listaProductos.isEmpty()) {
                        respuesta = "No pude identificar los productos que deseas pedir.";
                        break;
                    }

                    // Empleado por defecto
                    Empleado empleadoAsignado = empleadoService.obtenerEmpleadoPorId(1L)
                            .orElseThrow(() -> new RuntimeException("Empleado default no encontrado"));

                 // 1️⃣ Verificar stock de todos los productos antes de reservar
                    for (ProductoCantidadDTO pc : listaProductos) {
                        Producto producto = productoService.obtenerProductoPorId(pc.getId())
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                        if (!inventarioService.puedeFabricar(producto.getId(), pc.getCantidad())) {
                            respuesta = "Stock insuficiente: " + producto.getNombre();
                            return ResponseEntity.ok(Map.of("respuesta", respuesta));
                        }
                    }

                 // Crear pedido con reserva y detalles automáticamente
                    Pedido pedido = new Pedido();
                    pedido.setCliente(clienteEntidad);
                    pedido.setEmpleadoAsignado(empleadoAsignado);
                    pedido.setEstado("CREATED");

                    pedido = pedidoService.crearPedidoConReserva(pedido, listaProductos);


                    respuesta = "Pedido #" + pedido.getId() + " creado con "
                            + pedido.getDetalles().size() + " productos. Total: $" + pedido.getTotal();
                    registrarLog("CREAR_PEDIDO", "Pedido creado ID: " + pedido.getId());
                    break;

                case "CONSULTAR_PEDIDO":
                    Long pedidoIdConsulta = Arrays.stream(texto.split(" "))
                            .filter(s -> s.matches("\\d+"))
                            .map(Long::parseLong)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No se encontró ID de pedido en el mensaje"));
                    PedidoDTO pedidoDTO = pedidoService.obtenerPedidoCompleto(pedidoIdConsulta);
                    respuesta = "Pedido #" + pedidoIdConsulta + " - Estado: " + pedidoDTO.getEstado() +
                                (pedidoDTO.getNotas() != null ? " - Nota: " + pedidoDTO.getNotas() : "");
                    registrarLog("CONSULTAR_PEDIDO", "Consulta de pedido ID: " + pedidoIdConsulta);
                    break;

                case "CANCELAR_PEDIDO":
                    if (clienteEntidad == null) {
                        respuesta = "Solo un cliente puede cancelar pedidos.";
                        break;
                    }
                    Long pedidoIdCancelar = Arrays.stream(texto.split(" "))
                            .filter(s -> s.matches("\\d+"))
                            .map(Long::parseLong)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No se encontró ID de pedido en el mensaje"));
                    PedidoDTO pCancel = pedidoService.obtenerPedidoCompleto(pedidoIdCancelar);
                    if (pCancel.getEstado().equalsIgnoreCase("SHIPPED") || pCancel.getEstado().equalsIgnoreCase("DELIVERED")) {
                        respuesta = "No se puede cancelar el pedido #" + pedidoIdCancelar + " porque ya fue enviado.";
                    } else {
                        pedidoService.actualizarPedido(pedidoIdCancelar, "CANCELLED", "Pedido cancelado por el cliente", null);
                        respuesta = "Pedido #" + pedidoIdCancelar + " cancelado correctamente.";
                    }
                    registrarLog("CANCELAR_PEDIDO", "Pedido cancelado ID: " + pedidoIdCancelar);
                    break;

                case "PEDIDOS_PROXIMOS":
                    if (empleadoEntidad != null) {
                        List<PedidoDTO> proximos = pedidoService.listarPedidosProximos();
                        if (proximos.isEmpty()) {
                            respuesta = "No hay pedidos próximos a entregar.";
                        } else {
                            respuesta = "Pedidos próximos:\n" +
                                        proximos.stream()
                                                .map(p -> "Pedido #" + p.getId() +
                                                          " - Cliente: " + p.getClienteNombre() +
                                                          " - Fecha entrega: " + p.getFechaEntrega())
                                                .collect(Collectors.joining("\n"));
                        }
                        registrarLog("LISTAR_PEDIDOS_PROXIMOS", "Listado de pedidos próximos");
                    } else {
                        respuesta = "Solo un empleado puede consultar los pedidos próximos.";
                    }
                    break;

                default:
                    respuesta = "Lo siento, no entendí tu mensaje. Puedes consultar productos, stock o pedidos.";
                    registrarLog("RESPUESTA_IA", "Mensaje no reconocido: " + textoOriginal);
                    break;
            }

            return ResponseEntity.ok(Map.of("respuesta", respuesta));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    

    private String detectarIntencion(String texto, Producto productoEntidad) {
        texto = texto.toLowerCase();

        if (texto.matches(".*\\b(hola|buenos días|buenas tardes|saludos)\\b.*")) return "SALUDO";
        if (texto.matches(".*\\b(productos|disponibles|qué tienes)\\b.*")) return "CONSULTAR_PRODUCTOS";
        if (texto.matches(".*\\b(materiales|disponibles|qué tienes)\\b.*")) return "CONSULTAR_MATERIALES";
        if (texto.matches(".*\\b(¿cuánto hay|cantidad|stock|minimo|material)\\b.*")) return "CONSULTAR_STOCK";
        if (texto.matches(".*\\b(proximos|entrega|pendientes|a entregar)\\b.*")) return "PEDIDOS_PROXIMOS";
        if (texto.matches(".*\\b(quiero|pedir|comprar|deseo)\\b.*")) return "CREAR_PEDIDO";
        if (texto.matches(".*\\b(estado|pedido)\\b.*") && texto.matches(".*\\d+.*")) return "CONSULTAR_PEDIDO";
        if (texto.matches(".*\\b(cancelar|anular)\\b.*") && texto.matches(".*\\d+.*")) return "CANCELAR_PEDIDO";

        return "DESCONOCIDO";
    }
    
    private String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.toLowerCase();
    }
    
    private List<ProductoCantidadDTO> extraerProductosMultiples(String texto) {
        List<ProductoCantidadDTO> lista = new ArrayList<>();
        if (texto == null || texto.isBlank()) return lista;
        String t = normalize(texto);

        List<Producto> productos = productoService.listarProductos();
        for (Producto p : productos) {
            String nombre = normalize(p.getNombre()).trim();
            if (nombre.isEmpty()) continue;
            
            Pattern patAntes = Pattern.compile("(\\d+)\\s+" + Pattern.quote(nombre));
            Matcher mAntes = patAntes.matcher(t);
            while (mAntes.find()) {
                int cantidad = Integer.parseInt(mAntes.group(1));
                lista.add(new ProductoCantidadDTO(p.getId(), cantidad));
            }
            
            Pattern patDespues = Pattern.compile(Pattern.quote(nombre) + "\\s*(?:x|\\*)?\\s*(\\d+)");
            Matcher mDespues = patDespues.matcher(t);
            while (mDespues.find()) {
                int cantidad = Integer.parseInt(mDespues.group(1));
                lista.add(new ProductoCantidadDTO(p.getId(), cantidad));
            }
            
            if (!lista.stream().anyMatch(pc -> pc.getId().equals(p.getId()))) {
                if (t.contains(nombre)) {
                    Pattern simple = Pattern.compile("\\b" + Pattern.quote(nombre) + "\\b");
                    Matcher ms = simple.matcher(t);
                    if (ms.find()) {
                        lista.add(new ProductoCantidadDTO(p.getId(), 1));
                    } else {
                        if (nombre.endsWith("s")) {
                            String singular = nombre.substring(0, nombre.length()-1);
                            if (t.contains(singular)) {
                                lista.add(new ProductoCantidadDTO(p.getId(), 1));
                            }
                        }
                    }
                }
            }
        }

        Map<Long, Integer> map = new LinkedHashMap<>();
        for (ProductoCantidadDTO pc : lista) {
            map.put(pc.getId(), map.getOrDefault(pc.getId(), 0) + pc.getCantidad());
        }
        List<ProductoCantidadDTO> consolidada = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : map.entrySet()) {
            consolidada.add(new ProductoCantidadDTO(e.getKey(), e.getValue()));
        }
        return consolidada;
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
