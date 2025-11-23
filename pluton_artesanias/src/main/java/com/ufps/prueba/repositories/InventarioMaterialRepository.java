package com.ufps.prueba.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ufps.prueba.entities.InventarioMaterial;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface InventarioMaterialRepository extends JpaRepository<InventarioMaterial, Long> {
    
    Optional<InventarioMaterial> findByMaterialId(Long materialId);

    @Modifying
    @Query("UPDATE InventarioMaterial i SET i.cantidad = i.cantidad + :cantidad WHERE i.material.id = :materialId")
    void agregarStock(@Param("materialId") int materialId,
                      @Param("cantidad") int cantidad);
}
