package br.com.murilocb

class BootStrap {

    def init = {
        if (Book.count() == 0) {
            new Book(title: 'Clean Code', author: 'Robert C. Martin', publishedYear: 2008, price: 89.90).save(failOnError: true)
            new Book(title: 'Grails in Action', author: 'Glen Smith', publishedYear: 2014, price: 120.00).save(failOnError: true)
        }
    }

    def destroy = {
    }
}
