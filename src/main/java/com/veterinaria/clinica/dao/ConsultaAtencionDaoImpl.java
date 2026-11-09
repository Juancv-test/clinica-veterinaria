package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.ConsultaAtencion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ConsultaAtencionDaoImpl implements ConsultaAtencionDao {

    private final JdbcTemplate jdbc;

    public ConsultaAtencionDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<ConsultaAtencion> consultaMapper = (rs, rowNum) -> {
        long idRecVal = rs.getLong("id_recepcionista");
        Long idRecepcionista = rs.wasNull() ? null : idRecVal;

        long idCliVal = rs.getLong("id_cliente");
        Long idCliente = rs.wasNull() ? null : idCliVal;

        Timestamp tsRespuesta = rs.getTimestamp("fecha_respuesta");
        LocalDateTime fechaRespuesta = tsRespuesta != null ? tsRespuesta.toLocalDateTime() : null;

        return new ConsultaAtencion(
                rs.getLong("id_consulta"),
                idCliente,
                rs.getString("nombre_cliente"),
                rs.getString("email_cliente"),
                rs.getString("asunto"),
                rs.getString("mensaje"),
                rs.getString("respuesta"),
                idRecepcionista,
                rs.getString("nombre_recepcionista"),
                rs.getString("estado"),
                rs.getTimestamp("fecha_creacion").toLocalDateTime(),
                fechaRespuesta
        );
    };

    private static final String SELECT_BASE = """
            SELECT c.id_consulta, c.id_cliente,
                   COALESCE(u.nombres || ' ' || u.apellidos, c.nombre_contacto, 'Contacto Web') AS nombre_cliente,
                   COALESCE(u.email, c.email_contacto, '') AS email_cliente,
                   c.asunto, c.mensaje, c.respuesta,
                   c.id_recepcionista,
                   COALESCE(r.nombres || ' ' || r.apellidos, '') AS nombre_recepcionista,
                   c.estado, c.fecha_creacion, c.fecha_respuesta
            FROM consultas_atencion c
            LEFT JOIN usuarios u ON c.id_cliente = u.id_usuario
            LEFT JOIN usuarios r ON c.id_recepcionista = r.id_usuario
            """;

    @Override
    public Optional<ConsultaAtencion> findById(Long id) {
        String sql = SELECT_BASE + "WHERE c.id_consulta = ?";
        List<ConsultaAtencion> res = jdbc.query(sql, consultaMapper, id);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public List<ConsultaAtencion> findAll(String estado) {
        if (estado != null && !estado.isBlank()) {
            String sql = SELECT_BASE + "WHERE c.estado = ? ORDER BY c.fecha_creacion DESC";
            return new ArrayList<>(jdbc.query(sql, consultaMapper, estado.toUpperCase()));
        }
        String sql = SELECT_BASE + "ORDER BY c.fecha_creacion DESC";
        return new ArrayList<>(jdbc.query(sql, consultaMapper));
    }

    @Override
    public List<ConsultaAtencion> findByClienteId(Long idCliente) {
        String sql = SELECT_BASE + "WHERE c.id_cliente = ? ORDER BY c.fecha_creacion DESC";
        return new ArrayList<>(jdbc.query(sql, consultaMapper, idCliente));
    }

    @Override
    public Long save(ConsultaAtencion consulta) {
        String sql = """
                INSERT INTO consultas_atencion (id_cliente, nombre_contacto, email_contacto, asunto, mensaje, estado, fecha_creacion)
                VALUES (?, ?, ?, ?, ?, 'PENDIENTE', NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_consulta"});
            if (consulta.idCliente() != null) {
                ps.setLong(1, consulta.idCliente());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, consulta.nombreCliente());
            ps.setString(3, consulta.emailCliente());
            ps.setString(4, consulta.asunto());
            ps.setString(5, consulta.mensaje());
            return ps;
        }, keyHolder);
        Number key = (Number) keyHolder.getKeys().get("id_consulta");
        return key.longValue();
    }

    @Override
    public void responder(Long idConsulta, String respuesta, Long idRecepcionista) {
        String sql = """
                UPDATE consultas_atencion
                SET respuesta = ?, id_recepcionista = ?, estado = 'RESPONDIDA', fecha_respuesta = NOW()
                WHERE id_consulta = ?
                """;
        jdbc.update(sql, respuesta, idRecepcionista, idConsulta);
    }
}
