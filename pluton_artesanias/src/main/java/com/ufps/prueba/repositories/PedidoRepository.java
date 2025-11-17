package com.ufps.prueba.repositories;

import com.ufps.prueba.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByEmpleadoAsignado_Id(Long empleadoId);
    List<Pedido> findByEstado(String estado);
    @Query(value = "SELECT * FROM pedidos WHERE estado::text NOT IN (:estados)", nativeQuery = true)
    List<Pedido> findPedidosActivos(@Param("estados") List<String> estados);
}
