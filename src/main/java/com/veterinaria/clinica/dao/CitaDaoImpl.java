package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Cita;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CitaDaoImpl implements CitaDao {

    private final JdbcTemplate jdbc;

    public CitaDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper con múltiples JOINs para obtener nombres descriptivos
    private final RowMapper<Cita> citaMapper = (rs, rowNum) -> {
        // id_veterinario es nullable → verificamos wasNull() después de getLong()
        long idVet = rs.getLong("id_veterinario");
        Long idVeterinario = rs.wasNull() ? null : idVet;

        return new Cita(
                rs.getLong("id_cita"),
                rs.getLong("id_mascota"),
                rs.getString("nombre_mascota"),
                rs.getInt("id_servicio"),
                rs.getString("nombre_servicio"),
                idVeterinario,
                rs.getString("nombre_veterinario"),
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora").toLocalTime(),
                rs.getString("estado"),
                rs.getString("motivo")
        );
    };

    // SQL base con tres JOINs: mascota, servicio y veterinario (LEFT porque es opcional)
    private static final String SELECT_BASE = """
            SELECT c.id_cita, c.id_mascota, m.nombre AS nombre_mascota,
                   c.id_servicio, s.nombre AS nombre_servicio,
                   c.id_veterinario,
                   COALESCE(v.nombres || ' ' || v.apellidos, '') AS nombre_veterinario,
                   c.fecha, c.hora, c.estado, c.motivo
            FROM citas c
            JOIN mascotas m  ON c.id_mascota  = m.id_mascota
            JOIN servicios s ON c.id_servicio = s.id_servicio
            LEFT JOIN usuarios v ON c.id_veterinario = v.id_usuario
            """;

    @Override
    public Optional<Cita> findById(Long id) {
        String sql = SELECT_BASE + "WHERE c.id_cita = ?";
        List<Cita> res = jdbc.query(sql, citaMapper, id);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public List<Cita> findAll(LocalDate fecha, String estado) {
        /*
         * Construcción dinámica SEGURA del WHERE:
         * - Los valores van como parámetros (List<Object>), nunca como texto en la query.
         * - Solo la estructura de la cláusula WHERE cambia según los filtros presentes.
         */
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fecha != null) {
            sql.append("AND c.fecha = ? ");
            params.add(Date.valueOf(fecha));
        }
        if (estado != null && !estado.isBlank()) {
            sql.append("AND c.estado = ? ");
            params.add(estado.toUpperCase());
        }
        sql.append("ORDER BY c.fecha, c.hora");

        return new ArrayList<>(jdbc.query(sql.toString(), citaMapper, params.toArray()));
    }

    @Override
    public List<Cita> findProgramadasOrdenadas() {
        // Carga todas las citas PROGRAMADAS en orden para reconstruir la cola al iniciar
        String sql = SELECT_BASE + "WHERE c.estado = 'PROGRAMADA' ORDER BY c.fecha ASC, c.hora ASC";
        return new ArrayList<>(jdbc.query(sql, citaMapper));
    }

    @Override
    public Long save(Cita cita) {
        String sql = """
                INSERT INTO citas (id_mascota, id_servicio, id_veterinario, fecha, hora, estado, motivo)
                VALUES (?, ?, ?, ?, ?, 'PROGRAMADA', ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_cita"});
            ps.setLong(1, cita.idMascota());
            ps.setInt(2, cita.idServicio());
            // id_veterinario es opcional
            if (cita.idVeterinario() != null) {
                ps.setLong(3, cita.idVeterinario());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setDate(4, Date.valueOf(cita.fecha()));
            ps.setTime(5, Time.valueOf(cita.hora()));
            ps.setString(6, cita.motivo());
            return ps;
        }, keyHolder);
        // Leer la clave por nombre: más robusto con PostgreSQL que getKey()
        Number key = (Number) keyHolder.getKeys().get("id_cita");
        return key.longValue();
    }

    @Override
    public void update(Long id, Cita cita) {
        String sql = """
                UPDATE citas
                SET id_mascota = ?, id_servicio = ?, id_veterinario = ?,
                    fecha = ?, hora = ?, motivo = ?
                WHERE id_cita = ?
                """;
        jdbc.update(sql,
                cita.idMascota(), cita.idServicio(), cita.idVeterinario(),
                Date.valueOf(cita.fecha()), Time.valueOf(cita.hora()),
                cita.motivo(), id);
    }

    @Override
    public void cancel(Long id) {
        jdbc.update("UPDATE citas SET estado = 'CANCELADA' WHERE id_cita = ?", id);
    }

    @Override
    public boolean existsConflicto(Long idMascota, LocalDate fecha, LocalTime hora) {
        // Detecta si la mascota ya tiene una cita PROGRAMADA a esa misma fecha y hora
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM citas WHERE id_mascota = ? AND fecha = ? AND hora = ? AND estado = 'PROGRAMADA'",
                Integer.class,
                idMascota, Date.valueOf(fecha), Time.valueOf(hora));
        return count != null && count > 0;
    }
}
