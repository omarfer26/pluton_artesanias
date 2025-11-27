package com.ufps.prueba.services;

import com.ufps.prueba.entities.Inventario;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.entities.ProductoMaterial;
import com.ufps.prueba.repositories.InventarioRepository;
import com.ufps.prueba.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private InventarioRepository inventarioRepository;

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }
    
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    public Producto crearProducto(Producto producto) {
        
        producto.setId(null);

        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("La categoría debe tener un ID válido.");
        }

        List<ProductoMaterial> materiales = producto.getMateriales();
        producto.setMateriales(null);

        // 1. Guardar producto base
        Producto guardado = productoRepository.save(producto);

        // 2. Guardar materiales asociados (si hay)
        if (materiales != null) {
            materiales.forEach(pm -> {
                pm.setId(null);
                pm.setProducto(guardado);
            });

            guardado.setMateriales(materiales);
            productoRepository.save(guardado);
        }

        // 3. 🔥 Crear inventario para el producto
        Inventario inventario = new Inventario();
        inventario.setProducto(guardado);
        inventario.setCantidad(1);
        inventario.setReservado(0);
        inventarioRepository.save(inventario);

        inventarioRepository.save(inventario);

        return guardado;
    }


}
