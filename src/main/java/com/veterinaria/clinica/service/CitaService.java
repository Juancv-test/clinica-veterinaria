package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.CitaDao;
import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.CitaDto;
import com.veterinaria.clinica.exception.DuplicateResourceException;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Cita;
import com.veterinaria.clinica.model.Usuario;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Service
public class CitaService {

    private final CitaDao citaDao;
    private final MascotaDao mascotaDao;
    private final UsuarioDao usuarioDao;
    private final ServicioService servicioService;
    private final Deque<Cita> colaPendientes;

    public CitaService(CitaDao citaDao, MascotaDao mascotaDao, UsuarioDao usuarioDao, ServicioService servicioService) {
        this.citaDao = citaDao;
        this.mascotaDao = mascotaDao;
        this.usuarioDao = usuarioDao;
        this.servicioService = servicioService;
        this.colaPendientes = new ArrayDeque<>();
    }

    @PostConstruct
    public synchronized void inicializarCola() {
        colaPendientes.clear();
        List<Cita> programadas = citaDao.findProgramadasOrdenadas();
        for (Cita c : programadas) {
            if (Boolean.TRUE.equals(c.esEmergencia())) {
                colaPendientes.addFirst(c);
            } else {
                colaPendientes.addLast(c);
            }
        }
    }

    public List<Cita> buscarCitas(LocalDate fecha, String estado) {
        return new ArrayList<>(citaDao.findAll(fecha, estado));
    }

    public List<Cita> listarPorVeterinario(Long idVeterinario) {
        return new ArrayList<>(citaDao.findByVeterinario(idVeterinario));
    }

    public List<Cita> listarPorCliente(Long idCliente) {
        return new ArrayList<>(citaDao.findByCliente(idCliente));
    }

    public synchronized List<Cita> verColaPendientes() {
        return new ArrayList<>(colaPendientes);
    }

    /**
     * Programa una nueva cita, valida conflictos, guarda en BD y la encola.
     * Si es emergencia, entra al frente de la cola de atencion.
     */
    public synchronized Cita programarCita(CitaDto dto) {
        if (!mascotaDao.existsById(dto.idMascota())) {
            throw new ResourceNotFoundException("Mascota no encontrada con ID: " + dto.idMascota());
        }

        servicioService.obtenerPorId(dto.idServicio());

        boolean esEmergencia = Boolean.TRUE.equals(dto.esEmergencia());

        // Control de solapamiento de horarios (solo aplica para citas regulares)
        if (!esEmergencia) {
            if (citaDao.existsConflicto(dto.idMascota(), dto.fecha(), dto.hora())) {
                throw new DuplicateResourceException("La mascota ya tiene una cita programada en esa fecha y hora.");
            }
            if (citaDao.existsConflictoHorario(dto.fecha(), dto.hora(), null, dto.idVeterinario())) {
                throw new DuplicateResourceException("El horario seleccionado (" + dto.fecha() + " a las " + dto.hora() + ") ya se encuentra reservado. Por favor, seleccione otro horario.");
            }
        }

        Cita nueva = new Cita(
                null, dto.idMascota(), null, dto.idServicio(), null,
                dto.idVeterinario(), null, dto.fecha(), dto.hora(), "PROGRAMADA", dto.motivo(),
                esEmergencia
        );

        Long idGenerado = citaDao.save(nueva);

        Cita citaCompleta = citaDao.findById(idGenerado)
                .orElseThrow(() -> new RuntimeException("Error al recuperar la cita guardada"));

        if (esEmergencia) {
            colaPendientes.addFirst(citaCompleta);
        } else {
            colaPendientes.addLast(citaCompleta);
        }

        return citaCompleta;
    }

    public synchronized void encolarPrioridadEmergencia(Cita cita) {
        colaPendientes.addFirst(cita);
    }

