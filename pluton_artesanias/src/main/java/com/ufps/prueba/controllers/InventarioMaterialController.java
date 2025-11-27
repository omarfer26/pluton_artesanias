package com.ufps.prueba.controllers;

import com.ufps.prueba.dto.MaterialDTO;
import com.ufps.prueba.entities.InventarioMaterial;
import com.ufps.prueba.entities.Material;
import com.ufps.prueba.repositories.InventarioMaterialRepository;
import com.ufps.prueba.repositories.MaterialRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inventario/material")
public class InventarioMaterialController {

    @Autowired
    private InventarioMaterialRepository inventarioMaterialRepository;

    @Autowired
    private MaterialRepository materialRepository;
    
    @GetMapping("/listar")
    public String listarInventario(Model model) {
        model.addAttribute("items", inventarioMaterialRepository.findAll());
        return "empleado/inventario/material/listar";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Optional<InventarioMaterial> inv = inventarioMaterialRepository.findByMaterialId(id);
        model.addAttribute("inventario", inv);
        model.addAttribute("material", materialRepository.findById(id).orElse(null));
        return "empleado/inventario/materiales-form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute InventarioMaterial inv) {
        inventarioMaterialRepository.save(inv);
        return "redirect:/empleado/inventario/material";
    }
}
