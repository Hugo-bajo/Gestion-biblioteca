package com.biblioteca.dao;

import com.biblioteca.dto.LibroDTO;

import java.util.List;

public interface LibroDao {
    LibroDTO create(LibroDTO libro);

    LibroDTO findById(Long idLibro);

    List<LibroDTO> findAll();

    LibroDTO update(LibroDTO libro);

    void delete(Long idLibro);
}