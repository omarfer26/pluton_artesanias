package com.ufps.prueba.repositories;

import com.ufps.prueba.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    Cliente findByCorreo(String correo);
    
}
