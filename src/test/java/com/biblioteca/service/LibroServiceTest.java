package com.biblioteca.service;

import com.biblioteca.dao.AutorDao;
import com.biblioteca.dao.CategoriaDao;
import com.biblioteca.dao.LibroDao;
import com.biblioteca.dto.AutorDTO;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.exception.DuplicateException;
import com.biblioteca.exception.NotFoundException;
import com.biblioteca.exception.ValidationException;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LibroServiceTest {
    @Test
    void delegatesCrudOperationsToDao() {
        LibroDao libroDao = mock(LibroDao.class);
        AutorDao autorDao = mock(AutorDao.class);
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        LibroService service = new LibroService(libroDao, autorDao, categoriaDao);
        LibroDTO libro = new LibroDTO(1L, "Título", "9780307474728", 2020, 1L);
        when(autorDao.findById(1L)).thenReturn(new AutorDTO(1L, "Nombre", "Apellido"));
        when(libroDao.create(libro)).thenReturn(libro);
        when(libroDao.findById(1L)).thenReturn(libro);
        when(libroDao.findAll()).thenReturn(List.of(libro));
        when(libroDao.update(libro)).thenReturn(libro);

        assertEquals(libro, service.create(libro));
        assertEquals(libro, service.findById(1L));
        assertEquals(List.of(libro), service.findAll());
        assertEquals(libro, service.update(libro));
        service.delete(1L);

        verify(libroDao).create(libro);
        verify(libroDao).findById(1L);
        verify(libroDao).findAll();
        verify(libroDao).update(libro);
        verify(libroDao).delete(1L);
    }

    @Test
    void rejectsInvalidInputWithoutCallingDao() {
        LibroDao libroDao = mock(LibroDao.class);
        AutorDao autorDao = mock(AutorDao.class);
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        LibroService service = new LibroService(libroDao, autorDao, categoriaDao);

        assertThrows(ValidationException.class,
                () -> service.create(new LibroDTO(null, " ", "9780307474728", 2020, 1L)));
        assertThrows(ValidationException.class,
                () -> service.create(new LibroDTO(null, "Título", "9780307474729", 2020, 1L)));
        assertThrows(ValidationException.class,
                () -> service.create(new LibroDTO(null, "Título", "9780307474728", Year.now().getValue() + 1, 1L)));
        LibroDTO categoriaInvalida = new LibroDTO(null, "Título", "9780307474728", 2020, 1L);
        categoriaInvalida.setIdCategorias(List.of(0L));
        assertThrows(ValidationException.class, () -> service.create(categoriaInvalida));
        assertThrows(ValidationException.class, () -> service.findById(0L));

        verifyNoInteractions(libroDao, autorDao, categoriaDao);
    }

    @Test
    void propagatesMissingAndDuplicateErrorsFromDao() {
        LibroDao libroDao = mock(LibroDao.class);
        AutorDao autorDao = mock(AutorDao.class);
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        LibroService service = new LibroService(libroDao, autorDao, categoriaDao);
        when(libroDao.findById(99L)).thenThrow(new NotFoundException("No encontrado"));
        when(autorDao.findById(1L)).thenReturn(new AutorDTO(1L, "Nombre", "Apellido"));
        when(libroDao.create(any(LibroDTO.class))).thenThrow(new DuplicateException("Duplicado"));

        assertThrows(NotFoundException.class, () -> service.findById(99L));
        assertThrows(DuplicateException.class,
                () -> service.create(new LibroDTO(null, "Título", "9780307474728", 2020, 1L)));
    }

    @Test
    void treatsNullOptionalCategoriesAsEmpty() {
        LibroDao libroDao = mock(LibroDao.class);
        AutorDao autorDao = mock(AutorDao.class);
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        LibroService service = new LibroService(libroDao, autorDao, categoriaDao);
        when(autorDao.findById(1L)).thenReturn(new AutorDTO(1L, "Nombre", "Apellido"));
        when(libroDao.create(any(LibroDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LibroDTO libro = new Gson().fromJson(
                "{\"titulo\":\"Título\",\"isbn\":\"9780307474728\",\"anioPublicacion\":2020," +
                        "\"idAutor\":1,\"idCategorias\":null}", LibroDTO.class);

        LibroDTO creado = service.create(libro);

        assertEquals(List.of(), creado.getIdCategorias());
    }

    @Test
    void rejectsUnknownAuthorAndCategoryReferences() {
        LibroDao libroDao = mock(LibroDao.class);
        AutorDao autorDao = mock(AutorDao.class);
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        LibroService service = new LibroService(libroDao, autorDao, categoriaDao);
        when(autorDao.findById(99L)).thenThrow(new NotFoundException("Autor no encontrado"));
        when(autorDao.findById(1L)).thenReturn(new AutorDTO(1L, "Nombre", "Apellido"));
        when(categoriaDao.findById(99L)).thenThrow(new NotFoundException("Categoría no encontrada"));

        assertThrows(NotFoundException.class, () -> service.create(
                new LibroDTO(null, "Título", "9780307474728", 2020, 99L)));
        LibroDTO categoriaInexistente = new LibroDTO(null, "Título", "9780307474728", 2020, 1L);
        categoriaInexistente.setIdCategorias(List.of(99L));
        assertThrows(NotFoundException.class, () -> service.create(categoriaInexistente));

        verifyNoInteractions(libroDao);
    }
}