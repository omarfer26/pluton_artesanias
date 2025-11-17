package com.ufps.prueba.dto;

import lombok.*;
import java.math.BigDecimal;
import com.ufps.prueba.entities.Producto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoDTO {
    private Long id;
    private Integer cantidad;
    private BigDecimal subtotal;
    private Producto producto;
}
