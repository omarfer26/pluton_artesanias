package com.ufps.prueba.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ufps.prueba.dto.MaterialDTO;
import com.ufps.prueba.dto.ProductoDTO;
import com.ufps.prueba.entities.Material;
import com.ufps.prueba.repositories.InventarioMaterialRepository;
import com.ufps.prueba.repositories.InventarioRepository;
import com.ufps.prueba.repositories.MaterialRepository;
import com.ufps.prueba.services.ProductoService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Controller
@RequestMapping("/empleado/inventario")
public class AdminInventarioWebController {

    @Autowired
    private InventarioRepository inventarioProductoRepo;

    @Autowired
    private InventarioMaterialRepository inventarioMaterialRepo;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private MaterialRepository materialRepository;

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
