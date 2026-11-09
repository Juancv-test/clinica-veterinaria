package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Mascota;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MascotaDaoImpl implements MascotaDao {

    private final JdbcTemplate jdbc;

    public MascotaDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Mascota> mascotaMapper = (rs, rowNum) -> {
        Date sqlFecha = rs.getDate("fecha_nacimiento");
        BigDecimal peso = rs.getBigDecimal("peso_kg");

        long idClienteVal = rs.getLong("id_cliente");
        Long idCliente = rs.wasNull() ? null : idClienteVal;

        boolean esTemporal = false;
        try {
            esTemporal = rs.getBoolean("es_temporal");
        } catch (Exception ignored) {
        }

        return new Mascota(
                rs.getLong("id_mascota"),
                rs.getString("nombre"),
                rs.getString("especie"),
                rs.getString("raza"),
                rs.getString("sexo"),
                sqlFecha != null ? sqlFecha.toLocalDate() : null,
                peso,
                idCliente,
                rs.getString("nombre_cliente"),
                rs.getBoolean("activo"),
                esTemporal
        );
    };

    // LEFT JOIN para permitir mascotas de emergencia express sin dueno asignado aun
    private static final String SELECT_BASE = """
            SELECT m.id_mascota, m.nombre, m.especie, m.raza, m.sexo,
                   m.fecha_nacimiento, m.peso_kg, m.id_cliente,
                   COALESCE(u.nombres || ' ' || u.apellidos, '') AS nombre_cliente,
                   m.activo, COALESCE(m.es_temporal, false) AS es_temporal
            FROM mascotas m
            LEFT JOIN usuarios u ON m.id_cliente = u.id_usuario
            """;

    @Override
    public Optional<Mascota> findById(Long id) {
        String sql = SELECT_BASE + "WHERE m.id_mascota = ?";
        List<Mascota> res = jdbc.query(sql, mascotaMapper, id);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public List<Mascota> findAll() {
        String sql = SELECT_BASE + "WHERE m.activo = true ORDER BY m.id_mascota";
        return new ArrayList<>(jdbc.query(sql, mascotaMapper));
    }

    @Override
    public List<Mascota> findByClienteId(Long clienteId) {
        String sql = SELECT_BASE + "WHERE m.id_cliente = ? AND m.activo = true ORDER BY m.id_mascota";
        return new ArrayList<>(jdbc.query(sql, mascotaMapper, clienteId));
    }

    @Override
    public List<Mascota> findTemporalesPendientes() {
        String sql = SELECT_BASE + "WHERE m.activo = true AND m.es_temporal = true ORDER BY m.id_mascota DESC";
        return new ArrayList<>(jdbc.query(sql, mascotaMapper));
    }

    @Override
    public Long save(Mascota mascota) {
        String sql = """
                INSERT INTO mascotas (nombre, especie, raza, sexo, fecha_nacimiento, peso_kg, id_cliente, es_temporal)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_mascota"});
            ps.setString(1, mascota.nombre());
            ps.setString(2, mascota.especie());
            ps.setString(3, mascota.raza());
            ps.setString(4, mascota.sexo());
            if (mascota.fechaNacimiento() != null) {
                ps.setDate(5, Date.valueOf(mascota.fechaNacimiento()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            if (mascota.pesoKg() != null) {
                ps.setBigDecimal(6, mascota.pesoKg());
            } else {
                ps.setNull(6, Types.NUMERIC);
            }
            if (mascota.idCliente() != null) {
                ps.setLong(7, mascota.idCliente());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            ps.setBoolean(8, mascota.esTemporal() != null && mascota.esTemporal());
            return ps;
        }, keyHolder);
        Number key = (Number) keyHolder.getKeys().get("id_mascota");
        return key.longValue();
    }

    @Override
    public void update(Long id, Mascota mascota) {
        String sql = """
                UPDATE mascotas
                SET nombre = ?, especie = ?, raza = ?, sexo = ?,
                    fecha_nacimiento = ?, peso_kg = ?
                WHERE id_mascota = ?
                """;
        jdbc.update(sql,
                mascota.nombre(), mascota.especie(), mascota.raza(), mascota.sexo(),
                mascota.fechaNacimiento() != null ? Date.valueOf(mascota.fechaNacimiento()) : null,
                mascota.pesoKg(),
                id);
    }

    @Override
    public void regularizar(Long idMascota, Long idCliente, String nombre, String raza, String sexo, LocalDate fechaNacimiento) {
        String sql = """
                UPDATE mascotas
                SET id_cliente = ?, nombre = ?, raza = ?, sexo = ?,
                    fecha_nacimiento = ?, es_temporal = false
                WHERE id_mascota = ?
                """;
        jdbc.update(sql,
                idCliente,
                nombre,
                raza,
                sexo,
                fechaNacimiento != null ? Date.valueOf(fechaNacimiento) : null,
                idMascota);
    }

    @Override
    public void deactivate(Long id) {
        jdbc.update("UPDATE mascotas SET activo = false WHERE id_mascota = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mascotas WHERE id_mascota = ? AND activo = true",
                Integer.class, id);
        return count != null && count > 0;
    }
}
