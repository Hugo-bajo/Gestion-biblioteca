const apiBase = new URL('api/', document.baseURI).toString().replace(/\/$/, '');
const state = {
    books: [],
    authors: [],
    categories: []
};

const elements = {
    bookForm: document.querySelector('#book-form'),
    bookId: document.querySelector('#book-id'),
    bookTitle: document.querySelector('#book-title'),
    bookIsbn: document.querySelector('#book-isbn'),
    bookYear: document.querySelector('#book-year'),
    bookAuthor: document.querySelector('#book-author'),
    bookCategories: document.querySelector('#book-categories'),
    bookFormTitle: document.querySelector('#book-form-title'),
    bookSubmit: document.querySelector('#book-submit'),
    bookCancel: document.querySelector('#book-cancel'),
    booksList: document.querySelector('#books-list'),
    booksStatus: document.querySelector('#books-status'),
    authorForm: document.querySelector('#author-form'),
    authorId: document.querySelector('#author-id'),
    authorName: document.querySelector('#author-name'),
    authorSurname: document.querySelector('#author-surname'),
    authorFormTitle: document.querySelector('#author-form-title'),
    authorSubmit: document.querySelector('#author-submit'),
    authorCancel: document.querySelector('#author-cancel'),
    authorsList: document.querySelector('#authors-list'),
    authorsStatus: document.querySelector('#authors-status'),
    categoryForm: document.querySelector('#category-form'),
    categoryId: document.querySelector('#category-id'),
    categoryName: document.querySelector('#category-name'),
    categoryFormTitle: document.querySelector('#category-form-title'),
    categorySubmit: document.querySelector('#category-submit'),
    categoryCancel: document.querySelector('#category-cancel'),
    categoriesList: document.querySelector('#categories-list'),
    categoriesStatus: document.querySelector('#categories-status')
};

function setStatus(element, message, type = '') {
    element.textContent = message;
    element.dataset.state = type;
}

function errorMessage(status, responseError) {
    if (responseError) {
        return responseError;
    }
    if (status === 400) {
        return 'Revisa los datos: hay campos obligatorios o valores no válidos.';
    }
    if (status === 404) {
        return 'No se encontró el registro solicitado.';
    }
    if (status === 409) {
        return 'Ya existe un registro con esos datos.';
    }
    return 'El servidor no pudo completar la operación. Inténtalo de nuevo.';
}

async function apiRequest(resource, options = {}) {
    let response;
    try {
        response = await fetch(`${apiBase}/${resource}`, {
            ...options,
            headers: {
                ...(options.body ? { 'Content-Type': 'application/json' } : {}),
                ...options.headers
            }
        });
    } catch (error) {
        throw new Error('No se pudo conectar con el servidor. Comprueba que la aplicación esté disponible.');
    }

    const text = await response.text();
    let data = null;
    if (text) {
        try {
            data = JSON.parse(text);
        } catch (error) {
            throw new Error('El servidor devolvió una respuesta no válida.');
        }
    }
    if (!response.ok) {
        throw new Error(errorMessage(response.status, data?.error));
    }
    return data;
}

async function runWithStatus(element, action, successMessage) {
    setStatus(element, 'Cargando...', 'loading');
    try {
        await action();
        setStatus(element, successMessage, 'success');
    } catch (error) {
        setStatus(element, error.message || 'Se produjo un error inesperado.', 'error');
    }
}

function appendCell(row, value, className = '') {
    const cell = document.createElement('td');
    cell.textContent = value;
    if (className) {
        cell.className = className;
    }
    row.append(cell);
    return cell;
}

function appendAction(cell, label, action, id, style) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `button button-small ${style}`;
    button.dataset.action = action;
    button.dataset.id = id;
    button.textContent = label;
    cell.append(button);
}

function renderEmptyRow(list, columns, message) {
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = columns;
    cell.className = 'empty-cell';
    cell.textContent = message;
    row.append(cell);
    list.replaceChildren(row);
}

function renderBooks() {
    if (state.books.length === 0) {
        renderEmptyRow(elements.booksList, 4, 'Todavía no hay libros registrados.');
        return;
    }

    const authorNames = new Map(state.authors.map(author => [String(author.idAutor), `${author.nombre} ${author.apellido}`]));
    const categoryNames = new Map(state.categories.map(category => [String(category.idCategoria), category.nombre]));
    const rows = state.books.map(book => {
        const row = document.createElement('tr');
        const titleCell = document.createElement('td');
        const title = document.createElement('span');
        title.className = 'row-title';
        title.textContent = book.titulo;
        const detail = document.createElement('span');
        detail.className = 'row-detail';
        detail.textContent = `${book.isbn} · ${book.anioPublicacion}`;
        titleCell.append(title, detail);
        row.append(titleCell);
        appendCell(row, authorNames.get(String(book.idAutor)) || 'Autor no disponible');
        const categories = (book.idCategorias || [])
            .map(id => categoryNames.get(String(id)))
            .filter(Boolean);
        appendCell(row, categories.length ? categories.join(', ') : 'Sin categorías');
        const actions = document.createElement('td');
        actions.className = 'row-actions';
        appendAction(actions, 'Editar', 'edit-book', book.idLibro, 'button-secondary');
        appendAction(actions, 'Borrar', 'delete-book', book.idLibro, 'button-danger');
        row.append(actions);
        return row;
    });
    elements.booksList.replaceChildren(...rows);
}

