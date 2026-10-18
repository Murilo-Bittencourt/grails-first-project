package br.com.murilocb

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Teste de integração: sobe o contexto do Grails com H2 de verdade.
 * @Rollback desfaz tudo que cada teste gravar.
 * @Stepwise garante a ordem, para o segundo teste provar o rollback do primeiro.
 */
@Integration
@Rollback
@Stepwise
class BookServiceIntegrationSpec extends Specification {

    BookService bookService   // injetado pelo nome

    def "salva um book válido"() {
        given:
        def antes = Book.count()

        when:
        def book = bookService.save(new Book(title: 'Isolado', author: 'Autor', publishedYear: 2020, price: 10.0))

        then:
        !book.hasErrors()
        book.id != null
        Book.count() == antes + 1
        bookService.get(book.id).title == 'Isolado'
    }

    def "o rollback desfez o book do teste anterior"() {
        expect:
        Book.countByTitle('Isolado') == 0
    }

    def "não grava book inválido"() {
        given:
        def antes = Book.count()

        when:
        def book = bookService.save(new Book(title: '', author: 'Autor'))

        then:
        book.hasErrors()
        book.errors.getFieldError('title') != null
        Book.count() == antes
    }

    def "delete retorna true se existia e false se não existia"() {
        given:
        def book = bookService.save(new Book(title: 'Apagar', author: 'Autor'))

        expect:
        bookService.delete(book.id)
        !bookService.delete(book.id)
        bookService.get(book.id) == null
    }
}
