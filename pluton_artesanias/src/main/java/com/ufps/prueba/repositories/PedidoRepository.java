package com.ufps.prueba.repositories;

import com.ufps.prueba.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByEmpleadoAsignado_Id(Long empleadoId);
    List<Pedido> findByEstado(String estado);
}