function renderAuthors() {
    if (state.authors.length === 0) {
        renderEmptyRow(elements.authorsList, 2, 'Todavía no hay autores registrados.');
    } else {
        const rows = state.authors.map(author => {
            const row = document.createElement('tr');
            appendCell(row, `${author.nombre} ${author.apellido}`);
            const actions = document.createElement('td');
            actions.className = 'row-actions';
            appendAction(actions, 'Editar', 'edit-author', author.idAutor, 'button-secondary');
            appendAction(actions, 'Borrar', 'delete-author', author.idAutor, 'button-danger');
            row.append(actions);
            return row;
        });
        elements.authorsList.replaceChildren(...rows);
    }

    const selectedAuthor = elements.bookAuthor.value;
    const authorOptions = state.authors.map(author => {
        const option = document.createElement('option');
        option.value = author.idAutor;
        option.textContent = `${author.nombre} ${author.apellido}`;
        return option;
    });
    elements.bookAuthor.replaceChildren(new Option('Selecciona un autor', ''), ...authorOptions);
    elements.bookAuthor.value = selectedAuthor;
}

function renderCategories() {
    if (state.categories.length === 0) {
        renderEmptyRow(elements.categoriesList, 2, 'Todavía no hay categorías registradas.');
    } else {
        const rows = state.categories.map(category => {
            const row = document.createElement('tr');
            appendCell(row, category.nombre);
            const actions = document.createElement('td');
            actions.className = 'row-actions';
            appendAction(actions, 'Editar', 'edit-category', category.idCategoria, 'button-secondary');
            appendAction(actions, 'Borrar', 'delete-category', category.idCategoria, 'button-danger');
            row.append(actions);
            return row;
        });
        elements.categoriesList.replaceChildren(...rows);
    }

    const selectedCategories = new Set(Array.from(elements.bookCategories.selectedOptions, option => option.value));
    const categoryOptions = state.categories.map(category => {
        const option = document.createElement('option');
        option.value = category.idCategoria;
        option.textContent = category.nombre;
        option.selected = selectedCategories.has(option.value);
        return option;
    });
    elements.bookCategories.replaceChildren(...categoryOptions);
}

async function refreshBooks() {
    state.books = await apiRequest('libros');
    renderBooks();
}

async function refreshAuthors() {
    state.authors = await apiRequest('autores');
    renderAuthors();
    renderBooks();
}

async function refreshCategories() {
    state.categories = await apiRequest('categorias');
    renderCategories();
    renderBooks();
}

function resetBookForm() {
    elements.bookForm.reset();
    elements.bookId.value = '';
    elements.bookFormTitle.textContent = 'Añadir libro';
    elements.bookSubmit.textContent = 'Guardar libro';
    elements.bookCancel.hidden = true;
}

function resetAuthorForm() {
    elements.authorForm.reset();
    elements.authorId.value = '';
    elements.authorFormTitle.textContent = 'Añadir autor';
    elements.authorSubmit.textContent = 'Guardar autor';
    elements.authorCancel.hidden = true;
}

function resetCategoryForm() {
    elements.categoryForm.reset();
    elements.categoryId.value = '';
    elements.categoryFormTitle.textContent = 'Añadir categoría';
    elements.categorySubmit.textContent = 'Guardar categoría';
    elements.categoryCancel.hidden = true;
}

function startBookEdit(id) {
    const book = state.books.find(item => String(item.idLibro) === id);
    if (!book) {
        return;
    }
    elements.bookId.value = book.idLibro;
    elements.bookTitle.value = book.titulo;
    elements.bookIsbn.value = book.isbn;
    elements.bookYear.value = book.anioPublicacion;
    elements.bookAuthor.value = book.idAutor;
    const categories = new Set((book.idCategorias || []).map(String));
    Array.from(elements.bookCategories.options).forEach(option => {
        option.selected = categories.has(option.value);
    });
    elements.bookFormTitle.textContent = 'Editar libro';
    elements.bookSubmit.textContent = 'Guardar cambios';
    elements.bookCancel.hidden = false;
    elements.bookTitle.focus();
}

function startAuthorEdit(id) {
    const author = state.authors.find(item => String(item.idAutor) === id);
    if (!author) {
        return;
    }
    elements.authorId.value = author.idAutor;
    elements.authorName.value = author.nombre;
    elements.authorSurname.value = author.apellido;
    elements.authorFormTitle.textContent = 'Editar autor';
    elements.authorSubmit.textContent = 'Guardar cambios';
    elements.authorCancel.hidden = false;
    elements.authorName.focus();
}

