package br.com.murilocb

/**
 * MODEL (M) - Domain class do GORM.
 * Cada propriedade vira uma coluna na tabela BOOK (criada automaticamente
 * pelo dbCreate: create-drop do ambiente development).
 */
class Book {

    String title
    String author
    Integer publishedYear
    BigDecimal price

    // O GORM preenche esses dois campos sozinho
    Date dateCreated
    Date lastUpdated

    static constraints = {
        title blank: false, maxSize: 200
        author blank: false, maxSize: 120
        publishedYear nullable: true, min: 0, max: 2100
        price nullable: true, min: 0.0
    }

    static mapping = {
        table 'book'
    }
}
