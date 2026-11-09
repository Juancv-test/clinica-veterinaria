package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.CitaDao;
import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dao.ServicioDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.IngresoEmergenciaExpressDto;
import com.veterinaria.clinica.dto.RegularizarEmergenciaDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Cita;
import com.veterinaria.clinica.model.Mascota;
import com.veterinaria.clinica.model.Servicio;
import com.veterinaria.clinica.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio para el triage y gestion de atencion de emergencias veterinarias express.
 * Permite la atencion medica inmediata de pacientes sin registro previo,
 * facilitando la regularizacion posterior de datos del dueno.
 */
@Service
public class EmergenciaService {

    private final MascotaDao mascotaDao;
    private final CitaDao citaDao;
    private final CitaService citaService;
    private final ServicioDao servicioDao;
    private final UsuarioService usuarioService;
    private final UsuarioDao usuarioDao;

    public EmergenciaService(MascotaDao mascotaDao,
                             CitaDao citaDao,
                             CitaService citaService,
                             ServicioDao servicioDao,
                             UsuarioService usuarioService,
                             UsuarioDao usuarioDao) {
        this.mascotaDao = mascotaDao;
        this.citaDao = citaDao;
        this.citaService = citaService;
        this.servicioDao = servicioDao;
        this.usuarioService = usuarioService;
        this.usuarioDao = usuarioDao;
    }

    /**
     * Registra un ingreso de emergencia express en segundos.
     * Crea un paciente temporal y una cita prioritaria al frente de la cola de espera.
     */
    @Transactional
    public Cita registrarIngresoExpress(IngresoEmergenciaExpressDto dto) {
        String nombreTemp = (dto.nombreTemporal() != null && !dto.nombreTemporal().isBlank())
                ? dto.nombreTemporal().trim()
                : "Paciente Urgente NN";

        String raza = (dto.raza() != null && !dto.raza().isBlank()) ? dto.raza().trim() : "No especificada";
        String sexo = (dto.sexo() != null && (dto.sexo().equalsIgnoreCase("M") || dto.sexo().equalsIgnoreCase("H")))
                ? dto.sexo().toUpperCase()
                : "M";

        // 1. Crear mascota temporal sin id_cliente
        Mascota mascotaTemporal = new Mascota(
                null,
                nombreTemp,
                dto.especie().trim(),
                raza,
                sexo,
                null,
                null,
                null, // id_cliente null temporalmente
                null,
                true,
                true  // es_temporal = true
        );

        Long idMascota = mascotaDao.save(mascotaTemporal);

        // 2. Buscar servicio de consulta/emergencia por defecto
        List<Servicio> servicios = servicioDao.findAll();
        Servicio servicioUrgencia = servicios.stream()
                .filter(s -> s.nombre().toLowerCase().contains("emerg") || s.nombre().toLowerCase().contains("cirug") || s.nombre().toLowerCase().contains("consulta"))
                .findFirst()
                .orElseGet(() -> servicios.isEmpty() ? null : servicios.get(0));

        Integer idServicio = servicioUrgencia != null ? servicioUrgencia.idServicio() : 1;

        // 3. Crear cita de emergencia inmediata
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now().withNano(0);
        String motivo = "EMERGENCIA URGENTE: " + dto.motivoUrgencia().trim();

        Cita citaEmergencia = new Cita(
                null,
                idMascota,
                nombreTemp,
                idServicio,
                servicioUrgencia != null ? servicioUrgencia.nombre() : "Atencion de Emergencia",
                null,
                null,
                hoy,
                ahora,
                "PROGRAMADA",
                motivo,
                true, // esEmergencia = true
                dto.contactoEmergencia() != null ? dto.contactoEmergencia().trim() : null
        );

        Long idCita = citaDao.save(citaEmergencia);
        Cita citaGuardada = citaDao.findById(idCita).orElseThrow();

        citaService.encolarPrioridadEmergencia(citaGuardada);

        return citaGuardada;
    }

    /**
     * Regulariza los datos de la mascota y la asocia a un dueno (cliente existente o nuevo).
     */
    @Transactional
    public Mascota regularizarMascota(RegularizarEmergenciaDto dto) {
        Mascota mascota = mascotaDao.findById(dto.idMascota())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + dto.idMascota()));

        Long idClienteFinal;
        if (dto.idClienteExistente() != null) {
            if (!usuarioDao.findById(dto.idClienteExistente()).isPresent()) {
                throw new ResourceNotFoundException("Cliente existente no encontrado con ID: " + dto.idClienteExistente());
            }
            idClienteFinal = dto.idClienteExistente();
        } else if (dto.nuevoCliente() != null) {
            Usuario creado = usuarioService.crearUsuario(dto.nuevoCliente(), "CLIENTE");
            idClienteFinal = creado.idUsuario();
        } else {
            throw new IllegalArgumentException("Debe seleccionar un cliente existente o ingresar los datos para crear uno nuevo");
        }

        String nombreDefinitivo = (dto.nombreDefinitivo() != null && !dto.nombreDefinitivo().isBlank())
                ? dto.nombreDefinitivo().trim()
                : mascota.nombre();

        String raza = (dto.raza() != null && !dto.raza().isBlank()) ? dto.raza().trim() : mascota.raza();
        String sexo = (dto.sexo() != null && !dto.sexo().isBlank()) ? dto.sexo().trim() : mascota.sexo();

        mascotaDao.regularizar(
                dto.idMascota(),
                idClienteFinal,
                nombreDefinitivo,
                raza,
                sexo,
                dto.fechaNacimiento()
        );

        return mascotaDao.findById(dto.idMascota()).orElseThrow();
    }

    public List<Mascota> listarTemporalesPendientes() {
        return mascotaDao.findTemporalesPendientes();
    }
}
