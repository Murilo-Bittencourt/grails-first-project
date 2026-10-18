package br.com.murilocb

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

/**
 * Camada de SERVICE - regras de negócio e acesso ao banco via GORM.
 * O controller não fala direto com o banco, só com o service.
 */
@Transactional
@CompileStatic
class BookService {

    List<Book> list(Map params = [:]) {
        Book.list(params)
    }

    Book get(Serializable id) {
        Book.get(id)
    }

    /** Salva (insert ou update). Se não validar, o book volta com book.errors preenchido. */
    Book save(Book book) {
        book.save()
        book
    }

    boolean delete(Serializable id) {
        Book book = Book.get(id)
        if (!book) {
            return false
        }
        book.delete()
        true
    }
}
