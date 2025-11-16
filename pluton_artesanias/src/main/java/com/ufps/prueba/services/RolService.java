package com.ufps.prueba.services;

import com.ufps.prueba.entities.Rol;
import com.ufps.prueba.repositories.RolRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    public Rol crearRol(Rol rol) {
        return rolRepository.save(rol);
    }
}
