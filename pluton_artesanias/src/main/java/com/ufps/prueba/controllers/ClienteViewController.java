package com.ufps.prueba.controllers;

import com.ufps.prueba.dto.PedidoDTO;
import com.ufps.prueba.entities.Cliente;
import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.services.ClienteService;
import com.ufps.prueba.services.PedidoService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cliente")
public class ClienteViewController {

    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private PedidoService pedidoService;

    Pedido pedido = new Pedido();

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login-cliente";
    }

    @PostMapping("/login")
    public String loginCliente(@RequestParam String correo,
                               @RequestParam String contrasena,
                               Model model,
                               HttpSession session) {

        Cliente cliente = clienteService.buscarPorCorreo(correo);

        if (cliente == null || !cliente.getContrasenaHash().equals(contrasena)) {
            model.addAttribute("error", "Credenciales inválidas");
            return "login-cliente";
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

        List<Pedido> pedidos = pedidoService.listarPorCliente(cliente.getId());

        model.addAttribute("cliente", cliente);
        model.addAttribute("pedidos", pedidos);

        return "cliente/dashboard";
    }
    
    @GetMapping("/dashboard/pedido/{id}")
    public String verPedido(@PathVariable Long id, Model model) {
        PedidoDTO pedidoDTO = pedidoService.obtenerPedidoCompleto(id);
        model.addAttribute("pedido", pedidoDTO);
        return "cliente/pedido-detalle";
    }

    @PostMapping("/dashboard/pedido/{id}/cancelar")
    public String cancelarPedido(@PathVariable Long id) {

        pedidoService.actualizarPedido(
                id,
                "CANCELLED",
                "Pedido cancelado por el cliente"
        );

        return "redirect:/cliente/dashboard";
    }

    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/cliente/login";
    }


}
