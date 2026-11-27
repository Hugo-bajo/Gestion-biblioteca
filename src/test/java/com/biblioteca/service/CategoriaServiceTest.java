package com.biblioteca.service;

import com.biblioteca.dao.CategoriaDao;
import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.exception.DuplicateException;
import com.biblioteca.exception.NotFoundException;
import com.biblioteca.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CategoriaServiceTest {
    @Test
    void delegatesCrudOperationsToDao() {
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        CategoriaService service = new CategoriaService(categoriaDao);
        CategoriaDTO categoria = new CategoriaDTO(1L, "Novela");
        when(categoriaDao.create(categoria)).thenReturn(categoria);
        when(categoriaDao.findById(1L)).thenReturn(categoria);
        when(categoriaDao.findAll()).thenReturn(List.of(categoria));
        when(categoriaDao.update(categoria)).thenReturn(categoria);

        assertEquals(categoria, service.create(categoria));
        assertEquals(categoria, service.findById(1L));
        assertEquals(List.of(categoria), service.findAll());
        assertEquals(categoria, service.update(categoria));
        service.delete(1L);

        verify(categoriaDao).create(categoria);
        verify(categoriaDao).findById(1L);
        verify(categoriaDao).findAll();
        verify(categoriaDao).update(categoria);
        verify(categoriaDao).delete(1L);
    }

    @Test
    void rejectsInvalidInputWithoutCallingDao() {
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        CategoriaService service = new CategoriaService(categoriaDao);

        assertThrows(ValidationException.class, () -> service.create(new CategoriaDTO(null, " ")));
        assertThrows(ValidationException.class, () -> service.findById(0L));
        assertThrows(ValidationException.class, () -> service.update(new CategoriaDTO(null, "Novela")));
        assertThrows(ValidationException.class, () -> service.delete(-1L));

        verifyNoInteractions(categoriaDao);
    }

    @Test
    void propagatesMissingAndDuplicateErrorsFromDao() {
        CategoriaDao categoriaDao = mock(CategoriaDao.class);
        CategoriaService service = new CategoriaService(categoriaDao);
        when(categoriaDao.findById(99L)).thenThrow(new NotFoundException("No encontrada"));
        when(categoriaDao.create(any(CategoriaDTO.class))).thenThrow(new DuplicateException("Duplicada"));

        assertThrows(NotFoundException.class, () -> service.findById(99L));
        assertThrows(DuplicateException.class,
                () -> service.create(new CategoriaDTO(null, "Novela")));
    }
}