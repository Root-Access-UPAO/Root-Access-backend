package com.example.eventociber.service;

import com.example.eventociber.dto.InscripcionRequest;
import com.example.eventociber.dto.InscripcionResponse;

public interface InscripcionService {
    InscripcionResponse registrarInscripcion(InscripcionRequest request);
}
