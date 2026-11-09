package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.AtencionDao;
import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dto.AtencionDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Atencion;
import com.veterinaria.clinica.model.Mascota;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar la atencion clinica y el historial de salud de las mascotas.
 */
@Service
public class AtencionService {

    private final AtencionDao atencionDao;
    private final MascotaDao mascotaDao;
    private final CitaService citaService;

    public AtencionService(AtencionDao atencionDao,
                           MascotaDao mascotaDao,
                           CitaService citaService) {
        this.atencionDao = atencionDao;
        this.mascotaDao = mascotaDao;
        this.citaService = citaService;
    }

    @Transactional
    public Atencion registrarAtencion(AtencionDto dto, Long idVeterinario) {
        Mascota mascota = mascotaDao.findById(dto.idMascota())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + dto.idMascota()));

        Atencion nueva = new Atencion(
                null,
                dto.idCita(),
                dto.idMascota(),
                mascota.nombre(),
                idVeterinario,
                null,
                null,
                dto.diagnostico(),
                dto.tratamiento(),
                dto.observaciones(),
                dto.pesoActualKg()
        );

        Long idGenerado = atencionDao.save(nueva);

        // Si la atencion corresponde a una cita programada, marcarla como atendida
        if (dto.idCita() != null) {
            citaService.marcarCitaAtendida(dto.idCita());
        }

        // Si se registro peso actual, actualizar el peso de la mascota
        if (dto.pesoActualKg() != null) {
            Mascota actualizada = new Mascota(
                    mascota.idMascota(),
                    mascota.nombre(),
                    mascota.especie(),
                    mascota.raza(),
                    mascota.sexo(),
                    mascota.fechaNacimiento(),
                    dto.pesoActualKg(),
                    mascota.idCliente(),
                    mascota.nombreCliente(),
                    mascota.activo()
            );
            mascotaDao.update(mascota.idMascota(), actualizada);
        }

        return atencionDao.findById(idGenerado).orElseThrow();
    }

    public List<Atencion> obtenerHistorialPorMascota(Long idMascota) {
        if (!mascotaDao.existsById(idMascota)) {
            throw new ResourceNotFoundException("Mascota no encontrada con ID: " + idMascota);
        }
        return new ArrayList<>(atencionDao.findByMascotaId(idMascota));
    }

    public List<Atencion> listarPorVeterinario(Long idVeterinario) {
        return new ArrayList<>(atencionDao.findByVeterinarioId(idVeterinario));
    }

    public List<Atencion> listarTodas() {
        return new ArrayList<>(atencionDao.findAll());
    }
}
