package com.ufps.prueba.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufps.prueba.entities.Inventario;
import com.ufps.prueba.entities.InventarioMaterial;
import com.ufps.prueba.entities.ProductoMaterial;
import com.ufps.prueba.repositories.InventarioMaterialRepository;
import com.ufps.prueba.repositories.InventarioRepository;
import com.ufps.prueba.repositories.ProductoMaterialRepository;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepo;

    @Autowired
    private InventarioMaterialRepository inventarioMaterialRepo;


    @Autowired
    private ProductoMaterialRepository productoMaterialRepo;
    
    public Inventario obtenerInventarioProducto(Long productoId) {
        return inventarioRepo.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para producto " + productoId));
    }

    public void agregarStockProducto(Long productoId, int cantidad) {
        Inventario inv = obtenerInventarioProducto(productoId);
        inv.setCantidad(inv.getCantidad() + cantidad);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepo.save(inv);
    }

    public void reservarProducto(Long productoId, int cantidad) {
        Inventario inv = obtenerInventarioProducto(productoId);

        if (inv.getCantidad() - inv.getReservado() < cantidad) {
            throw new RuntimeException("No hay suficientes productos disponibles para reservar");
        }

        inv.setReservado(inv.getReservado() + cantidad);
        inventarioRepo.save(inv);
    }

    public void liberarReservaProducto(Long productoId, int cantidad) {
        Inventario inv = obtenerInventarioProducto(productoId);
        inv.setReservado(inv.getReservado() - cantidad);
        inventarioRepo.save(inv);
    }

    public void consumirProducto(Long productoId, int cantidad) {
        Inventario inv = obtenerInventarioProducto(productoId);

        if (inv.getCantidad() < cantidad) {
            throw new RuntimeException("No hay suficientes productos para consumir");
        }

        inv.setCantidad(inv.getCantidad() - cantidad);
        inv.setReservado(inv.getReservado() - cantidad);
        inventarioRepo.save(inv);
    }
    
    public InventarioMaterial obtenerInventarioMaterial(Long materialId) {
        return inventarioMaterialRepo.findByMaterialId(materialId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para material " + materialId));
    }

    public void agregarStockMaterial(Long materialId, int cantidad) {
        InventarioMaterial inv = obtenerInventarioMaterial(materialId);
        inv.setCantidad(inv.getCantidad() + cantidad);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioMaterialRepo.save(inv);
    }

    public void consumirMaterial(Long materialId, int cantidad) {
        InventarioMaterial inv = obtenerInventarioMaterial(materialId);

        if (inv.getCantidad() < cantidad) {
            throw new RuntimeException("No hay suficiente material en bodega");
        }

        inv.setCantidad(inv.getCantidad() - cantidad);
        inventarioMaterialRepo.save(inv);
    }
    
    public void consumirMaterialesPorProducto(Long productoId, int cantidad) {

        List<ProductoMaterial> lista = productoMaterialRepo.findByProductoId(productoId);

        for (ProductoMaterial pm : lista) {
            int totalConsumir = pm.getCantidadUsada() * cantidad;
            consumirMaterial(pm.getMaterial().getId(), totalConsumir);
        }
    }
    
    public boolean puedeFabricar(Long productoId, int cantidad) {

        List<ProductoMaterial> lista = productoMaterialRepo.findByProductoId(productoId);

        for (ProductoMaterial pm : lista) {
            int necesario = pm.getCantidadUsada() * cantidad;

            InventarioMaterial inv = obtenerInventarioMaterial(pm.getMaterial().getId());

            if (inv.getCantidad() < necesario) {
                return false; 
            }
        }
        return true;
    }
    
    public List<Inventario> listarInventarioProductos() {
        return inventarioRepo.findAll();
    }
    
}
