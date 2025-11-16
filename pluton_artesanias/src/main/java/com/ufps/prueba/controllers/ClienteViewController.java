package com.ufps.prueba.controllers;

import com.ufps.prueba.entities.Cliente;
import com.ufps.prueba.services.ClienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cliente")
public class ClienteViewController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login-cliente";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                HttpSession session) {

        Cliente cliente = clienteService.buscarPorCorreo(correo);

        if (cliente == null) {
            return "redirect:/cliente/login?error=true";
        }

        session.setAttribute("cliente", cliente);

        return "redirect:/cliente/dashboard";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {

        Cliente cliente = (Cliente) session.getAttribute("cliente");

        if (cliente == null) {
            return "redirect:/cliente/login";
        }

        model.addAttribute("cliente", cliente);

        return "cliente/dashboard";
    }
}
