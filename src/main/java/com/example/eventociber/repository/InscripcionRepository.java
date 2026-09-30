package com.example.eventociber.repository;

import com.example.eventociber.model.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    boolean existsByCorreo(String correo);
    boolean existsByIdEstudiante(String idEstudiante);
}
