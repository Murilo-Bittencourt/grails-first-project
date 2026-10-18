package br.com.murilocb

import static org.springframework.http.HttpStatus.*

/**
 * CONTROLLER (C) - recebe a requisição HTTP, chama o service e escolhe a resposta.
 * As rotas vêm do UrlMappings.groovy (padrão REST):
 *   GET    /book        -> index
 *   GET    /book/1      -> show
 *   POST   /book        -> save
 *   PUT    /book/1      -> update
 *   DELETE /book/1      -> delete
 */
class BookController {

    static responseFormats = ['json']

    BookService bookService

    def index() {
        respond bookService.list(params)
    }

    def show(Long id) {
        Book book = bookService.get(id)
        if (!book) {
            render status: NOT_FOUND
            return
        }
        respond book
    }

    def save() {
        Book book = new Book()
        bindData(book, request.JSON)
        bookService.save(book)

        if (book.hasErrors()) {
            respond book.errors, status: UNPROCESSABLE_ENTITY   // 422
            return
        }
        respond book, status: CREATED                           // 201
    }

    def update(Long id) {
        Book book = bookService.get(id)
        if (!book) {
            render status: NOT_FOUND
            return
        }
        bindData(book, request.JSON)
        bookService.save(book)

        if (book.hasErrors()) {
            respond book.errors, status: UNPROCESSABLE_ENTITY
            return
        }
        respond book, status: OK
    }

    def delete(Long id) {
        if (!bookService.delete(id)) {
            render status: NOT_FOUND
            return
        }
        render status: NO_CONTENT                               // 204
    }
}
