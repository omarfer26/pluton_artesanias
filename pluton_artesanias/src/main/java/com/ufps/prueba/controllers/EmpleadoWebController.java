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

import com.ufps.prueba.dto.PedidoDTO;
import com.ufps.prueba.entities.Empleado;
import com.ufps.prueba.entities.Pedido;
import com.ufps.prueba.repositories.EmpleadoRepository;
import com.ufps.prueba.services.PedidoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/empleado")
public class EmpleadoWebController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PedidoController pedidoController;
    
    @Autowired
    private PedidoService pedidoService;

    Pedido pedido = new Pedido();
    
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

        model.addAttribute("empleado", emp.get());
        return "redirect:/empleado/dashboard/" + emp.get().getId();
    }
    
    @GetMapping("/dashboard/{id}")
    public String dashboard(@PathVariable Long id, Model model) {

        Empleado empleado = empleadoRepository.findById(id).orElse(null);
        model.addAttribute("empleado", empleado);
        List<Pedido> pedidosActivos = pedidoController.listarPedidosActivos();
        model.addAttribute("pedidos", pedidosActivos);
        List<Pedido> totalPedidos = pedidoController.listarPedidos();
        model.addAttribute("totalPedidos", totalPedidos);
        
        return "empleado/dashboard";
    }
    
    @GetMapping("/dashboard/pedido/{id}")
    public String verPedido(@PathVariable Long id, Model model) {
        PedidoDTO pedidoDTO = pedidoService.obtenerPedidoCompleto(id);
        model.addAttribute("pedido", pedidoDTO);
        return "empleado/pedido-detalle";
    }

    @PostMapping("/dashboard/pedido/{id}/actualizar")
    public String actualizarPedido(@PathVariable Long id,
                                   @RequestParam("estado") String estado,
                                   @RequestParam("notas") String notas) {

        pedidoService.actualizarPedido(id, estado, notas);
        return "redirect:/empleado/dashboard/pedido/" + id;
    }
    
    @PostMapping("/dashboard/pedido/{id}/cancelar")
    public String cancelarPedido(@PathVariable Long id) {

        pedidoService.actualizarPedido(
                id,
                "CANCELLED",
                "Pedido cancelado por el empleado"
        );

        return "redirect:/empleado/dashboard";
    }


    @GetMapping("/logout")
    public String logoutEmpleado(HttpSession session) {
        session.invalidate();
        return "redirect:/empleado/login";
    }

    
}
