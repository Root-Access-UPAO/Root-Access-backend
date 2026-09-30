package com.example.eventociber.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InscripcionRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 150, message = "El nombre de usuario no puede exceder los 150 caracteres")
    @Pattern(regexp = "^[\\p{L}\\p{M}' .\\-]+$", message = "Solo se permiten letras, espacios, guion, apóstrofe y punto")
    private String nombreUsuario;

    @NotBlank(message = "La carrera es obligatoria")
    @Size(max = 100, message = "La carrera no puede exceder los 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\p{M}' .\\-]+$", message = "Solo se permiten letras, espacios, guion, apóstrofe y punto")
    private String carrera;

    @NotBlank(message = "El ID de estudiante es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El ID de estudiante debe tener exactamente 9 dígitos")
    @Size(min = 9, max = 9, message = "El ID de estudiante debe tener exactamente 9 caracteres")
    private String idEstudiante;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo es inválido")
    @Size(max = 150, message = "El correo no puede exceder los 150 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$", message = "El formato del correo es inválido")
    private String correo;

    @NotBlank(message = "La asistencia a la jornada es obligatoria")
    @Pattern(regexp = "^(AMBOS_DIAS|SOLO_DIA_1|SOLO_DIA_2)$", message = "Asistencia a jornada desconocida")
    private String asistenciaJornada;

    @NotBlank(message = "El equipo para el día 2 es obligatorio")
    @Pattern(regexp = "^(MI_LAPTOP|SIN_LAPTOP|NO_IRE)$", message = "Valor de equipo desconocido")
    private String equipoDia2;

    // Honeypot: campo invisible para humanos, si viene lleno, es un bot
    private String sitioWeb;

    // Setters explícitos con trim() para sanear la entrada antes de validar
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario != null ? nombreUsuario.trim() : null;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera != null ? carrera.trim() : null;
    }

    public void setIdEstudiante(String idEstudiante) {
        this.idEstudiante = idEstudiante != null ? idEstudiante.trim() : null;
    }

    public void setCorreo(String correo) {
        this.correo = correo != null ? correo.trim() : null;
    }
}
