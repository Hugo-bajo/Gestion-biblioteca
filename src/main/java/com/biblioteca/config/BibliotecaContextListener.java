package com.biblioteca.config;

import com.biblioteca.dao.AutorDao;
import com.biblioteca.dao.CategoriaDao;
import com.biblioteca.dao.JdbcAutorDao;
import com.biblioteca.dao.JdbcCategoriaDao;
import com.biblioteca.dao.JdbcLibroDao;
import com.biblioteca.dao.LibroDao;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.CategoriaService;
import com.biblioteca.service.LibroService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.sql.DataSource;

@WebListener
public class BibliotecaContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        DataSource dataSource = DataSourceProvider.getDataSource();
        AutorDao autorDao = new JdbcAutorDao(dataSource);
        LibroDao libroDao = new JdbcLibroDao(dataSource);
        CategoriaDao categoriaDao = new JdbcCategoriaDao(dataSource);
        event.getServletContext().setAttribute(AutorService.class.getName(), new AutorService(autorDao, libroDao));
        event.getServletContext().setAttribute(LibroService.class.getName(),
                new LibroService(libroDao, autorDao, categoriaDao));
        event.getServletContext().setAttribute(CategoriaService.class.getName(), new CategoriaService(categoriaDao));
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        DataSourceProvider.closeDataSource();
    }
}