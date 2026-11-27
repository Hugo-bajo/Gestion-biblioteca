package com.biblioteca.controller;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.service.LibroService;
import jakarta.servlet.annotation.WebServlet;

import java.util.List;

@WebServlet(urlPatterns = "/api/libros/*")
public class LibroServlet extends JsonCrudServlet<LibroDTO> {
    private LibroService service;

    @Override
    public void init() {
        service = (LibroService) getServletContext().getAttribute(LibroService.class.getName());
    }

    @Override
    protected Class<LibroDTO> dtoClass() {
        return LibroDTO.class;
    }

    @Override
    protected List<LibroDTO> findAll() {
        return service.findAll();
    }

    @Override
    protected LibroDTO findById(Long id) {
        return service.findById(id);
    }

    @Override
    protected LibroDTO create(LibroDTO dto) {
        return service.create(dto);
    }

    @Override
    protected LibroDTO update(LibroDTO dto) {
        return service.update(dto);
    }

    @Override
    protected void setId(LibroDTO dto, Long id) {
        dto.setIdLibro(id);
    }

    @Override
    protected void delete(Long id) {
        service.delete(id);
    }
}