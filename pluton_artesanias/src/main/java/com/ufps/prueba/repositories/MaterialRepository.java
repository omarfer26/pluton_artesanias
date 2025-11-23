package com.ufps.prueba.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ufps.prueba.entities.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {

}
