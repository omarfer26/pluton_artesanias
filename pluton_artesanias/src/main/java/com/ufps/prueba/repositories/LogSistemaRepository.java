package com.ufps.prueba.repositories;

import com.ufps.prueba.entities.LogSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogSistemaRepository extends JpaRepository<LogSistema, Integer> {}
