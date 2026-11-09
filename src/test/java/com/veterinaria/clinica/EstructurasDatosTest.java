package com.veterinaria.clinica;

import com.veterinaria.clinica.dto.AtencionDto;
import com.veterinaria.clinica.dto.CitaDto;
import com.veterinaria.clinica.dto.ConsultaAtencionDto;
import com.veterinaria.clinica.model.Atencion;
import com.veterinaria.clinica.model.Cita;
import com.veterinaria.clinica.model.ConsultaAtencion;
import com.veterinaria.clinica.model.Mascota;
import com.veterinaria.clinica.model.Servicio;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.service.AtencionService;
import com.veterinaria.clinica.service.CitaService;
import com.veterinaria.clinica.service.ConsultaAtencionService;
import com.veterinaria.clinica.service.ServicioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
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

    @Autowired
    private AtencionService atencionService;

    @Autowired
    private ConsultaAtencionService consultaService;

    @Autowired
    private com.veterinaria.clinica.dao.MascotaDao mascotaDao;

    @Autowired
    private com.veterinaria.clinica.dao.UsuarioDao usuarioDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Utiliza la cuenta del cliente Juan Campos para los testeos
     * y garantiza que su contraseña sea siempre 123456.
     */
    private Usuario obtenerClienteTest() {
        return usuarioDao.findByEmail("juancampos@gmail.com")
                .map(u -> {
                    // Asegurar que la contraseña sea 123456
                    usuarioDao.updatePassword(u.idUsuario(), passwordEncoder.encode("123456"));
                    return u;
                })
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario(
                            null, "Juan", "Campos", "30780984", "919097235", "Av. Principal 123",
                            "juancampos@gmail.com", passwordEncoder.encode("123456"), 2, "CLIENTE", true, null
                    );
                    Long id = usuarioDao.save(nuevo);
                    return usuarioDao.findById(id).orElseThrow();
                });
    }

    @Test
    void testHashMapServiciosCargadoEnO1() {
        List<Servicio> activos = servicioService.listarActivos();
        assertFalse(activos.isEmpty(), "La cache de servicios no debe estar vacia");

        Servicio s = servicioService.obtenerPorId(1);
        assertNotNull(s);
        assertEquals("Consulta General", s.nombre());
    }

    @Test
    void testColaFifoCitasPendientes() {
        Usuario cliente = obtenerClienteTest();

        Long idMascota = mascotaDao.save(new Mascota(
                null, "TestDogCola", "Perro", "RazaTest", "M", LocalDate.now().minusYears(1), new BigDecimal("5"), cliente.idUsuario(), null, true
        ));

        int sizeInicial = citaService.verColaPendientes().size();

        CitaDto dto = new CitaDto(idMascota, 1, null, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Test cola");
        Cita programada = citaService.programarCita(dto);

        List<Cita> colaActual = citaService.verColaPendientes();
        assertEquals(sizeInicial + 1, colaActual.size());
        assertEquals(programada.idCita(), colaActual.get(colaActual.size() - 1).idCita());

        citaService.cancelarCita(programada.idCita());
        assertEquals(sizeInicial, citaService.verColaPendientes().size());

        // Limpiar registro de prueba para no dejar datos residuales
        jdbc.update("DELETE FROM citas WHERE id_cita = ?", programada.idCita());
        jdbc.update("DELETE FROM mascotas WHERE id_mascota = ?", idMascota);
    }

    @Test
    void testEmergenciaPrioridadAlFrenteDeCola() {
        Usuario cliente = obtenerClienteTest();

        Long idMascota = mascotaDao.save(new Mascota(
                null, "TestEmergenciaPrioridad", "Canino", "Mestizo", "M", LocalDate.now().minusYears(2), new BigDecimal("8"), cliente.idUsuario(), null, true
        ));

        // Programar cita regular
        CitaDto regularDto = new CitaDto(idMascota, 1, null, LocalDate.now().plusDays(2), LocalTime.of(11, 0), "Regular");
        Cita regular = citaService.programarCita(regularDto);

        // Programar cita de emergencia
        CitaDto emergDto = new CitaDto(idMascota, 1, null, LocalDate.now().plusDays(2), LocalTime.of(12, 0), "Urgente", true);
        Cita emergencia = citaService.programarCita(emergDto);

        List<Cita> colaActual = citaService.verColaPendientes();
        assertFalse(colaActual.isEmpty());
        // La emergencia debe haber entrado al inicio de la cola
        assertEquals(emergencia.idCita(), colaActual.get(0).idCita(), "La emergencia debe situarse al inicio de la cola");

        // Limpiar de la cola
        citaService.cancelarCita(emergencia.idCita());
        citaService.cancelarCita(regular.idCita());

        // Limpiar de la BD para no dejar residuos
        jdbc.update("DELETE FROM citas WHERE id_cita IN (?, ?)", emergencia.idCita(), regular.idCita());
        jdbc.update("DELETE FROM mascotas WHERE id_mascota = ?", idMascota);
    }

    @Test
    void testRegistrarAtencionYHistorialClinico() {
        Usuario cliente = obtenerClienteTest();
        Usuario admin = usuarioDao.findByEmail("admin@veterinaria.com").orElseThrow();

        Long idMascota = mascotaDao.save(new Mascota(
                null, "TestHistorialPet", "Felino", "Siames", "H", LocalDate.now().minusYears(1), new BigDecimal("3.5"), cliente.idUsuario(), null, true
        ));

        AtencionDto atencionDto = new AtencionDto(
                null, idMascota, "Gastroenteritis leve", "Hidratacion y dieta blanda", "Revisar en 48 horas", new BigDecimal("3.6")
        );

        Atencion registrada = atencionService.registrarAtencion(atencionDto, admin.idUsuario());
        assertNotNull(registrada.idAtencion());
        assertEquals("Gastroenteritis leve", registrada.diagnostico());

        List<Atencion> historial = atencionService.obtenerHistorialPorMascota(idMascota);
        assertFalse(historial.isEmpty());
        assertEquals("Gastroenteritis leve", historial.get(0).diagnostico());

        // Limpiar registros de prueba de la BD
        jdbc.update("DELETE FROM atenciones WHERE id_atencion = ?", registrada.idAtencion());
        jdbc.update("DELETE FROM mascotas WHERE id_mascota = ?", idMascota);
    }

    @Test
    void testConsultaAtencionAlCliente() {
        Usuario cliente = obtenerClienteTest();
        Usuario admin = usuarioDao.findByEmail("admin@veterinaria.com").orElseThrow();

        ConsultaAtencionDto consultaDto = new ConsultaAtencionDto("Horarios de atencion", "Que dias atienden vacunacion?");
        ConsultaAtencion creada = consultaService.registrarConsulta(cliente.idUsuario(), consultaDto);
        assertNotNull(creada.idConsulta());
        assertEquals("PENDIENTE", creada.estado());

        consultaService.responderConsulta(creada.idConsulta(), admin.idUsuario(), "Atendemos de lunes a sabado de 8am a 7pm.");

        List<ConsultaAtencion> lista = consultaService.listarPorCliente(cliente.idUsuario());
        ConsultaAtencion respondida = lista.stream()
                .filter(c -> c.idConsulta().equals(creada.idConsulta()))
                .findFirst()
                .orElseThrow();

        assertEquals("RESPONDIDA", respondida.estado());
        assertEquals("Atendemos de lunes a sabado de 8am a 7pm.", respondida.respuesta());

        // Limpiar consulta de prueba de la BD
        jdbc.update("DELETE FROM consultas_atencion WHERE id_consulta = ?", creada.idConsulta());
    }

    @Autowired
    private com.veterinaria.clinica.service.EmergenciaService emergenciaService;

    @Test
    void testIngresoEmergenciaExpressYRegularizacion() {
        Usuario cliente = obtenerClienteTest();

        // 1. Ingreso express de emergencia sin registrar cliente previo
        var ingresoDto = new com.veterinaria.clinica.dto.IngresoEmergenciaExpressDto(
                "Canino",
                "Perro atropellado Av Grau",
                "Shock y fractura expuesta",
                "Rescatista Luis 987654321",
                "Mestizo",
                "M"
        );

        Cita citaUrgente = emergenciaService.registrarIngresoExpress(ingresoDto);
        assertNotNull(citaUrgente.idCita());
        assertTrue(citaUrgente.esEmergencia());

        // Verificar que la cita esta en el tope de la cola (indice 0 del Deque)
        List<Cita> cola = citaService.verColaPendientes();
        assertEquals(citaUrgente.idCita(), cola.get(0).idCita());

        // Verificar que la mascota es temporal y no tiene cliente aun
        Mascota mascotaTemp = mascotaDao.findById(citaUrgente.idMascota()).orElseThrow();
        assertTrue(mascotaTemp.esTemporal());
        assertNull(mascotaTemp.idCliente());

        // 2. Regularizar la mascota asignandole dueno definitivo (el cliente Juan Campos)
        var regDto = new com.veterinaria.clinica.dto.RegularizarEmergenciaDto(
                mascotaTemp.idMascota(),
                "FirulaisTest",
                "Mestizo",
                "M",
                LocalDate.now().minusYears(3),
                cliente.idUsuario(),
                null
        );

        Mascota mascotaRegularizada = emergenciaService.regularizarMascota(regDto);
        assertFalse(mascotaRegularizada.esTemporal());
        assertEquals(cliente.idUsuario(), mascotaRegularizada.idCliente());

        // Limpiar registros de prueba de la BD
        citaService.cancelarCita(citaUrgente.idCita());
        jdbc.update("DELETE FROM citas WHERE id_cita = ?", citaUrgente.idCita());
        jdbc.update("DELETE FROM mascotas WHERE id_mascota = ?", mascotaTemp.idMascota());
    }

    @Test
    void testConflictoHorarioYAsignacionRapida() {
        Usuario cliente = obtenerClienteTest();
        Usuario vet = usuarioDao.findByEmail("veterinario@veterinaria.com").orElseThrow();

        Long idMascota1 = mascotaDao.save(new Mascota(null, "PetTest1", "Perro", "Mestizo", "M", LocalDate.now().minusYears(1), new BigDecimal("6"), cliente.idUsuario(), null, true));
        Long idMascota2 = mascotaDao.save(new Mascota(null, "PetTest2", "Gato", "Siames", "H", LocalDate.now().minusYears(1), new BigDecimal("3"), cliente.idUsuario(), null, true));

        LocalDate fechaPrueba = LocalDate.now().plusDays(10);
        LocalTime horaPrueba = LocalTime.of(16, 0);

        // 1. Programar primera cita
        CitaDto cita1 = new CitaDto(idMascota1, 1, null, fechaPrueba, horaPrueba, "Cita 1");
        Cita creada1 = citaService.programarCita(cita1);
        assertNotNull(creada1.idCita());

        // 2. Intentar programar segunda cita al mismo horario exacto (debe lanzar excepción por conflicto)
        CitaDto cita2 = new CitaDto(idMascota2, 1, null, fechaPrueba, horaPrueba, "Cita 2");
        assertThrows(com.veterinaria.clinica.exception.DuplicateResourceException.class, () -> {
            citaService.programarCita(cita2);
        }, "No debe permitir citas solapadas en la misma fecha y hora");

        // 3. Probar asignación rápida de veterinario a la cita 1
        Cita conVet = citaService.asignarVeterinario(creada1.idCita(), vet.idUsuario());
        assertEquals(vet.idUsuario(), conVet.idVeterinario());

        // Limpiar registros
        citaService.cancelarCita(creada1.idCita());
        jdbc.update("DELETE FROM citas WHERE id_cita = ?", creada1.idCita());
        jdbc.update("DELETE FROM mascotas WHERE id_mascota IN (?, ?)", idMascota1, idMascota2);
    }
}
