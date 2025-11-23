package com.ufps.prueba.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ufps.prueba.entities.Inventario;
import com.ufps.prueba.entities.Producto;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProducto(Producto producto);

    Optional<Inventario> findByProductoId(Long productoId);

    @Modifying
    @Query("UPDATE Inventario i SET i.cantidad = i.cantidad + :cantidad WHERE i.producto.id = :productoId")
    void agregarStock(@Param("productoId") int productoId,
                      @Param("cantidad") int cantidad);

}
