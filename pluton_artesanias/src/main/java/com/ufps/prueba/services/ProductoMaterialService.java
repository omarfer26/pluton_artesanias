package com.ufps.prueba.services;

import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.entities.ProductoMaterial;
import com.ufps.prueba.entities.Material;
import com.ufps.prueba.repositories.ProductoMaterialRepository;
import com.ufps.prueba.repositories.MaterialRepository;
import com.ufps.prueba.repositories.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoMaterialService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProductoMaterialRepository productoMaterialRepository;

    public List<ProductoMaterial> listarMaterialesDeProducto(Long productoId) {
        return productoMaterialRepository.findByProductoId(productoId);
    }

    public void agregarMaterial(Long productoId, Long materialId, Integer cantidadUsada) {

        if (productoMaterialRepository.existsByProductoIdAndMaterialId(productoId, materialId)) {
            throw new RuntimeException("El material ya está asociado al producto.");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        ProductoMaterial pm = new ProductoMaterial();
        pm.setProducto(producto);
        pm.setMaterial(material);
        pm.setCantidadUsada(cantidadUsada);

        productoMaterialRepository.save(pm);
    }


    public void eliminarRelacion(Long relacionId) {
        productoMaterialRepository.deleteById(relacionId);
    }
}
