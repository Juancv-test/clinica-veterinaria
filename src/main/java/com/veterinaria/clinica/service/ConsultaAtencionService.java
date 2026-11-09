package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.ConsultaAtencionDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.ConsultaAtencionDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.ConsultaAtencion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar la atencion al cliente y resolucion de consultas.
 */
@Service
public class ConsultaAtencionService {

    private final ConsultaAtencionDao consultaDao;
    private final UsuarioDao usuarioDao;

    public ConsultaAtencionService(ConsultaAtencionDao consultaDao, UsuarioDao usuarioDao) {
        this.consultaDao = consultaDao;
        this.usuarioDao = usuarioDao;
    }

    public ConsultaAtencion registrarConsultaPublica(com.veterinaria.clinica.dto.MensajeContactoDto dto) {
        ConsultaAtencion nueva = new ConsultaAtencion(
                null,
                null,
                dto.nombre(),
                dto.correo(),
                "Contacto Web: " + (dto.nombre().length() > 30 ? dto.nombre().substring(0, 30) : dto.nombre()),
                dto.mensaje(),
                null,
                null,
                null,
                "PENDIENTE",
                null,
                null
        );

        Long id = consultaDao.save(nueva);
        return consultaDao.findById(id).orElseThrow();
    }

    public ConsultaAtencion registrarConsulta(Long idCliente, ConsultaAtencionDto dto) {
        if (!usuarioDao.findById(idCliente).isPresent()) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }

        ConsultaAtencion nueva = new ConsultaAtencion(
                null,
                idCliente,
                null,
                null,
                dto.asunto(),
                dto.mensaje(),
                null,
                null,
                null,
                "PENDIENTE",
                null,
                null
        );

        Long id = consultaDao.save(nueva);
        return consultaDao.findById(id).orElseThrow();
    }

    public void responderConsulta(Long idConsulta, Long idRecepcionista, String respuesta) {
        if (!consultaDao.findById(idConsulta).isPresent()) {
            throw new ResourceNotFoundException("Consulta no encontrada con ID: " + idConsulta);
        }
        consultaDao.responder(idConsulta, respuesta, idRecepcionista);
    }

    public List<ConsultaAtencion> listarTodas(String estado) {
        return new ArrayList<>(consultaDao.findAll(estado));
    }

    public List<ConsultaAtencion> listarPorCliente(Long idCliente) {
        return new ArrayList<>(consultaDao.findByClienteId(idCliente));
    }
}
