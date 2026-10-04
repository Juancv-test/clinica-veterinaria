package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Servicio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ServicioDaoImpl implements ServicioDao {

    private final JdbcTemplate jdbc;

    public ServicioDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper para Servicio
    private final RowMapper<Servicio> servicioMapper = (rs, rowNum) ->
            new Servicio(
                    rs.getInt("id_servicio"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getBigDecimal("precio"),
                    rs.getInt("duracion_min"),
                    rs.getBoolean("activo")
            );

    @Override
    public Optional<Servicio> findById(Integer id) {
        // Nota: el ServicioService consulta primero la caché (HashMap) antes de llamar aquí
        String sql = "SELECT * FROM servicios WHERE id_servicio = ?";
        List<Servicio> res = jdbc.query(sql, servicioMapper, id);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public List<Servicio> findAll() {
        // Solo servicios activos para mostrar a los usuarios
        return jdbc.query(
                "SELECT * FROM servicios WHERE activo = true ORDER BY id_servicio",
                servicioMapper);
    }

    @Override
    public List<Servicio> findAllIncludingInactive() {
        // Carga completa para la caché (ServicioCache). Incluye inactivos.
        return jdbc.query(
                "SELECT * FROM servicios ORDER BY id_servicio",
                servicioMapper);
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM servicios WHERE id_servicio = ? AND activo = true",
                Integer.class, id);
        return count != null && count > 0;
    }
}
