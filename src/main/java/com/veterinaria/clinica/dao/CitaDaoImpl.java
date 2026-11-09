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

    private final RowMapper<Cita> citaMapper = (rs, rowNum) -> {
        long idVet = rs.getLong("id_veterinario");
        Long idVeterinario = rs.wasNull() ? null : idVet;
        boolean esEmergencia = false;
        try {
            esEmergencia = rs.getBoolean("es_emergencia");
        } catch (Exception ignored) {
        }

        String contactoEmergencia = null;
        try {
            contactoEmergencia = rs.getString("contacto_emergencia");
        } catch (Exception ignored) {
        }

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
                rs.getString("motivo"),
                esEmergencia,
                contactoEmergencia
        );
    };

    private static final String SELECT_BASE = """
            SELECT c.id_cita, c.id_mascota, m.nombre AS nombre_mascota,
                   c.id_servicio, s.nombre AS nombre_servicio,
                   c.id_veterinario,
                   COALESCE(v.nombres || ' ' || v.apellidos, '') AS nombre_veterinario,
                   c.fecha, c.hora, c.estado, c.motivo,
                   COALESCE(c.es_emergencia, false) AS es_emergencia,
                   c.contacto_emergencia
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
        String sql = SELECT_BASE + "WHERE c.estado = 'PROGRAMADA' ORDER BY c.fecha ASC, c.hora ASC";
        return new ArrayList<>(jdbc.query(sql, citaMapper));
    }

    @Override
    public List<Cita> findByVeterinario(Long idVeterinario) {
        String sql = SELECT_BASE + "WHERE c.id_veterinario = ? ORDER BY c.fecha ASC, c.hora ASC";
        return new ArrayList<>(jdbc.query(sql, citaMapper, idVeterinario));
    }

    @Override
    public List<Cita> findByCliente(Long idCliente) {
        String sql = SELECT_BASE + "WHERE m.id_cliente = ? ORDER BY c.fecha DESC, c.hora DESC";
        return new ArrayList<>(jdbc.query(sql, citaMapper, idCliente));
    }

    @Override
    public Long save(Cita cita) {
        String sql = """
                INSERT INTO citas (id_mascota, id_servicio, id_veterinario, fecha, hora, estado, motivo, es_emergencia, contacto_emergencia)
                VALUES (?, ?, ?, ?, ?, 'PROGRAMADA', ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_cita"});
            ps.setLong(1, cita.idMascota());
            ps.setInt(2, cita.idServicio());
            if (cita.idVeterinario() != null) {
                ps.setLong(3, cita.idVeterinario());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setDate(4, Date.valueOf(cita.fecha()));
            ps.setTime(5, Time.valueOf(cita.hora()));
            ps.setString(6, cita.motivo());
            ps.setBoolean(7, cita.esEmergencia() != null && cita.esEmergencia());
            ps.setString(8, cita.contactoEmergencia());
            return ps;
        }, keyHolder);
        Number key = (Number) keyHolder.getKeys().get("id_cita");
        return key.longValue();
    }

    @Override
    public void update(Long id, Cita cita) {
        String sql = """
                UPDATE citas
                SET id_mascota = ?, id_servicio = ?, id_veterinario = ?,
                    fecha = ?, hora = ?, motivo = ?, es_emergencia = ?, contacto_emergencia = ?
                WHERE id_cita = ?
                """;
        jdbc.update(sql,
                cita.idMascota(), cita.idServicio(), cita.idVeterinario(),
                Date.valueOf(cita.fecha()), Time.valueOf(cita.hora()),
                cita.motivo(),
                cita.esEmergencia() != null && cita.esEmergencia(),
                cita.contactoEmergencia(),
                id);
    }

    @Override
    public void cancel(Long id) {
        jdbc.update("UPDATE citas SET estado = 'CANCELADA' WHERE id_cita = ?", id);
    }

    @Override
    public void marcarAtendida(Long id) {
        jdbc.update("UPDATE citas SET estado = 'ATENDIDA' WHERE id_cita = ?", id);
    }

    @Override
    public boolean existsConflicto(Long idMascota, LocalDate fecha, LocalTime hora) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM citas WHERE id_mascota = ? AND fecha = ? AND hora = ? AND estado = 'PROGRAMADA'",
                Integer.class,
                idMascota, Date.valueOf(fecha), Time.valueOf(hora));
        return count != null && count > 0;
    }

    @Override
    public boolean existsConflictoHorario(LocalDate fecha, LocalTime hora, Long excludeIdCita, Long idVeterinario) {
        // 1. Si se especificó un veterinario, verificar si ese doctor ya tiene una cita PROGRAMADA en esa fecha y hora
        if (idVeterinario != null) {
            String sqlVet = """
                    SELECT COUNT(*) FROM citas 
                    WHERE fecha = ? AND hora = ? AND estado = 'PROGRAMADA' 
                      AND id_veterinario = ?
                    """ + (excludeIdCita != null ? " AND id_cita != ?" : "");

            Integer countVet = (excludeIdCita != null)
                    ? jdbc.queryForObject(sqlVet, Integer.class, Date.valueOf(fecha), Time.valueOf(hora), idVeterinario, excludeIdCita)
                    : jdbc.queryForObject(sqlVet, Integer.class, Date.valueOf(fecha), Time.valueOf(hora), idVeterinario);

            if (countVet != null && countVet > 0) {
                return true;
            }
        }

        // 2. Capacidad simultánea según la cantidad de veterinarios activos en la clínica
        Integer totalVets = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol WHERE r.nombre = 'VETERINARIO' AND u.activo = true",
                Integer.class);
        int maxCapacidad = (totalVets != null && totalVets > 0) ? totalVets : 1;

        // 3. Contar el total de citas PROGRAMADAS activas en esa fecha y hora exacta
        String sqlTotal = """
                SELECT COUNT(*) FROM citas 
                WHERE fecha = ? AND hora = ? AND estado = 'PROGRAMADA'
                """ + (excludeIdCita != null ? " AND id_cita != ?" : "");

        Integer countTotal = (excludeIdCita != null)
                ? jdbc.queryForObject(sqlTotal, Integer.class, Date.valueOf(fecha), Time.valueOf(hora), excludeIdCita)
                : jdbc.queryForObject(sqlTotal, Integer.class, Date.valueOf(fecha), Time.valueOf(hora));

        return countTotal != null && countTotal >= maxCapacidad;
    }

    @Override
    public void updateVeterinario(Long idCita, Long idVeterinario) {
        jdbc.update("UPDATE citas SET id_veterinario = ? WHERE id_cita = ?", idVeterinario, idCita);
    }
}
