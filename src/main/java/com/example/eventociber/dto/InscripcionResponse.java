package com.example.eventociber.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InscripcionResponse {
    private String nombreUsuario;
    private String mensaje;
}
