package com.ufps.prueba.controllers;

import com.ufps.prueba.entities.Inventario;
import com.ufps.prueba.repositories.InventarioRepository;
import com.ufps.prueba.repositories.ProductoRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/inventario/productos")
public class InventarioProductoController {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/listar")
    public String listarInventario(Model model) {
        model.addAttribute("items", inventarioRepository.findAll());
        return "empleado/inventario/productos/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarProductoInventario(@PathVariable Long id, Model model) {
        Optional<Inventario> inv = inventarioRepository.findByProductoId(id);
        model.addAttribute("inventario", inv);
        model.addAttribute("producto", productoRepository.findById(id).orElse(null));
        return "empleado/inventario/productos-form";
    }

    @PostMapping("/guardar")
    public String guardarInventario(@ModelAttribute Inventario inventario) {
        inventarioRepository.save(inventario);
        return "redirect:/empleado/inventario/productos";
    }
}
