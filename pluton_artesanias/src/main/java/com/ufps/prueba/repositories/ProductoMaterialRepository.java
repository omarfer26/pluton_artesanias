package com.ufps.prueba.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufps.prueba.entities.Material;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.entities.ProductoMaterial;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoMaterialRepository extends JpaRepository<ProductoMaterial, Long> {
	
    List<ProductoMaterial> findByProductoId(Long productoId);
    boolean existsByProductoAndMaterial(Producto producto, Material material);
    boolean existsByProductoIdAndMaterialId(Long productoId, Long materialId);
    Optional<ProductoMaterial> findByProductoAndMaterial(Producto producto, Material material);

}

