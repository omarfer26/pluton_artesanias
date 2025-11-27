package com.ufps.prueba.controllers;

import com.ufps.prueba.dto.ProductoMaterialDTO;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.repositories.ProductoRepository;
import com.ufps.prueba.services.ProductoMaterialService;
import com.ufps.prueba.repositories.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/productos")
public class ProductoMaterialController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProductoMaterialService productoMaterialService;

    @GetMapping("/{id}/materiales")
    public String verMateriales(@PathVariable Long id, Model model) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("producto", producto);
        model.addAttribute("relaciones", productoMaterialService.listarMaterialesDeProducto(id));
        model.addAttribute("materialesDisponibles", materialRepository.findAll());

        return "admin/productos/materiales";
    }

    @PostMapping("/{id}/materiales/agregar")
    public String agregarMaterial(
            @PathVariable Long id,
            @RequestParam Long materialId,
            @RequestParam Integer cantidad) {

        productoMaterialService.agregarMaterial(id, materialId, cantidad);

        return "redirect:/admin/productos/" + id + "/materiales";
    }

    @PostMapping("/asociar")
    public String asociarMaterialAProducto(@ModelAttribute ProductoMaterialDTO dto, RedirectAttributes ra) {

        try {
            productoMaterialService.agregarMaterial(
                dto.getProductoId(),
                dto.getMaterialId(),
                dto.getCantidadUsada()
            );

            ra.addFlashAttribute("success", "Material asociado correctamente.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/producto/" + dto.getProductoId() + "/materiales";
    }


    @PostMapping("/materiales/{relacionId}/eliminar")
    public String eliminarMaterial(@PathVariable Long relacionId) {

        productoMaterialService.eliminarRelacion(relacionId);
        return "redirect:/admin/productos";
    }
}
