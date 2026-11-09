package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.ServicioDao;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Servicio;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ServicioService {

    private final ServicioDao servicioDao;
    private final HashMap<Integer, Servicio> cacheServicios;

    public ServicioService(ServicioDao servicioDao) {
        this.servicioDao = servicioDao;
        this.cacheServicios = new HashMap<>();
    }

    @PostConstruct
    public void inicializarCache() {
        List<Servicio> todos = servicioDao.findAllIncludingInactive();
        for (Servicio s : todos) {
            cacheServicios.put(s.idServicio(), s);
        }
    }

    public Servicio obtenerPorId(Integer id) {
        Servicio servicio = cacheServicios.get(id);
        if (servicio == null || !servicio.activo()) {
            throw new ResourceNotFoundException("Servicio no encontrado o inactivo con ID: " + id);
        }
        return servicio;
    }

    public List<Servicio> listarActivos() {
        List<Servicio> activos = new ArrayList<>();
        for (Servicio s : cacheServicios.values()) {
            if (s.activo()) {
                activos.add(s);
            }
        }
        return activos;
    }
}
