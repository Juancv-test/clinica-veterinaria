package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Rol;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del acceso a datos de roles.
 * Usa JdbcTemplate con consultas parametrizadas (sin concatenar SQL).
 *
 * @Repository: Spring lo detecta automáticamente y lo registra como bean.
 */
@Repository
public class RolDaoImpl implements RolDao {

    private final JdbcTemplate jdbc;

    // Inyección de dependencias por constructor (recomendado sobre @Autowired en campo)
    public RolDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper: convierte cada fila del ResultSet en un objeto Rol
    private final RowMapper<Rol> rolMapper = (rs, rowNum) ->
            new Rol(rs.getInt("id_rol"), rs.getString("nombre"));

    @Override
    public Optional<Rol> findByNombre(String nombre) {
        String sql = "SELECT id_rol, nombre FROM roles WHERE nombre = ?";
        List<Rol> resultado = jdbc.query(sql, rolMapper, nombre);
        // Si la lista está vacía devolvemos Optional.empty(), evitando la excepción de queryForObject
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public Optional<Rol> findById(Integer id) {
        String sql = "SELECT id_rol, nombre FROM roles WHERE id_rol = ?";
        List<Rol> resultado = jdbc.query(sql, rolMapper, id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<Rol> findAll() {
        String sql = "SELECT id_rol, nombre FROM roles ORDER BY id_rol";
        return jdbc.query(sql, rolMapper);
    }
}
