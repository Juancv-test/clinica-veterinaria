package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.MascotaDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Mascota;
import com.veterinaria.clinica.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de Mascotas.
 */
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaDao mascotaDao;
    private final UsuarioDao usuarioDao;

    public MascotaController(MascotaDao mascotaDao, UsuarioDao usuarioDao) {
        this.mascotaDao = mascotaDao;
        this.usuarioDao = usuarioDao;
    }

    @GetMapping
    public List<Mascota> listarMascotas(@RequestParam(required = false) Long clienteId) {
        if (clienteId != null) {
            return mascotaDao.findByClienteId(clienteId);
        }
        return mascotaDao.findAll();
    }

    @GetMapping("/mis-mascotas")
    public List<Mascota> listarMisMascotas(Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        return mascotaDao.findByClienteId(usuario.idUsuario());
    }

    @GetMapping("/{id}")
    public Mascota obtenerPorId(@PathVariable Long id) {
        return mascotaDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mascota crearMascota(@Valid @RequestBody MascotaDto dto, Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);

        Long idClienteFinal;
        if ("CLIENTE".equalsIgnoreCase(usuario.nombreRol())) {
            idClienteFinal = usuario.idUsuario();
        } else {
            if (dto.idCliente() == null) {
                throw new IllegalArgumentException("El ID del cliente es obligatorio para el personal");
            }
            idClienteFinal = dto.idCliente();
        }

        if (!usuarioDao.findById(idClienteFinal).isPresent()) {
            throw new ResourceNotFoundException("Cliente no encontrado con ID: " + idClienteFinal);
        }

        Mascota nueva = new Mascota(
                null, dto.nombre(), dto.especie(), dto.raza(), dto.sexo(),
                dto.fechaNacimiento(), dto.pesoKg(), idClienteFinal, null, true
        );
        Long id = mascotaDao.save(nueva);
        return mascotaDao.findById(id).orElseThrow();
    }

    @PutMapping("/{id}")
    public Mascota actualizarMascota(@PathVariable Long id, @Valid @RequestBody MascotaDto dto, Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        Mascota existente = mascotaDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + id));

        boolean esAdminORecep = "ADMIN".equalsIgnoreCase(usuario.nombreRol()) || "RECEPCIONISTA".equalsIgnoreCase(usuario.nombreRol());
        if (!esAdminORecep) {
            if (existente.idCliente() == null || !existente.idCliente().equals(usuario.idUsuario())) {
                throw new AccessDeniedException("No tienes permiso para modificar esta mascota");
            }
        }

        String nombreFinal = (dto.nombre() != null && !dto.nombre().isBlank()) ? dto.nombre().trim() : existente.nombre();
        String especieFinal = (dto.especie() != null && !dto.especie().isBlank()) ? dto.especie().trim() : existente.especie();
        String razaFinal = (dto.raza() != null && !dto.raza().isBlank()) ? dto.raza().trim() : null;
        String sexoFinal = (dto.sexo() != null && !dto.sexo().isBlank()) ? dto.sexo() : existente.sexo();
        LocalDate fechaFinal = dto.fechaNacimiento();
        BigDecimal pesoFinal = dto.pesoKg();

        Mascota actualizada = new Mascota(
                id, nombreFinal, especieFinal, razaFinal, sexoFinal,
                fechaFinal, pesoFinal, existente.idCliente(), existente.nombreCliente(), existente.activo()
        );

        mascotaDao.update(id, actualizada);
        return mascotaDao.findById(id).orElseThrow();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMascota(@PathVariable Long id, Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        Mascota existente = mascotaDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + id));

        boolean esPersonal = "ADMIN".equalsIgnoreCase(usuario.nombreRol()) || "RECEPCIONISTA".equalsIgnoreCase(usuario.nombreRol());
        if (!esPersonal) {
            if (existente.idCliente() == null || !existente.idCliente().equals(usuario.idUsuario())) {
                throw new AccessDeniedException("No tienes permiso para eliminar esta mascota");
            }
        }

        mascotaDao.deactivate(id);
    }

    private Usuario obtenerUsuarioEnSesion(Authentication auth) {
        return usuarioDao.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no autenticado"));
    }
}
