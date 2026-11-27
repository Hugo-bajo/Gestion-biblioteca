package com.biblioteca.dao;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JdbcLibroDao implements LibroDao {
    private final DataSource dataSource;

    public JdbcLibroDao(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public LibroDTO create(LibroDTO libro) {
        String sql = "INSERT INTO libro (titulo, isbn, anio_publicacion, id_autor) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long idLibro;
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    setLibroParameters(statement, libro);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new DaoException("No se pudo obtener el id del libro");
                        }
                        idLibro = keys.getLong(1);
                    }
                }
                insertCategorias(connection, idLibro, libro.getIdCategorias());
                connection.commit();
                libro.setIdLibro(idLibro);
                return libro;
            } catch (SQLException exception) {
                JdbcDaoSupport.rollback(connection, exception);
                throw JdbcDaoSupport.translate("No se pudo crear el libro", exception);
            } catch (RuntimeException exception) {
                JdbcDaoSupport.rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo crear el libro", exception);
        }
    }

    @Override
    public LibroDTO findById(Long idLibro) {
        String sql = "SELECT l.id_libro, l.titulo, l.isbn, l.anio_publicacion, l.id_autor, lc.id_categoria " +
                "FROM libro l LEFT JOIN libro_categoria lc ON l.id_libro = lc.id_libro " +
                "WHERE l.id_libro = ? ORDER BY lc.id_categoria";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idLibro);
            List<LibroDTO> libros = readLibros(statement);
            if (libros.isEmpty()) {
                throw new NotFoundException("Libro no encontrado: " + idLibro);
            }
            return libros.get(0);
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo buscar el libro", exception);
        }
    }

    @Override
    public List<LibroDTO> findAll() {
        String sql = "SELECT l.id_libro, l.titulo, l.isbn, l.anio_publicacion, l.id_autor, lc.id_categoria " +
                "FROM libro l LEFT JOIN libro_categoria lc ON l.id_libro = lc.id_libro " +
                "ORDER BY l.id_libro, lc.id_categoria";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return readLibros(statement);
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudieron listar los libros", exception);
        }
    }

    @Override
    public LibroDTO update(LibroDTO libro) {
        String sql = "UPDATE libro SET titulo = ?, isbn = ?, anio_publicacion = ?, id_autor = ? WHERE id_libro = ?";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    setLibroParameters(statement, libro);
                    statement.setLong(5, libro.getIdLibro());
                    if (statement.executeUpdate() == 0) {
                        ensureExists(connection, libro.getIdLibro());
                    }
                }
                deleteCategorias(connection, libro.getIdLibro());
                insertCategorias(connection, libro.getIdLibro(), libro.getIdCategorias());
                connection.commit();
                return libro;
            } catch (SQLException exception) {
                JdbcDaoSupport.rollback(connection, exception);
                throw JdbcDaoSupport.translate("No se pudo actualizar el libro", exception);
            } catch (RuntimeException exception) {
                JdbcDaoSupport.rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo actualizar el libro", exception);
        }
    }

    @Override
    public void delete(Long idLibro) {
        String sql = "DELETE FROM libro WHERE id_libro = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idLibro);
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Libro no encontrado: " + idLibro);
            }
        } catch (SQLException exception) {
            throw JdbcDaoSupport.translate("No se pudo borrar el libro", exception);
        }
    }

    private void setLibroParameters(PreparedStatement statement, LibroDTO libro) throws SQLException {
        statement.setString(1, libro.getTitulo());
        statement.setString(2, libro.getIsbn());
        statement.setObject(3, libro.getAnioPublicacion());
        statement.setObject(4, libro.getIdAutor());
    }

    private void insertCategorias(Connection connection, Long idLibro, List<Long> idCategorias) throws SQLException {
        String sql = "INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Long idCategoria : idCategorias) {
                statement.setLong(1, idLibro);
                statement.setLong(2, idCategoria);
                statement.executeUpdate();
            }
        }
    }

    private void deleteCategorias(Connection connection, Long idLibro) throws SQLException {
        String sql = "DELETE FROM libro_categoria WHERE id_libro = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idLibro);
            statement.executeUpdate();
        }
    }

    private void ensureExists(Connection connection, Long idLibro) throws SQLException {
        String sql = "SELECT 1 FROM libro WHERE id_libro = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idLibro);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new NotFoundException("Libro no encontrado: " + idLibro);
                }
            }
        }
    }

    private List<LibroDTO> readLibros(PreparedStatement statement) throws SQLException {
        Map<Long, LibroDTO> libros = new LinkedHashMap<>();
        try (ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Long idLibro = results.getLong("id_libro");
                LibroDTO libro = libros.computeIfAbsent(idLibro, id -> new LibroDTO(
                        id,
                        getString(results, "titulo"),
                        getString(results, "isbn"),
                        getInteger(results, "anio_publicacion"),
                        getLong(results, "id_autor")));
                long idCategoria = results.getLong("id_categoria");
                if (!results.wasNull()) {
                    libro.getIdCategorias().add(idCategoria);
                }
            }
        }
        return new ArrayList<>(libros.values());
    }

    private String getString(ResultSet results, String column) {
        try {
            return results.getString(column);
        } catch (SQLException exception) {
            throw new DaoException("No se pudo leer el libro", exception);
        }
    }

    private Integer getInteger(ResultSet results, String column) {
        try {
            int value = results.getInt(column);
            return results.wasNull() ? null : value;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo leer el libro", exception);
        }
    }

    private Long getLong(ResultSet results, String column) {
        try {
            long value = results.getLong(column);
            return results.wasNull() ? null : value;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo leer el libro", exception);
        }
    }
}