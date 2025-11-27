package com.ufps.prueba.controllers;

import com.ufps.prueba.entities.Material;
import com.ufps.prueba.repositories.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    @GetMapping
    public List<Material> listar() {
        return materialRepository.findAll();
    }

    @GetMapping("/{id}")
    public Material obtener(@PathVariable Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));
    }

    @PostMapping
    public Material crear(@RequestBody Material material) {
        material.setId(null);
        return materialRepository.save(material);
    }

    @PutMapping("/{id}")
    public Material actualizar(@PathVariable Long id, @RequestBody Material material) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        m.setNombre(material.getNombre());
        m.setUnidad(material.getUnidad());
        m.setStockActual(material.getStockActual());
        m.setStockMinimo(material.getStockMinimo());
        m.setProveedor(material.getProveedor());
        m.setTiempoReposicionDias(material.getTiempoReposicionDias());

        return materialRepository.save(m);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        materialRepository.deleteById(id);
    }
}
