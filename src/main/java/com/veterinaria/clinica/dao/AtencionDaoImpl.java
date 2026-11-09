package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Atencion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AtencionDaoImpl implements AtencionDao {

    private final JdbcTemplate jdbc;

    public AtencionDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Atencion> atencionMapper = (rs, rowNum) -> {
        long idCitaVal = rs.getLong("id_cita");
        Long idCita = rs.wasNull() ? null : idCitaVal;

        return new Atencion(
                rs.getLong("id_atencion"),
                idCita,
                rs.getLong("id_mascota"),
                rs.getString("nombre_mascota"),
                rs.getLong("id_veterinario"),
                rs.getString("nombre_veterinario"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getString("diagnostico"),
                rs.getString("tratamiento"),
                rs.getString("observaciones"),
                rs.getBigDecimal("peso_actual_kg")
        );
    };

    private static final String SELECT_BASE = """
            SELECT a.id_atencion, a.id_cita, a.id_mascota, m.nombre AS nombre_mascota,
                   a.id_veterinario,
                   COALESCE(v.nombres || ' ' || v.apellidos, '') AS nombre_veterinario,
                   a.fecha, a.diagnostico, a.tratamiento, a.observaciones, a.peso_actual_kg
            FROM atenciones a
            JOIN mascotas m ON a.id_mascota = m.id_mascota
            JOIN usuarios v ON a.id_veterinario = v.id_usuario
            """;

    @Override
    public Optional<Atencion> findById(Long id) {
        String sql = SELECT_BASE + "WHERE a.id_atencion = ?";
        List<Atencion> res = jdbc.query(sql, atencionMapper, id);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public List<Atencion> findByMascotaId(Long idMascota) {
        String sql = SELECT_BASE + "WHERE a.id_mascota = ? ORDER BY a.fecha DESC";
        return new ArrayList<>(jdbc.query(sql, atencionMapper, idMascota));
    }

    @Override
    public List<Atencion> findByVeterinarioId(Long idVeterinario) {
        String sql = SELECT_BASE + "WHERE a.id_veterinario = ? ORDER BY a.fecha DESC";
        return new ArrayList<>(jdbc.query(sql, atencionMapper, idVeterinario));
    }

    @Override
    public List<Atencion> findAll() {
        String sql = SELECT_BASE + "ORDER BY a.fecha DESC";
        return new ArrayList<>(jdbc.query(sql, atencionMapper));
    }

    @Override
    public Long save(Atencion atencion) {
        String sql = """
                INSERT INTO atenciones (id_cita, id_mascota, id_veterinario, fecha, diagnostico, tratamiento, observaciones, peso_actual_kg)
                VALUES (?, ?, ?, NOW(), ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_atencion"});
            if (atencion.idCita() != null) {
                ps.setLong(1, atencion.idCita());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, atencion.idMascota());
            ps.setLong(3, atencion.idVeterinario());
            ps.setString(4, atencion.diagnostico());
            ps.setString(5, atencion.tratamiento());
            ps.setString(6, atencion.observaciones());
            if (atencion.pesoActualKg() != null) {
                ps.setBigDecimal(7, atencion.pesoActualKg());
            } else {
                ps.setNull(7, Types.NUMERIC);
            }
            return ps;
        }, keyHolder);
        Number key = (Number) keyHolder.getKeys().get("id_atencion");
        return key.longValue();
    }
}
