package com.ufps.prueba.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufps.prueba.entities.Inventario;
import com.ufps.prueba.entities.InventarioMaterial;

@Service
public class AlertaInventarioService {

    @Autowired
    private EmailService emailService;

    private final int UMBRAL_PRODUCTO = 5;
    private final int UMBRAL_MATERIAL = 5;

    // Lista en memoria de alertas flotantes
    private final List<String> alertasFlotantes = new ArrayList<>();

    public void verificarProducto(Inventario inv) {
        int disponible = inv.getCantidad() - inv.getReservado();
        if (disponible <= UMBRAL_PRODUCTO) {
            String asunto = "⚠ Stock bajo del producto " + inv.getProducto().getNombre();
            String mensaje = 
                "El producto '" + inv.getProducto().getNombre() + "' está por agotarse.\n" +
                "Cantidad total: " + inv.getCantidad() + "\n" +
                "Reservado: " + inv.getReservado() + "\n" +
                "Disponible: " + disponible + "\n" +
                "⚠ Revisión recomendada.";
            
            // Enviar correo
            emailService.enviarAlerta(asunto, mensaje);

            // Registrar alerta flotante
            registrarAlertaFlotante(mensaje);
        }
    }

    public void verificarMaterial(InventarioMaterial inv) {
        int disponible = inv.getCantidad() - inv.getReservado();
        if (disponible <= UMBRAL_MATERIAL) {
            String asunto = "⚠ Stock bajo del material " + inv.getMaterial().getNombre();
            String mensaje = 
                "El material '" + inv.getMaterial().getNombre() + "' está por agotarse.\n" +
                "Cantidad total: " + inv.getCantidad() + "\n" +
                "Reservado: " + inv.getReservado() + "\n" +
                "Disponible: " + disponible + "\n" +
                "⚠ Considera realizar un pedido al proveedor: " + inv.getMaterial().getProveedor();

            // Enviar correo
            emailService.enviarAlerta(asunto, mensaje);

            // Registrar alerta flotante
            registrarAlertaFlotante(mensaje);
        }
    }

    private void registrarAlertaFlotante(String mensaje) {
        synchronized (alertasFlotantes) {
            alertasFlotantes.add(mensaje);
            if (alertasFlotantes.size() > 50) { // Limitar historial
                alertasFlotantes.remove(0);
            }
        }
    }

    public List<String> obtenerAlertasRecientes() {
        synchronized (alertasFlotantes) {
            return new ArrayList<>(alertasFlotantes);
        }
    }
}
