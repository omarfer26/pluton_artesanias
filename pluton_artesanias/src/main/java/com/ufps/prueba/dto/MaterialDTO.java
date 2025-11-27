package com.ufps.prueba.dto;

import lombok.Data;

@Data
public class MaterialDTO {
    private String nombre;
    private String unidad;
    private Double stockActual;
    private Double stockMinimo;
    private String proveedor;
    private Integer tiempoReposicionDias;
}
