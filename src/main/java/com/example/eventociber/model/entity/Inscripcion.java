package com.example.eventociber.model.entity;

import com.example.eventociber.model.enums.AsistenciaJornada;
import com.example.eventociber.model.enums.EquipoDia2;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Table(
    name = "inscripciones",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inscripciones_correo", columnNames = "correo"),
        @UniqueConstraint(name = "uk_inscripciones_id_estudiante", columnNames = "id_estudiante")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, length = 150)
    private String nombreUsuario;

    @Column(name = "carrera", nullable = false, length = 100)
    private String carrera;

    @Column(name = "id_estudiante", nullable = false, length = 9)
    private String idEstudiante;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(name = "asistencia_jornada", nullable = false, length = 20)
    private AsistenciaJornada asistenciaJornada;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipo_dia_2", nullable = false, length = 20)
    private EquipoDia2 equipoDia2;

    @Column(name = "fecha_inscripcion", nullable = false, updatable = false)
    private LocalDateTime fechaInscripcion;

    @PrePersist
    protected void onCreate() {
        this.fechaInscripcion = LocalDateTime.now();
    }
}
