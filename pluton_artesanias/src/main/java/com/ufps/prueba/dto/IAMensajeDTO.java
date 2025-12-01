package com.ufps.prueba.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IAMensajeDTO {
    private String mensaje;
    private ClienteDTO cliente;
    private EmpleadoDTO empleado;
    private ProductoDTO producto;
    private MaterialDTO material;
    private String rol;
    private int cantidad;
    private List<ProductoCantidadDTO> productos;
}