function startCategoryEdit(id) {
    const category = state.categories.find(item => String(item.idCategoria) === id);
    if (!category) {
        return;
    }
    elements.categoryId.value = category.idCategoria;
    elements.categoryName.value = category.nombre;
    elements.categoryFormTitle.textContent = 'Editar categoría';
    elements.categorySubmit.textContent = 'Guardar cambios';
    elements.categoryCancel.hidden = false;
    elements.categoryName.focus();
}

elements.bookForm.addEventListener('submit', event => {
    event.preventDefault();
    if (!elements.bookForm.reportValidity()) {
        return;
    }
    const id = elements.bookId.value;
    const book = {
        titulo: elements.bookTitle.value.trim(),
        isbn: elements.bookIsbn.value.trim(),
        anioPublicacion: Number(elements.bookYear.value),
        idAutor: Number(elements.bookAuthor.value),
        idCategorias: Array.from(elements.bookCategories.selectedOptions, option => Number(option.value))
    };
    runWithStatus(elements.booksStatus, async () => {
        await apiRequest(id ? `libros/${id}` : 'libros', {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(book)
        });
        resetBookForm();
        await refreshBooks();
    }, id ? 'Libro actualizado correctamente.' : 'Libro creado correctamente.');
});

elements.authorForm.addEventListener('submit', event => {
    event.preventDefault();
    if (!elements.authorForm.reportValidity()) {
        return;
    }
    const id = elements.authorId.value;
    const author = {
        nombre: elements.authorName.value.trim(),
        apellido: elements.authorSurname.value.trim()
    };
    runWithStatus(elements.authorsStatus, async () => {
        await apiRequest(id ? `autores/${id}` : 'autores', {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(author)
        });
        resetAuthorForm();
        await refreshAuthors();
    }, id ? 'Autor actualizado correctamente.' : 'Autor creado correctamente.');
});

elements.categoryForm.addEventListener('submit', event => {
    event.preventDefault();
    if (!elements.categoryForm.reportValidity()) {
        return;
    }
    const id = elements.categoryId.value;
    const category = { nombre: elements.categoryName.value.trim() };
    runWithStatus(elements.categoriesStatus, async () => {
        await apiRequest(id ? `categorias/${id}` : 'categorias', {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(category)
        });
        resetCategoryForm();
        await refreshCategories();
    }, id ? 'Categoría actualizada correctamente.' : 'Categoría creada correctamente.');
});

elements.bookCancel.addEventListener('click', resetBookForm);
elements.authorCancel.addEventListener('click', resetAuthorForm);
elements.categoryCancel.addEventListener('click', resetCategoryForm);

elements.booksList.addEventListener('click', event => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
        return;
    }
    if (button.dataset.action === 'edit-book') {
        startBookEdit(button.dataset.id);
        return;
    }
    if (!window.confirm('¿Quieres borrar este libro?')) {
        return;
    }
    runWithStatus(elements.booksStatus, async () => {
        await apiRequest(`libros/${button.dataset.id}`, { method: 'DELETE' });
        if (elements.bookId.value === button.dataset.id) {
            resetBookForm();
        }
        await refreshBooks();
    }, 'Libro eliminado correctamente.');
});

elements.authorsList.addEventListener('click', event => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
        return;
    }
    if (button.dataset.action === 'edit-author') {
        startAuthorEdit(button.dataset.id);
        return;
    }
    if (!window.confirm('¿Quieres borrar este autor? Los autores con libros asociados no se pueden borrar.')) {
        return;
    }
    runWithStatus(elements.authorsStatus, async () => {
        await apiRequest(`autores/${button.dataset.id}`, { method: 'DELETE' });
        if (elements.authorId.value === button.dataset.id) {
            resetAuthorForm();
        }
        await refreshAuthors();
    }, 'Autor eliminado correctamente.');
});

elements.categoriesList.addEventListener('click', event => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
        return;
    }
    if (button.dataset.action === 'edit-category') {
        startCategoryEdit(button.dataset.id);
        return;
    }
    if (!window.confirm('¿Quieres borrar esta categoría?')) {
        return;
    }
    runWithStatus(elements.categoriesStatus, async () => {
        await apiRequest(`categorias/${button.dataset.id}`, { method: 'DELETE' });
        if (elements.categoryId.value === button.dataset.id) {
            resetCategoryForm();
        }
        await refreshCategories();
        await refreshBooks();
    }, 'Categoría eliminada correctamente.');
});

async function initialize() {
    await Promise.all([
        runWithStatus(elements.authorsStatus, refreshAuthors, ''),
        runWithStatus(elements.categoriesStatus, refreshCategories, ''),
        runWithStatus(elements.booksStatus, refreshBooks, '')
    ]);
}

initialize();