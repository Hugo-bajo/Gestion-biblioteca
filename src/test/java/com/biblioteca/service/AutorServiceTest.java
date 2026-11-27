package com.biblioteca.service;

import com.biblioteca.dao.AutorDao;
import com.biblioteca.dao.LibroDao;
import com.biblioteca.dto.AutorDTO;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.exception.DuplicateException;
import com.biblioteca.exception.NotFoundException;
import com.biblioteca.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AutorServiceTest {
    @Test
    void delegatesCrudOperationsToDaos() {
        AutorDao autorDao = mock(AutorDao.class);
        LibroDao libroDao = mock(LibroDao.class);
        AutorService service = new AutorService(autorDao, libroDao);
        AutorDTO autor = new AutorDTO(1L, "Jane", "Austen");
        when(autorDao.create(autor)).thenReturn(autor);
        when(autorDao.findById(1L)).thenReturn(autor);
        when(autorDao.findAll()).thenReturn(List.of(autor));
        when(autorDao.update(autor)).thenReturn(autor);
        when(libroDao.findAll()).thenReturn(List.of());

        assertEquals(autor, service.create(autor));
        assertEquals(autor, service.findById(1L));
        assertEquals(List.of(autor), service.findAll());
        assertEquals(autor, service.update(autor));
        service.delete(1L);

        verify(autorDao).create(autor);
        verify(autorDao).findById(1L);
        verify(autorDao).findAll();
        verify(autorDao).update(autor);
        verify(autorDao).delete(1L);
    }

    @Test
    void rejectsInvalidInputWithoutCallingDaos() {
        AutorDao autorDao = mock(AutorDao.class);
        LibroDao libroDao = mock(LibroDao.class);
        AutorService service = new AutorService(autorDao, libroDao);

        assertThrows(ValidationException.class, () -> service.create(new AutorDTO(null, "  ", "Apellido")));
        assertThrows(ValidationException.class, () -> service.findById(0L));
        assertThrows(ValidationException.class, () -> service.update(new AutorDTO(null, "Jane", "Austen")));
        assertThrows(ValidationException.class, () -> service.delete(-1L));

        verifyNoInteractions(autorDao, libroDao);
    }

    @Test
    void propagatesMissingAndDuplicateErrorsFromDao() {
        AutorDao autorDao = mock(AutorDao.class);
        AutorService service = new AutorService(autorDao, mock(LibroDao.class));
        when(autorDao.findById(99L)).thenThrow(new NotFoundException("No encontrado"));
        when(autorDao.create(any(AutorDTO.class))).thenThrow(new DuplicateException("Duplicado"));

        assertThrows(NotFoundException.class, () -> service.findById(99L));
        assertThrows(DuplicateException.class,
                () -> service.create(new AutorDTO(null, "Jane", "Austen")));
    }

    @Test
    void deleteRejectsAuthorWithBooks() {
        AutorDao autorDao = mock(AutorDao.class);
        LibroDao libroDao = mock(LibroDao.class);
        AutorService service = new AutorService(autorDao, libroDao);
        when(libroDao.findAll()).thenReturn(List.of(new LibroDTO(10L, "Título", "0306406152", 2000, 3L)));

        assertThrows(ValidationException.class, () -> service.delete(3L));
        verify(autorDao, never()).delete(3L);
    }
}