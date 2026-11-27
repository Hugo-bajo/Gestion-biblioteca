package com.biblioteca.dao;

import com.biblioteca.dto.CategoriaDTO;
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

public class JdbcCategoriaDao implements CategoriaDao {
    private final DataSource dataSource;

    public JdbcCategoriaDao(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public CategoriaDTO create(CategoriaDTO categoria) {
        String sql = "INSERT INTO categoria (nombre) VALUES (?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, categoria.getNombre());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DaoException("No se pudo obtener el id de la categoría");
                }
                categoria.setIdCategoria(keys.getLong(1));
            }
            return categoria;
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo crear la categoría", exception);
        }
    }

    @Override
    public CategoriaDTO findById(Long idCategoria) {
        String sql = "SELECT id_categoria, nombre FROM categoria WHERE id_categoria = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idCategoria);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new NotFoundException("Categoría no encontrada: " + idCategoria);
                }
                return new CategoriaDTO(results.getLong("id_categoria"), results.getString("nombre"));
            }
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo buscar la categoría", exception);
        }
    }

    @Override
    public List<CategoriaDTO> findAll() {
        String sql = "SELECT id_categoria, nombre FROM categoria ORDER BY id_categoria";
        List<CategoriaDTO> categorias = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                categorias.add(new CategoriaDTO(results.getLong("id_categoria"), results.getString("nombre")));
            }
            return categorias;
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudieron listar las categorías", exception);
        }
    }

    @Override
    public CategoriaDTO update(CategoriaDTO categoria) {
        String sql = "UPDATE categoria SET nombre = ? WHERE id_categoria = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoria.getNombre());
            statement.setLong(2, categoria.getIdCategoria());
            if (statement.executeUpdate() == 0) {
                ensureExists(connection, categoria.getIdCategoria());
            }
            return categoria;
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo actualizar la categoría", exception);
        }
    }

    @Override
    public void delete(Long idCategoria) {
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idCategoria);
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Categoría no encontrada: " + idCategoria);
            }
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo borrar la categoría", exception);
        }
    }

    private void ensureExists(Connection connection, Long idCategoria) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE id_categoria = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idCategoria);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new NotFoundException("Categoría no encontrada: " + idCategoria);
                }
            }
        }
    }
}