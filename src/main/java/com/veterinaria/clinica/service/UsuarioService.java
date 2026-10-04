package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dao.RolDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.MascotaDto;
import com.veterinaria.clinica.dto.RegistroClienteDto;
import com.veterinaria.clinica.dto.UsuarioDto;
import com.veterinaria.clinica.exception.DuplicateResourceException;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Mascota;
import com.veterinaria.clinica.model.Rol;
import com.veterinaria.clinica.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar la lógica de Usuarios y Clientes.
 */
@Service
public class UsuarioService {

    private final UsuarioDao usuarioDao;
    private final MascotaDao mascotaDao;
    private final RolDao rolDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioDao usuarioDao, MascotaDao mascotaDao, 
                          RolDao rolDao, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioDao = usuarioDao;
        this.mascotaDao = mascotaDao;
        this.rolDao = rolDao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioDao.findAll();
    }
    
    public List<Usuario> listarPorRol(String rol) {
        return usuarioDao.findAllByRol(rol);
    }

    /**
     * @Transactional garantiza que si falla la creación de la mascota,
     * se hace un rollback automático y no se crea el cliente huérfano.
     */
    @Transactional
    public Usuario registrarClienteConMascota(RegistroClienteDto dto) {
        // 1. Capa de Seguridad anti Escalada de Privilegios (Mass Assignment)
        // Forzamos a que el idRol sea null, ignorando si el usuario intentó inyectar un rol (ej. Admin) en el JSON.
        UsuarioDto clienteSeguro = new UsuarioDto(
                dto.cliente().nombres(), dto.cliente().apellidos(),
                dto.cliente().dni(), dto.cliente().telefono(),
                dto.cliente().direccion(), dto.cliente().email(),
                dto.cliente().password(),
                null // <- ¡CLAVE! Obligamos a que el rol venga nulo para que el sistema asigne el defecto
        );

        // 2. Crear el cliente forzando el rol por defecto "CLIENTE"
        Usuario cliente = crearUsuario(clienteSeguro, "CLIENTE");

        // 3. Crear la mascota si viene en la petición
        if (dto.mascota() != null) {
            crearMascotaVinculada(dto.mascota(), cliente.idUsuario());
        }

        // Devolver el cliente creado (con el JOIN resuelto)
        return usuarioDao.findById(cliente.idUsuario()).orElseThrow();
    }

    @Transactional
    public Usuario crearUsuario(UsuarioDto dto, String nombreRolDefecto) {
        if (usuarioDao.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("El email ya está registrado");
        }
        if (usuarioDao.existsByDni(dto.dni())) {
            throw new DuplicateResourceException("El DNI ya está registrado");
        }

        // Determinar qué rol usar
        Integer idRolFinal;
        if (dto.idRol() != null) {
            idRolFinal = dto.idRol(); // El admin mandó un rol específico
        } else {
            // Buscar el ID del rol por defecto (ej. "CLIENTE")
            Rol rol = rolDao.findByNombre(nombreRolDefecto)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado en BD: " + nombreRolDefecto));
            idRolFinal = rol.idRol();
        }

        Usuario usuario = new Usuario(
                null, dto.nombres(), dto.apellidos(), dto.dni(),
                dto.telefono(), dto.direccion(), dto.email(),
                passwordEncoder.encode(dto.password()), // Siempre guardar hash
                idRolFinal, null, true, null
        );

        Long idGenerado = usuarioDao.save(usuario);
        return usuarioDao.findById(idGenerado).orElseThrow();
    }

    public void cambiarRol(Long idUsuario, Integer idNuevoRol) {
        if (!rolDao.findById(idNuevoRol).isPresent()) {
            throw new ResourceNotFoundException("El rol indicado no existe");
        }
        usuarioDao.updateRol(idUsuario, idNuevoRol);
    }

    public Usuario actualizarUsuario(Long idUsuario, UsuarioDto dto) {
        Usuario existente = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
                
        // Validar si el DNI o Email cambiaron y si ya existen en otro usuario
        if (!existente.email().equals(dto.email()) && usuarioDao.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("El nuevo email ya está registrado");
        }
        if (!existente.dni().equals(dto.dni()) && usuarioDao.existsByDni(dto.dni())) {
            throw new DuplicateResourceException("El nuevo DNI ya está registrado");
        }

        Usuario actualizado = new Usuario(
                idUsuario, dto.nombres(), dto.apellidos(), dto.dni(),
                dto.telefono(), dto.direccion(), dto.email(),
                existente.passwordHash(), existente.idRol(), existente.nombreRol(),
                existente.activo(), existente.fechaRegistro()
        );
        
        usuarioDao.update(actualizado);
        
        // Si además mandó un rol distinto, lo actualizamos (excepto que sea el mismo)
        if (dto.idRol() != null && !dto.idRol().equals(existente.idRol())) {
            cambiarRol(idUsuario, dto.idRol());
        }
        
        return usuarioDao.findById(idUsuario).orElseThrow();
    }

    public void darDeBaja(Long idUsuario) {
        usuarioDao.deactivate(idUsuario);
    }

    private void crearMascotaVinculada(MascotaDto dto, Long idCliente) {
        Mascota mascota = new Mascota(
                null, dto.nombre(), dto.especie(), dto.raza(), dto.sexo(),
                dto.fechaNacimiento(), dto.pesoKg(),
                idCliente, null, true
        );
        mascotaDao.save(mascota);
    }
}
