package com.biblioteca.dao;

import com.biblioteca.dto.AutorDTO;

import java.util.List;

public interface AutorDao {
    AutorDTO create(AutorDTO autor);

    AutorDTO findById(Long idAutor);

    List<AutorDTO> findAll();

    AutorDTO update(AutorDTO autor);

    void delete(Long idAutor);
}