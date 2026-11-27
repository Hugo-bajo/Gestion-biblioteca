CREATE TABLE autor (
    id_autor BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL
);

CREATE TABLE libro (
    id_libro BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(17) NOT NULL UNIQUE,
    anio_publicacion SMALLINT NOT NULL,
    id_autor BIGINT NOT NULL,
    CONSTRAINT fk_libro_autor FOREIGN KEY (id_autor) REFERENCES autor(id_autor)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE libro_categoria (
    id_libro_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_libro BIGINT NOT NULL,
    id_categoria BIGINT NOT NULL,
    UNIQUE (id_libro, id_categoria),
    CONSTRAINT fk_libro_categoria_libro FOREIGN KEY (id_libro) REFERENCES libro(id_libro)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_libro_categoria_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
        ON DELETE CASCADE ON UPDATE CASCADE
);