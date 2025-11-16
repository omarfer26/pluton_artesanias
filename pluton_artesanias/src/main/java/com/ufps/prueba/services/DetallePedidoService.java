package com.ufps.prueba.services;

import com.ufps.prueba.entities.DetallePedido;
import com.ufps.prueba.repositories.DetallePedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    public List<DetallePedido> listarDetalles() {
        return detallePedidoRepository.findAll();
    }

    public List<DetallePedido> listarPorPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    public DetallePedido guardarDetalle(DetallePedido detalle) {
        return detallePedidoRepository.save(detalle);
    }

    public void eliminarDetalle(Long id) {
        detallePedidoRepository.deleteById(id);
    }
}
