package com.ufps.prueba.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ufps.prueba.repositories.InventarioMaterialRepository;
import com.ufps.prueba.repositories.InventarioRepository;

@Controller
@RequestMapping("/empleado/inventario")
public class AdminInventarioWebController {

    @Autowired
    private InventarioRepository inventarioProductoRepo;

    @Autowired
    private InventarioMaterialRepository inventarioMaterialRepo;

    @GetMapping("/productos")
    public String verProductos(Model model) {
        model.addAttribute("inventario", inventarioProductoRepo.findAll());
        return "empleado/inventario/productos";
    }

    @GetMapping("/material")
    public String verMateriales(Model model) {
        model.addAttribute("inventario", inventarioMaterialRepo.findAll());
        return "empleado/inventario/material";
    }

    @PostMapping("/productos/{id}/agregar")
    public String agregarProducto(@PathVariable int id, @RequestParam int cantidad) {
        inventarioProductoRepo.agregarStock(id, cantidad);
        return "redirect:/empleado/inventario/productos";
    }

    @PostMapping("/material/{id}/agregar")
    public String agregarMaterial(@PathVariable int id, @RequestParam int cantidad) {
        inventarioMaterialRepo.agregarStock(id, cantidad);
        return "redirect:/empleado/inventario/material";
    }
}
