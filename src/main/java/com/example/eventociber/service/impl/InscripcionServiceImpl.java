package com.example.eventociber.service.impl;

import com.example.eventociber.dto.InscripcionRequest;
import com.example.eventociber.dto.InscripcionResponse;
import com.example.eventociber.exception.DatosInvalidosException;
import com.example.eventociber.exception.InscripcionDuplicadaException;
import com.example.eventociber.model.entity.Inscripcion;
import com.example.eventociber.model.enums.AsistenciaJornada;
import com.example.eventociber.model.enums.EquipoDia2;
import com.example.eventociber.repository.InscripcionRepository;
import com.example.eventociber.service.InscripcionService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository repository;

    @Override
    @Transactional
    public InscripcionResponse registrarInscripcion(InscripcionRequest request) {
        
        // 1. Manejo del Honeypot (Respuesta idéntica a la real)
        if (request.getSitioWeb() != null && !request.getSitioWeb().trim().isEmpty()) {
            log.warn("Bot detectado mediante honeypot. Inscripción simulada con éxito para el usuario: {}", maskLog(request.getNombreUsuario()));
            return InscripcionResponse.builder()
                    .nombreUsuario(request.getNombreUsuario())
                    .mensaje("Inscripción realizada con éxito")
                    .build();
        }

        // 2. Saneamiento de datos adicional
        String nombreSaneado = request.getNombreUsuario().replaceAll("\\s+", " "); // Colapsar espacios extra
        String carreraSaneada = request.getCarrera().replaceAll("\\s+", " ");
        String correoSaneado = request.getCorreo().toLowerCase(Locale.ROOT);
        String idEstudianteSaneado = request.getIdEstudiante();

        // 3. Validar duplicados a nivel de aplicación
        // Nota de Seguridad: El código 409 permite inferir si un correo o ID ya está inscrito.
        // Este es un compromiso aceptado por negocio; la mitigación principal contra 
        // la enumeración de usuarios será el rate limiting por IP (RateLimitFilter).
        if (repository.existsByCorreo(correoSaneado) || repository.existsByIdEstudiante(idEstudianteSaneado)) {
            log.warn("Intento de inscripción con datos duplicados (interceptado antes de persistir).");
            throw new InscripcionDuplicadaException("Ya existe una inscripción con estos datos");
        }

        // 4. Mapear enums
        AsistenciaJornada jornadaEnum;
        EquipoDia2 equipoEnum;
        try {
            jornadaEnum = AsistenciaJornada.valueOf(request.getAsistenciaJornada());
            equipoEnum = EquipoDia2.valueOf(request.getEquipoDia2());
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException("Valores de jornada o equipo desconocidos");
        }

        // 5. Regla de negocio: SOLO_DIA_1 fuerza NO_IRE en equipo_dia_2
        if (jornadaEnum == AsistenciaJornada.SOLO_DIA_1) {
            equipoEnum = EquipoDia2.NO_IRE;
        }

        // 6. Mapeo a entidad y persistencia
        Inscripcion inscripcion = Inscripcion.builder()
                .nombreUsuario(nombreSaneado)
                .carrera(carreraSaneada)
                .idEstudiante(idEstudianteSaneado)
                .correo(correoSaneado)
                .asistenciaJornada(jornadaEnum)
                .equipoDia2(equipoEnum)
                .build();

        try {
            Inscripcion guardada = repository.saveAndFlush(inscripcion);
            return InscripcionResponse.builder()
                    .nombreUsuario(guardada.getNombreUsuario())
                    .mensaje("Inscripción realizada con éxito")
                    .build();
        } catch (DataIntegrityViolationException ex) {
            Throwable cause = ex.getCause();
            while (cause != null) {
                if (cause instanceof java.sql.SQLException sqlEx) {
                    // En esta tabla las únicas restricciones únicas son correo e id_estudiante (el id PK es autogenerado).
                    // Por lo tanto, un SQLState "23505" (unique_violation) siempre indica que los datos del usuario están duplicados.
                    if ("23505".equals(sqlEx.getSQLState())) {
                        log.warn("Condición de carrera detectada en base de datos. Violación de unicidad (SQLState 23505).");
                        throw new InscripcionDuplicadaException("Ya existe una inscripción con estos datos");
                    }
                }
                cause = cause.getCause();
            }
            throw ex; // Vuelve a lanzar si es otro error, para que el ControllerAdvice devuelva 500
        }
    }

    private String maskLog(String data) {
        if (data == null || data.length() < 3) return "***";
        return data.substring(0, 3) + "***";
    }
}
