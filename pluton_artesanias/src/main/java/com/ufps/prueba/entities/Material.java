package com.ufps.prueba.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "materiales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String unidad;

    @Column(name = "stock_actual")
    private Double stockActual;

    @Column(name = "stock_minimo")
    private Double stockMinimo;

    private String proveedor;

    @Column(name = "tiempo_reposicion_dias")
    private Integer tiempoReposicionDias;
}
