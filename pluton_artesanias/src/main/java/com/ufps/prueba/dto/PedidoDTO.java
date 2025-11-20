package com.ufps.prueba.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {
    private Long id;
    private String estado;
    private BigDecimal total;
    private String notas;
    private Long clienteId;
    private String clienteNombre;
    private Long direccionEnvioId;
    private Long empleadoAsignadoId;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private List<DetallePedidoDTO> detalles;
}
