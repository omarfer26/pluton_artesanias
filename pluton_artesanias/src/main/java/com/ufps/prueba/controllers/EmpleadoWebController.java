package com.ufps.prueba.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ufps.prueba.entities.Empleado;
import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.repositories.EmpleadoRepository;
import com.ufps.prueba.repositories.PedidoRepository;

@Controller
@RequestMapping("/empleado")
public class EmpleadoWebController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login-empleado";
    }

    @PostMapping("/login")
    public String login(@RequestParam String correo,
                        @RequestParam String contrasena,
                        Model model) {

        Optional<Empleado> emp = empleadoRepository.findByCorreo(correo);

        if (emp.isEmpty() || !emp.get().getContrasenaHash().equals(contrasena)) {
            model.addAttribute("error", "Credenciales inválidas");
            return "login-empleado";
        }

        // Guardarlo en sesión
        model.addAttribute("empleado", emp.get());
        return "redirect:/empleado/dashboard/" + emp.get().getId();
    }
    
    @GetMapping("/dashboard/{id}")
    public String dashboard(@PathVariable Long id, Model model) {

        Empleado empleado = empleadoRepository.findById(id).orElse(null);
        List<Pedido> pedidos = pedidoRepository.findByEmpleadoAsignado_Id(empleado.getId());
        model.addAttribute("empleado", empleado);
        model.addAttribute("pedidos", pedidos);
        return "dashboard-empleado";
    }

    
}
