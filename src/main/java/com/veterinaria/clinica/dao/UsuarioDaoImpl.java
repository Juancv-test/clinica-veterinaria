package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de usuarios con JdbcTemplate.
 * Siempre hace JOIN con roles para obtener el nombre del rol en una sola consulta.
 */
@Repository
public class UsuarioDaoImpl implements UsuarioDao {

    private final JdbcTemplate jdbc;

    public UsuarioDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper: transforma una fila de la BD en un objeto Usuario
    private final RowMapper<Usuario> usuarioMapper = (rs, rowNum) -> {
        // rs.getTimestamp puede devolver null si la columna es null
        Timestamp ts = rs.getTimestamp("fecha_registro");
        return new Usuario(
                rs.getLong("id_usuario"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("dni"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("id_rol"),
                rs.getString("nombre_rol"),     // del JOIN con roles
                rs.getBoolean("activo"),
                ts != null ? ts.toLocalDateTime() : null
        );
    };

    // SQL base con JOIN para todas las consultas que devuelven usuario completo
    private static final String SELECT_BASE = """
            SELECT u.id_usuario, u.nombres, u.apellidos, u.dni, u.telefono,
                   u.direccion, u.email, u.password_hash, u.id_rol,
                   r.nombre AS nombre_rol, u.activo, u.fecha_registro
            FROM usuarios u
            JOIN roles r ON u.id_rol = r.id_rol
            """;

    @Override
    public Optional<Usuario> findByEmail(String email) {
        String sql = SELECT_BASE + "WHERE u.email = ?";
        List<Usuario> res = jdbc.query(sql, usuarioMapper, email);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        String sql = SELECT_BASE + "WHERE u.id_usuario = ?";
        List<Usuario> res = jdbc.query(sql, usuarioMapper, id);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public List<Usuario> findAll() {
        // ArrayList explícito para demostrar la estructura de datos requerida por el curso
        String sql = SELECT_BASE + "WHERE u.activo = true ORDER BY u.id_usuario";
        return new ArrayList<>(jdbc.query(sql, usuarioMapper));
    }

    @Override
    public List<Usuario> findAllByRol(String rol) {
        String sql = SELECT_BASE + "WHERE r.nombre = ? AND u.activo = true ORDER BY u.id_usuario";
        return new ArrayList<>(jdbc.query(sql, usuarioMapper, rol));
    }

    @Override
    public Long save(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios
                    (nombres, apellidos, dni, telefono, direccion, email, password_hash, id_rol)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        // KeyHolder recibe el ID generado por la secuencia BIGSERIAL
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_usuario"});
            ps.setString(1, usuario.nombres());
            ps.setString(2, usuario.apellidos());
            ps.setString(3, usuario.dni());
            ps.setString(4, usuario.telefono());
            ps.setString(5, usuario.direccion());
            ps.setString(6, usuario.email());
            ps.setString(7, usuario.passwordHash());   // ya hasheado con BCrypt
            ps.setInt(8, usuario.idRol());
            return ps;
        }, keyHolder);

        // Leer la clave por nombre: más robusto con PostgreSQL que getKey()
        Number key = (Number) keyHolder.getKeys().get("id_usuario");
        return key.longValue();
    }

    @Override
    public void update(Usuario usuario) {
        String sql = """
                UPDATE usuarios
                SET nombres = ?, apellidos = ?, dni = ?, telefono = ?,
                    direccion = ?, email = ?
                WHERE id_usuario = ?
                """;
        jdbc.update(sql, usuario.nombres(), usuario.apellidos(), usuario.dni(),
                usuario.telefono(), usuario.direccion(), usuario.email(), usuario.idUsuario());
    }

    @Override
    public void updateRol(Long id, Integer idRol) {
        jdbc.update("UPDATE usuarios SET id_rol = ? WHERE id_usuario = ?", idRol, id);
    }

    @Override
    public void deactivate(Long id) {
        // Baja lógica: no borramos, solo marcamos inactivo
        jdbc.update("UPDATE usuarios SET activo = false WHERE id_usuario = ?", id);
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByDni(String dni) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE dni = ?", Integer.class, dni);
        return count != null && count > 0;
    }
}
