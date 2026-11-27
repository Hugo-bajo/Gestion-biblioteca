package com.biblioteca.controller;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.service.AutorService;
import jakarta.servlet.annotation.WebServlet;

import java.util.List;

@WebServlet(urlPatterns = "/api/autores/*")
public class AutorServlet extends JsonCrudServlet<AutorDTO> {
    private AutorService service;

    @Override
    public void init() {
        service = (AutorService) getServletContext().getAttribute(AutorService.class.getName());
    }

    @Override
    protected Class<AutorDTO> dtoClass() {
        return AutorDTO.class;
    }

    @Override
    protected List<AutorDTO> findAll() {
        return service.findAll();
    }

    @Override
    protected AutorDTO findById(Long id) {
        return service.findById(id);
    }

    @Override
    protected AutorDTO create(AutorDTO dto) {
        return service.create(dto);
    }

    @Override
    protected AutorDTO update(AutorDTO dto) {
        return service.update(dto);
    }

    @Override
    protected void setId(AutorDTO dto, Long id) {
        dto.setIdAutor(id);
    }

    @Override
    protected void delete(Long id) {
        service.delete(id);
    }
}