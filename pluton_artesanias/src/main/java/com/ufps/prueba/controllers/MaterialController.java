package com.ufps.prueba.controllers;

import com.ufps.prueba.entities.Material;
import com.ufps.prueba.repositories.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/materiales")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    @GetMapping
    public String listarMateriales(Model model) {
        model.addAttribute("materiales", materialRepository.findAll());
        return "admin/materiales/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoMaterial(Model model) {
        model.addAttribute("material", new Material());
        return "admin/materiales/form";
    }

    @PostMapping("/guardar")
    public String guardarMaterial(@ModelAttribute Material material) {
        materialRepository.save(material);
        return "redirect:/admin/materiales";
    }

    @GetMapping("/{id}/editar")
    public String editarMaterial(@PathVariable Long id, Model model) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));
        model.addAttribute("material", m);
        return "admin/materiales/form";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarMaterial(@PathVariable Long id) {
        materialRepository.deleteById(id);
        return "redirect:/admin/materiales";
    }
}
