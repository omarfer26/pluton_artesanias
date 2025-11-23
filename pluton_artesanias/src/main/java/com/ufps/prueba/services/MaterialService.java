package com.ufps.prueba.services;

import com.ufps.prueba.entities.Material;
import com.ufps.prueba.repositories.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    public List<Material> listarMateriales() {
        return materialRepository.findAll();
    }

    public Material obtenerMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));
    }

    public Material crearMaterial(Material material) {
        return materialRepository.save(material);
    }

    public Material actualizarMaterial(Material material) {
        Material m = obtenerMaterial(material.getId());
        m.setNombre(material.getNombre());
        m.setUnidad(material.getUnidad());
        m.setStockMinimo(material.getStockMinimo());
        return materialRepository.save(m);
    }

    public void eliminarMaterial(Long id) {
        materialRepository.deleteById(id);
    }
}
