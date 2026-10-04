package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.ServicioDao;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Servicio;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Servicio para gestionar el catálogo de servicios veterinarios.
 * Implementa el requisito del curso: HashMap como caché en memoria.
 */
@Service
public class ServicioService {

    private final ServicioDao servicioDao;

    // ESTRUCTURA DE DATOS 2: HashMap para búsqueda en O(1)
    private final HashMap<Integer, Servicio> cacheServicios;

    public ServicioService(ServicioDao servicioDao) {
        this.servicioDao = servicioDao;
        this.cacheServicios = new HashMap<>();
    }

    /**
     * @PostConstruct se ejecuta automáticamente al arrancar la aplicación
     * después de inyectar las dependencias. Carga la caché desde la BD.
     */
    @PostConstruct
    public void inicializarCache() {
        List<Servicio> todos = servicioDao.findAllIncludingInactive();
        for (Servicio s : todos) {
            cacheServicios.put(s.idServicio(), s);
        }
        System.out.println("✅ [ServicioService] Caché cargada con " + cacheServicios.size() + " servicios en HashMap.");
    }

    /**
     * Búsqueda en O(1) usando el HashMap en lugar de consultar la BD.
     */
    public Servicio obtenerPorId(Integer id) {
        Servicio servicio = cacheServicios.get(id);
        if (servicio == null || !servicio.activo()) {
            throw new ResourceNotFoundException("Servicio no encontrado o inactivo con ID: " + id);
        }
        return servicio;
    }

    /**
     * Devuelve la lista de servicios activos para el frontend.
     */
    public List<Servicio> listarActivos() {
        // ESTRUCTURA DE DATOS 1: ArrayList
        List<Servicio> activos = new ArrayList<>();
        for (Servicio s : cacheServicios.values()) {
            if (s.activo()) {
                activos.add(s);
            }
        }
        return activos;
    }
}
