package com.ufps.prueba.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufps.prueba.entities.InventarioMaterial;
import com.ufps.prueba.entities.Material;
import com.ufps.prueba.repositories.InventarioMaterialRepository;
import com.ufps.prueba.repositories.MaterialRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioMaterialService {

    @Autowired
    private InventarioMaterialRepository inventarioRepository;

    @Autowired
    private MaterialRepository materialRepository;

    public InventarioMaterial obtenerPorMaterial(Long materialId) {
        return inventarioRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para material " + materialId));
    }

    public InventarioMaterial crearInventario(Long materialId, int cantidadInicial) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        InventarioMaterial inv = InventarioMaterial.builder()
                .material(material)
                .cantidad(cantidadInicial)
                .reservado(0)
                .actualizadoEn(LocalDateTime.now())
                .build();

        return inventarioRepository.save(inv);
    }

    public void actualizarStock(Long materialId, int nuevaCantidad) {
        InventarioMaterial inv = obtenerPorMaterial(materialId);
        inv.setCantidad(nuevaCantidad);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepository.save(inv);
    }

    public void reservarStock(Long materialId, int cantidadReserva) {
        InventarioMaterial inv = obtenerPorMaterial(materialId);
        inv.setReservado(inv.getReservado() + cantidadReserva);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepository.save(inv);
    }

    public void consumirReservado(Long materialId, int cantidad) {
        InventarioMaterial inv = obtenerPorMaterial(materialId);
        inv.setCantidad(inv.getCantidad() - cantidad);
        inv.setReservado(inv.getReservado() - cantidad);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepository.save(inv);
    }
    
    public List<InventarioMaterial> listarInventarioMateriales() {
        return inventarioRepository.findAll();
    }
    
}
