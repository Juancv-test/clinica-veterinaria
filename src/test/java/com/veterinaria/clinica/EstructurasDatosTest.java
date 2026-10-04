package com.veterinaria.clinica;

import com.veterinaria.clinica.dto.CitaDto;
import com.veterinaria.clinica.model.Cita;
import com.veterinaria.clinica.model.Servicio;
import com.veterinaria.clinica.service.CitaService;
import com.veterinaria.clinica.service.ServicioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EstructurasDatosTest {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private CitaService citaService;

    @Test
    void testHashMapServiciosCargadoEnO1() {
        // La caché debe haberse inicializado en @PostConstruct
        List<Servicio> activos = servicioService.listarActivos();
        assertFalse(activos.isEmpty(), "La caché de servicios no debe estar vacía");

        // Buscar un ID existente (1 = Consulta General)
        Servicio s = servicioService.obtenerPorId(1);
        assertNotNull(s);
        assertEquals("Consulta General", s.nombre());
    }

    @Autowired
    private com.veterinaria.clinica.dao.MascotaDao mascotaDao;
    
    @Autowired
    private com.veterinaria.clinica.dao.UsuarioDao usuarioDao;

    @Test
    void testColaFifoCitasPendientes() {
        // 1. Preparar datos para evitar FK y NotFound exceptions
        com.veterinaria.clinica.model.Usuario admin = usuarioDao.findByEmail("admin@veterinaria.com")
            .orElseGet(() -> {
                // Evitar null si por algún motivo no hay admin en test
                return new com.veterinaria.clinica.model.Usuario(1L, "A", "A", "111", null, null, "a", "a", 1, null, true, null);
            });
            
        Long idMascota = mascotaDao.save(new com.veterinaria.clinica.model.Mascota(
            null, "TestDog", "Perro", "R", "M", LocalDate.now(), new java.math.BigDecimal("5"), admin.idUsuario(), null, true
        ));

        int sizeInicial = citaService.verColaPendientes().size();

        // 2. Programar cita (ingresa a la cola)
        CitaDto dto = new CitaDto(idMascota, 1, null, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Test cola");
        Cita programada = citaService.programarCita(dto);

        // 3. Verificar que la cola creció y el elemento está al final
        List<Cita> colaActual = citaService.verColaPendientes();
        assertEquals(sizeInicial + 1, colaActual.size());
        assertEquals(programada.idCita(), colaActual.get(colaActual.size() - 1).idCita());

        // 4. Desencolar (cancelar cita)
        citaService.cancelarCita(programada.idCita());
        assertEquals(sizeInicial, citaService.verColaPendientes().size());
    }
}
