package com.ufps.prueba.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.ufps.prueba.dto.IAMensajeDTO;

@Data
@Entity
@Table(name = "logs_sistema")
public class LogSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "empleado_id")
    private Integer empleadoId;

    private String accion;

    private String detalle;

    private String ip;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

}