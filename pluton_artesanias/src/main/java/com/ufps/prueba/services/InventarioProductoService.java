package com.ufps.prueba.services;

import com.ufps.prueba.entities.Inventario;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.repositories.InventarioRepository;
import com.ufps.prueba.repositories.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InventarioProductoService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Inventario obtenerPorProducto(Long productoId) {
        return inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para producto " + productoId));
    }

    public Inventario crearInventario(Long productoId, int cantidadInicial) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Inventario inv = Inventario.builder()
                .producto(producto)
                .cantidad(cantidadInicial)
                .reservado(0)
                .actualizadoEn(LocalDateTime.now())
                .build();

        return inventarioRepository.save(inv);
    }

    public void actualizarStock(Long productoId, int nuevaCantidad) {
        Inventario inv = obtenerPorProducto(productoId);
        inv.setCantidad(nuevaCantidad);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepository.save(inv);
    }

    public void reservarProducto(Long productoId, int cantidadReserva) {
        Inventario inv = obtenerPorProducto(productoId);
        inv.setReservado(inv.getReservado() + cantidadReserva);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepository.save(inv);
    }

    public void consumirReservado(Long productoId, int cantidad) {
        Inventario inv = obtenerPorProducto(productoId);
        inv.setCantidad(inv.getCantidad() - cantidad);
        inv.setReservado(inv.getReservado() - cantidad);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepository.save(inv);
    }
}
