package com.ufps.prueba.services;

import com.ufps.prueba.dto.DetallePedidoDTO;
import com.ufps.prueba.dto.PedidoDTO;
import com.ufps.prueba.dto.ProductoCantidadDTO;
import com.ufps.prueba.entities.DetallePedido;
import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.repositories.PedidoRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoService detallePedidoService;

    @Autowired
    private InventarioService inventarioService;

    public PedidoService(PedidoRepository pedidoRepository,
                         DetallePedidoService detallePedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoService = detallePedidoService;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> obtenerPedido(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional
    public Pedido guardarPedido(Pedido pedido) {
        // Validar stock
        for (DetallePedido det : pedido.getDetalles()) {
            if (!inventarioService.puedeFabricar(det.getProducto().getId(), det.getCantidad())) {
                throw new RuntimeException(
                        "Stock insuficiente para el producto: " + det.getProducto().getNombre());
            }
        }
        
        if (pedido.getCreadoEn() == null) {
            pedido.setCreadoEn(LocalDateTime.now());
        }
        pedido.setActualizadoEn(LocalDateTime.now());
        
        for (DetallePedido det : pedido.getDetalles()) {
            inventarioService.puedeFabricar(det.getProducto().getId(), det.getCantidad());
        }

        return pedidoRepository.save(pedido);
    }

    public void eliminarPedido(Long id) {
        pedidoRepository.deleteById(id);
    }

    public Optional<Pedido> obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    public List<Pedido> obtenerPedidosActivos() {
        List<String> estadosNoActivos = Arrays.asList("CANCELLED", "DELIVERED");
        return pedidoRepository.findPedidosActivos(estadosNoActivos);
    }

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "CREATED",
            "PROCESSING",
            "SHIPPED",
            "DELIVERED",
            "CANCELLED"
    );

    @Transactional
    public void actualizarPedido(Long id, String estado, String notas, LocalDateTime fechaEntrega) {

        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new IllegalArgumentException("Estado inválido: " + estado);
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(estado);
        pedido.setNotas(notas);
        
        if (fechaEntrega != null) {
            pedido.setFechaEntrega(fechaEntrega);
        }

        pedido.setActualizadoEn(LocalDateTime.now());

        pedidoRepository.save(pedido);
    }

    public PedidoDTO obtenerPedidoCompleto(Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setId(pedido.getId());
        pedidoDTO.setClienteNombre(pedido.getCliente().getNombre());
        pedidoDTO.setEstado(pedido.getEstado());
        pedidoDTO.setNotas(pedido.getNotas());
        pedidoDTO.setTotal(pedido.getTotal());
        pedidoDTO.setCreadoEn(pedido.getCreadoEn());
        pedidoDTO.setActualizadoEn(pedido.getActualizadoEn());
        pedidoDTO.setFechaEntrega(pedido.getFechaEntrega());

        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedidoId);

        List<DetallePedidoDTO> detallesDTO = detalles.stream().map(det -> {
            DetallePedidoDTO dDTO = new DetallePedidoDTO();
            dDTO.setProducto(det.getProducto());
            dDTO.setCantidad(det.getCantidad());
            dDTO.setSubtotal(det.getSubtotal());
            return dDTO;
        }).collect(Collectors.toList());

        pedidoDTO.setDetalles(detallesDTO);

        return pedidoDTO;
    }

    public List<PedidoDTO> listarPedidosProximos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<String> estadosExcluidos = Arrays.asList("CANCELLED", "DELIVERED");

        List<Pedido> proximos = pedidoRepository.findAll().stream()
                .filter(p -> p.getFechaEntrega() != null)
                .filter(p -> !estadosExcluidos.contains(p.getEstado()))
                .filter(p -> p.getFechaEntrega().isAfter(ahora))
                .sorted(Comparator.comparing(Pedido::getFechaEntrega))
                .collect(Collectors.toList());

        return proximos.stream().map(p -> {
            PedidoDTO dto = new PedidoDTO();
            dto.setId(p.getId());
            dto.setClienteNombre(p.getCliente().getNombre());
            dto.setEstado(p.getEstado());
            dto.setNotas(p.getNotas());
            dto.setFechaEntrega(p.getFechaEntrega());
            dto.setTotal(p.getTotal());
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public Pedido crearPedidoConReserva(Pedido pedido, List<ProductoCantidadDTO> listaProductos) {
        if (pedido.getCreadoEn() == null) {
            pedido.setCreadoEn(LocalDateTime.now());
        }
        pedido.setActualizadoEn(LocalDateTime.now());
        pedido.setTotal(BigDecimal.ZERO);

        // Guardar el pedido primero para tener ID
        pedido = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;

        for (ProductoCantidadDTO pc : listaProductos) {
            Producto producto = inventarioService.obtenerInventarioProducto(pc.getId()).getProducto();

            // Validar stock
            if (!inventarioService.puedeFabricar(producto.getId(), pc.getCantidad())) {
                throw new RuntimeException("Stock insuficiente: " + producto.getNombre());
            }

            // Reservar producto
            inventarioService.reservarProducto(producto.getId(), pc.getCantidad());

            // Crear detalle
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(pc.getCantidad());
            detalle.setUnitPrice(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(pc.getCantidad())));
            detallePedidoService.guardarDetalle(detalle);

            total = total.add(detalle.getSubtotal());
        }

        // Actualizar total del pedido
        pedido.setTotal(total);
        pedido.setActualizadoEn(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        return pedido;
    }

    
}
