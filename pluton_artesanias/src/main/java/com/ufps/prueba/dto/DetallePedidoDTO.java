package com.ufps.prueba.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoDTO {
    private Long id;
    private Integer cantidad;
    private BigDecimal subtotal;
    private ProductoDTO producto;
}
