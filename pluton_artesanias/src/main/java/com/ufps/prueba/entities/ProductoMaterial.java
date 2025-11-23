package com.ufps.prueba.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "cantidad_usada")
    private Integer cantidadUsada;
}
