package com.biblioteca.dao;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.exception.DuplicateException;
import com.biblioteca.exception.NotFoundException;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcDaoTest {
    private JdbcDataSource dataSource;
    private AutorDao autorDao;
    private CategoriaDao categoriaDao;
    private LibroDao libroDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID().toString().replace("-", "") + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection();
             InputStream schema = getClass().getClassLoader().getResourceAsStream("schema.sql");
             InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(schema), StandardCharsets.UTF_8)) {
            RunScript.execute(connection, reader);
        }
        autorDao = new JdbcAutorDao(dataSource);
        categoriaDao = new JdbcCategoriaDao(dataSource);
        libroDao = new JdbcLibroDao(dataSource);
    }

    @Test
    void autorDaoCompletaCrudYRechazaIdsInexistentes() {
        AutorDTO creado = autorDao.create(new AutorDTO(null, "Jane", "Austen"));

        assertNotNull(creado.getIdAutor());
        assertEquals("Jane", autorDao.findById(creado.getIdAutor()).getNombre());
        assertEquals(1, autorDao.findAll().size());
        creado.setNombre("Jane A.");
        assertEquals("Jane A.", autorDao.update(creado).getNombre());
        assertThrows(NotFoundException.class, () -> autorDao.findById(999L));
        assertThrows(NotFoundException.class,
                () -> autorDao.update(new AutorDTO(999L, "Nombre", "Apellido")));
        assertThrows(NotFoundException.class, () -> autorDao.delete(999L));
        autorDao.delete(creado.getIdAutor());
        assertTrue(autorDao.findAll().isEmpty());
        assertThrows(NotFoundException.class, () -> autorDao.findById(creado.getIdAutor()));
    }

    @Test
    void autorDaoNoBorraAutoresConLibros() {
        AutorDTO autor = autorDao.create(new AutorDTO(null, "Gabriel", "Márquez"));
        libroDao.create(new LibroDTO(null, "Cien años de soledad", "9780307474728", 1967, autor.getIdAutor()));

        assertThrows(DaoException.class, () -> autorDao.delete(autor.getIdAutor()));
        assertEquals("Gabriel", autorDao.findById(autor.getIdAutor()).getNombre());
    }

    @Test
    void categoriaDaoCompletaCrudRechazaDuplicadosEIdsInexistentes() {
        CategoriaDTO creada = categoriaDao.create(new CategoriaDTO(null, "Novela"));
        assertEquals("Novela", categoriaDao.findById(creada.getIdCategoria()).getNombre());
        assertEquals(1, categoriaDao.findAll().size());
        assertThrows(DuplicateException.class,
                () -> categoriaDao.create(new CategoriaDTO(null, "Novela")));

        CategoriaDTO duplicada = categoriaDao.create(new CategoriaDTO(null, "Otra"));
        duplicada.setNombre("Novela");
        assertThrows(DuplicateException.class, () -> categoriaDao.update(duplicada));

        creada.setNombre("Clásicos");
        assertEquals("Clásicos", categoriaDao.update(creada).getNombre());
        assertThrows(NotFoundException.class, () -> categoriaDao.findById(999L));
        assertThrows(NotFoundException.class,
                () -> categoriaDao.update(new CategoriaDTO(999L, "Inexistente")));
        assertThrows(NotFoundException.class, () -> categoriaDao.delete(999L));
        categoriaDao.delete(creada.getIdCategoria());
        assertTrue(categoriaDao.findAll().stream()
                .noneMatch(categoria -> categoria.getIdCategoria().equals(creada.getIdCategoria())));
    }

    @Test
    void libroDaoCompletaCrudYRechazaIsbnDuplicado() {
        AutorDTO autor = autorDao.create(new AutorDTO(null, "Gabriel", "Márquez"));
        CategoriaDTO clasico = categoriaDao.create(new CategoriaDTO(null, "Clásico"));
        CategoriaDTO realismo = categoriaDao.create(new CategoriaDTO(null, "Realismo mágico"));

        LibroDTO libro = new LibroDTO(null, "Cien años de soledad", "9780307474728", 1967, autor.getIdAutor());
        libro.setIdCategorias(List.of(clasico.getIdCategoria(), realismo.getIdCategoria()));
        LibroDTO creado = libroDao.create(libro);

        assertNotNull(creado.getIdLibro());
        assertEquals(2, libroDao.findById(creado.getIdLibro()).getIdCategorias().size());
        assertEquals(1, libroDao.findAll().size());
        assertThrows(DuplicateException.class, () -> libroDao.create(new LibroDTO(null,
                "Otro título", "9780307474728", 2000, autor.getIdAutor())));
        creado.setIdCategorias(List.of(clasico.getIdCategoria()));
        assertEquals(List.of(clasico.getIdCategoria()), libroDao.update(creado).getIdCategorias());
        assertThrows(NotFoundException.class, () -> libroDao.findById(999L));
        assertThrows(NotFoundException.class, () -> libroDao.update(new LibroDTO(999L,
                "Inexistente", "9780307474728", 2000, autor.getIdAutor())));
        assertThrows(NotFoundException.class, () -> libroDao.delete(999L));

        libroDao.delete(creado.getIdLibro());
        assertTrue(libroDao.findAll().isEmpty());
        assertThrows(NotFoundException.class, () -> libroDao.findById(creado.getIdLibro()));
    }

    @Test
    void libroDaoRevierteCambiosYRelacionesCuandoFallaLaTransaccion() {
        AutorDTO autor = autorDao.create(new AutorDTO(null, "Gabriel", "Márquez"));
        CategoriaDTO clasico = categoriaDao.create(new CategoriaDTO(null, "Clásico"));
        LibroDTO libro = new LibroDTO(null, "Cien años de soledad", "9780307474728", 1967, autor.getIdAutor());
        libro.setIdCategorias(List.of(clasico.getIdCategoria()));
        LibroDTO creado = libroDao.create(libro);

        creado.setTitulo("Título actualizado");
        creado.setIdCategorias(List.of(999L));
        assertThrows(DaoException.class, () -> libroDao.update(creado));
        LibroDTO sinCambios = libroDao.findById(creado.getIdLibro());
        assertEquals("Cien años de soledad", sinCambios.getTitulo());
        assertEquals(List.of(clasico.getIdCategoria()), sinCambios.getIdCategorias());

        LibroDTO invalido = new LibroDTO(null, "Libro inválido", "9780000000000", 2024, autor.getIdAutor());
        invalido.setIdCategorias(List.of(999L));
        assertThrows(DaoException.class, () -> libroDao.create(invalido));
        assertEquals(1, libroDao.findAll().size());
    }
}