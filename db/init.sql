CREATE DATABASE IF NOT EXISTS biblioteca
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE biblioteca;

CREATE TABLE IF NOT EXISTS autor (
    id_autor BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_autor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS libro (
    id_libro BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(17) NOT NULL,
    anio_publicacion SMALLINT UNSIGNED NOT NULL,
    id_autor BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id_libro),
    UNIQUE KEY uk_libro_isbn (isbn),
    CONSTRAINT fk_libro_autor FOREIGN KEY (id_autor)
        REFERENCES autor (id_autor)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_categoria),
    UNIQUE KEY uk_categoria_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS libro_categoria (
    id_libro_categoria BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_libro BIGINT UNSIGNED NOT NULL,
    id_categoria BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id_libro_categoria),
    UNIQUE KEY uk_libro_categoria (id_libro, id_categoria),
    CONSTRAINT fk_libro_categoria_libro FOREIGN KEY (id_libro)
        REFERENCES libro (id_libro)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_libro_categoria_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO autor (id_autor, nombre, apellido) VALUES
    (1, 'Miguel de', 'Cervantes'),
    (2, 'Jane', 'Austen'),
    (3, 'Gabriel García', 'Márquez'),
    (4, 'George', 'Orwell'),
    (5, 'Isabel', 'Allende');

INSERT IGNORE INTO libro (id_libro, titulo, isbn, anio_publicacion, id_autor) VALUES
    (1, 'Don Quijote de la Mancha', '9788420412146', 1605, 1),
    (2, 'Novelas ejemplares', '9788420412153', 1613, 1),
    (3, 'Orgullo y prejuicio', '9780141439518', 1813, 2),
    (4, 'Emma', '9780141439587', 1815, 2),
    (5, 'Cien años de soledad', '9780307474728', 1967, 3),
    (6, 'El amor en los tiempos del cólera', '9780307389732', 1985, 3),
    (7, '1984', '9780451524935', 1949, 4),
    (8, 'Rebelión en la granja', '9780451526342', 1945, 4),
    (9, 'La casa de los espíritus', '9788401352836', 1982, 5),
    (10, 'Eva Luna', '9788401352737', 1987, 5);

INSERT IGNORE INTO categoria (id_categoria, nombre) VALUES
    (1, 'Novela clásica'),
    (2, 'Romance'),
    (3, 'Distopía'),
    (4, 'Realismo mágico'),
    (5, 'Novela histórica');

INSERT IGNORE INTO libro_categoria (id_libro, id_categoria) VALUES
    (1, 1),
    (1, 5),
    (2, 1),
    (2, 5),
    (3, 1),
    (3, 2),
    (4, 1),
    (4, 2),
    (5, 1),
    (5, 4),
    (6, 1),
    (6, 2),
    (6, 4),
    (7, 1),
    (7, 3),
    (8, 1),
    (8, 3),
    (9, 1),
    (9, 4),
    (9, 5),
    (10, 1),
    (10, 4);