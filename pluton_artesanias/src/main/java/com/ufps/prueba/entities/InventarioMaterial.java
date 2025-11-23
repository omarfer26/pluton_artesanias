package com.ufps.prueba.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    private Integer cantidad;

    private Integer reservado;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
