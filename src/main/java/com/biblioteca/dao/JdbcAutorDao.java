package com.biblioteca.dao;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JdbcAutorDao implements AutorDao {
    private final DataSource dataSource;

    public JdbcAutorDao(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public AutorDTO create(AutorDTO autor) {
        String sql = "INSERT INTO autor (nombre, apellido) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, autor.getNombre());
            statement.setString(2, autor.getApellido());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DaoException("No se pudo obtener el id del autor");
                }
                autor.setIdAutor(keys.getLong(1));
            }
            return autor;
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo crear el autor", exception);
        }
    }

    @Override
    public AutorDTO findById(Long idAutor) {
        String sql = "SELECT id_autor, nombre, apellido FROM autor WHERE id_autor = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idAutor);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new NotFoundException("Autor no encontrado: " + idAutor);
                }
                return new AutorDTO(results.getLong("id_autor"), results.getString("nombre"),
                        results.getString("apellido"));
            }
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo buscar el autor", exception);
        }
    }

    @Override
    public List<AutorDTO> findAll() {
        String sql = "SELECT id_autor, nombre, apellido FROM autor ORDER BY id_autor";
        List<AutorDTO> autores = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                autores.add(new AutorDTO(results.getLong("id_autor"), results.getString("nombre"),
                        results.getString("apellido")));
            }
            return autores;
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudieron listar los autores", exception);
        }
    }

    @Override
    public AutorDTO update(AutorDTO autor) {
        String sql = "UPDATE autor SET nombre = ?, apellido = ? WHERE id_autor = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, autor.getNombre());
            statement.setString(2, autor.getApellido());
            statement.setLong(3, autor.getIdAutor());
            if (statement.executeUpdate() == 0) {
                ensureExists(connection, autor.getIdAutor());
            }
            return autor;
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo actualizar el autor", exception);
        }
    }

    @Override
    public void delete(Long idAutor) {
        String sql = "DELETE FROM autor WHERE id_autor = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idAutor);
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Autor no encontrado: " + idAutor);
            }
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo borrar el autor", exception);
        }
    }

    private void ensureExists(Connection connection, Long idAutor) throws SQLException {
        String sql = "SELECT 1 FROM autor WHERE id_autor = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idAutor);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new NotFoundException("Autor no encontrado: " + idAutor);
                }
            }
        }
    }
}