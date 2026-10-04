package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.CitaDao;
import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dto.CitaDto;
import com.veterinaria.clinica.exception.DuplicateResourceException;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Cita;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Servicio para gestionar las citas médicas.
 * Implementa el requisito del curso: Cola (Queue) FIFO para las citas pendientes.
 */
@Service
public class CitaService {

    private final CitaDao citaDao;
    private final MascotaDao mascotaDao;
    private final ServicioService servicioService;

    // ESTRUCTURA DE DATOS 3: Queue (ArrayDeque) como cola FIFO.
    private final Queue<Cita> colaPendientes;

    public CitaService(CitaDao citaDao, MascotaDao mascotaDao, ServicioService servicioService) {
        this.citaDao = citaDao;
        this.mascotaDao = mascotaDao;
        this.servicioService = servicioService;
        this.colaPendientes = new ArrayDeque<>();
    }

    /**
     * Reconstruye la cola de citas al arrancar la aplicación.
     */
    @PostConstruct
    public synchronized void inicializarCola() {
        List<Cita> programadas = citaDao.findProgramadasOrdenadas();
        colaPendientes.addAll(programadas);
        System.out.println("✅ [CitaService] Cola inicializada con " + colaPendientes.size() + " citas pendientes.");
    }

    public List<Cita> buscarCitas(LocalDate fecha, String estado) {
        // ESTRUCTURA DE DATOS 1: ArrayList
        return new ArrayList<>(citaDao.findAll(fecha, estado));
    }

    /** Devuelve una copia de la cola actual */
    public synchronized List<Cita> verColaPendientes() {
        return new ArrayList<>(colaPendientes);
    }

    /**
     * Programa una nueva cita, valida conflictos, guarda en BD y la encola.
     * El método es synchronized para ser seguro ante peticiones concurrentes.
     */
    public synchronized Cita programarCita(CitaDto dto) {
        // Validar mascota
        if (!mascotaDao.existsById(dto.idMascota())) {
            throw new ResourceNotFoundException("Mascota no encontrada con ID: " + dto.idMascota());
        }

        // Validar servicio (usa la caché en memoria del ServicioService)
        servicioService.obtenerPorId(dto.idServicio());

        // Validar conflicto de horario
        if (citaDao.existsConflicto(dto.idMascota(), dto.fecha(), dto.hora())) {
            throw new DuplicateResourceException("La mascota ya tiene una cita programada en esa fecha y hora.");
        }

        Cita nueva = new Cita(
                null, dto.idMascota(), null, dto.idServicio(), null,
                dto.idVeterinario(), null, dto.fecha(), dto.hora(), "PROGRAMADA", dto.motivo()
        );

        Long idGenerado = citaDao.save(nueva);
        
        // Recuperar la cita completa con todos los JOINs resueltos para meterla a la cola
        Cita citaCompleta = citaDao.findById(idGenerado)
                .orElseThrow(() -> new RuntimeException("Error al recuperar la cita guardada"));

        colaPendientes.offer(citaCompleta); // Encolar al final
        return citaCompleta;
    }

    /**
     * Cancela una cita: la actualiza en la BD y la remueve de la cola en memoria.
     */
    public synchronized void cancelarCita(Long idCita) {
        Cita cita = citaDao.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        
        if (!"PROGRAMADA".equals(cita.estado())) {
            throw new IllegalStateException("Solo se pueden cancelar citas PROGRAMADAS");
        }

        citaDao.cancel(idCita);
        
        // Remover de la cola usando un predicado para comparar por ID
        colaPendientes.removeIf(c -> c.idCita().equals(idCita));
    }
}
