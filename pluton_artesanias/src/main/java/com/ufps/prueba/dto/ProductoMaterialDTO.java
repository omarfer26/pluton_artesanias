package com.ufps.prueba.dto;

import lombok.Data;

@Data
public class ProductoMaterialDTO {

    private Long productoId;
    private Long materialId;
    private Integer cantidadUsada; // cuántas unidades se usan por producto
}
