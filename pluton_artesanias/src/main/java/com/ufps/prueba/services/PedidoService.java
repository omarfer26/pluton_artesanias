package com.ufps.prueba.services;

import com.ufps.prueba.dto.DetallePedidoDTO;
import com.ufps.prueba.dto.PedidoDTO;
import com.ufps.prueba.entities.DetallePedido;
import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.repositories.PedidoRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoService detallePedidoService;

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

    public Pedido guardarPedido(Pedido pedido) {
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
    public void actualizarPedido(Long id, String estado, String notas) {

        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new IllegalArgumentException("Estado inválido: " + estado);
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(estado);
        pedido.setNotas(notas);
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

}
