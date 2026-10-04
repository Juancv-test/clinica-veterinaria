package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dto.MascotaDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Mascota;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Mascotas.
 * Todo bajo /api/mascotas está protegido y requiere ROLE_ADMIN.
 * (Para este avance 1, delego directamente en el DAO para mantenerlo simple,
 * aunque idealmente pasaría por un MascotaService completo).
 */
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaDao mascotaDao;

    public MascotaController(MascotaDao mascotaDao) {
        this.mascotaDao = mascotaDao;
    }

    @GetMapping
    public List<Mascota> listarMascotas(@RequestParam(required = false) Long clienteId) {
        if (clienteId != null) {
            return mascotaDao.findByClienteId(clienteId);
        }
        return mascotaDao.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mascota crearMascota(@Valid @RequestBody MascotaDto dto) {
        if (dto.idCliente() == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio para el admin");
        }
        
        Mascota nueva = new Mascota(
                null, dto.nombre(), dto.especie(), dto.raza(), dto.sexo(),
                dto.fechaNacimiento(), dto.pesoKg(), dto.idCliente(), null, true
        );
        Long id = mascotaDao.save(nueva);
        return mascotaDao.findById(id).orElseThrow();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMascota(@PathVariable Long id) {
        if (!mascotaDao.existsById(id)) {
            throw new ResourceNotFoundException("Mascota no encontrada");
        }
        mascotaDao.deactivate(id);
    }
}