    /**
     * Actualiza los datos de una cita (reprogramar/editar).
     */
    public synchronized Cita actualizarCita(Long idCita, CitaDto dto) {
        Cita existente = citaDao.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + idCita));

        if (!"PROGRAMADA".equals(existente.estado())) {
            throw new IllegalStateException("Solo se pueden editar citas en estado PROGRAMADA");
        }

        if (!mascotaDao.existsById(dto.idMascota())) {
            throw new ResourceNotFoundException("Mascota no encontrada con ID: " + dto.idMascota());
        }

        servicioService.obtenerPorId(dto.idServicio());

        boolean cambioHorario = !existente.fecha().equals(dto.fecha()) || !existente.hora().equals(dto.hora());
        if (cambioHorario && citaDao.existsConflicto(dto.idMascota(), dto.fecha(), dto.hora())) {
            throw new DuplicateResourceException("La mascota ya tiene otra cita en esa fecha y hora.");
        }
        if (cambioHorario && citaDao.existsConflictoHorario(dto.fecha(), dto.hora(), idCita, dto.idVeterinario())) {
            throw new DuplicateResourceException("El horario seleccionado (" + dto.fecha() + " a las " + dto.hora() + ") ya se encuentra reservado por otra cita.");
        }

        boolean esEmergencia = dto.esEmergencia() != null ? dto.esEmergencia() : Boolean.TRUE.equals(existente.esEmergencia());

        Cita modificada = new Cita(
                idCita, dto.idMascota(), null, dto.idServicio(), null,
                dto.idVeterinario(), null, dto.fecha(), dto.hora(), existente.estado(), dto.motivo(),
                esEmergencia
        );

        citaDao.update(idCita, modificada);

        // Actualizar en cola si esta presente
        colaPendientes.removeIf(c -> c.idCita().equals(idCita));
        Cita refrescada = citaDao.findById(idCita).orElseThrow();
        if (esEmergencia) {
            colaPendientes.addFirst(refrescada);
        } else {
            colaPendientes.addLast(refrescada);
        }

        return refrescada;
    }

    /**
     * Asigna o reasigna de manera rápida un veterinario a una cita pendiente desde la recepción.
     */
    public synchronized Cita asignarVeterinario(Long idCita, Long idVeterinario) {
        Cita existente = citaDao.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + idCita));

        if (!"PROGRAMADA".equals(existente.estado())) {
            throw new IllegalStateException("Solo se puede asignar veterinario a citas en estado PROGRAMADA");
        }

        if (idVeterinario != null) {
            Usuario vet = usuarioDao.findById(idVeterinario)
                    .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + idVeterinario));
            if (!"VETERINARIO".equalsIgnoreCase(vet.nombreRol())) {
                throw new IllegalArgumentException("El usuario seleccionado no tiene el rol de VETERINARIO");
            }
            if (citaDao.existsConflictoHorario(existente.fecha(), existente.hora(), idCita, idVeterinario)) {
                throw new DuplicateResourceException("El veterinario " + vet.nombres() + " " + vet.apellidos() + " ya tiene otra cita en esa fecha y hora.");
            }
        }

        citaDao.updateVeterinario(idCita, idVeterinario);

        // Refrescar en cola de atención en memoria
        colaPendientes.removeIf(c -> c.idCita().equals(idCita));
        Cita refrescada = citaDao.findById(idCita).orElseThrow();
        if (Boolean.TRUE.equals(refrescada.esEmergencia())) {
            colaPendientes.addFirst(refrescada);
        } else {
            colaPendientes.addLast(refrescada);
        }

        return refrescada;
    }

    public synchronized void cancelarCita(Long idCita) {
        Cita cita = citaDao.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        if (!"PROGRAMADA".equals(cita.estado())) {
            throw new IllegalStateException("Solo se pueden cancelar citas PROGRAMADAS");
        }

        citaDao.cancel(idCita);
        colaPendientes.removeIf(c -> c.idCita().equals(idCita));
    }

    public synchronized void marcarCitaAtendida(Long idCita) {
        citaDao.marcarAtendida(idCita);
        colaPendientes.removeIf(c -> c.idCita().equals(idCita));
    }
}
