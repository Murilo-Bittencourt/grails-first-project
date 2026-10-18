package br.com.murilocb

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Specification

// Parâmetro tipado como Object: sem flow typing, o tipo estático é Object.
class Dispatch {
    static String quem(Object o) { 'Object' }
    static String quem(String s) { 'String' }
}

@CompileStatic
class Estatico { String chamar(Object x) { Dispatch.quem(x) } }

@TypeChecked
class Checado { String chamar(Object x) { Dispatch.quem(x) } }


class Dinamico { String chamar(Object x) { Dispatch.quem(x) } }

@CompileStatic
class MisturaEstaticoDinamico {
    @CompileDynamic
    String viaMetaClass() {
        String.metaClass.shout = { -> delegate.toUpperCase() }
        'oi'.shout()
    }
}

class CompileAnnotationsSpec extends Specification {

    private Class compila(String codigo) {
        new GroovyClassLoader(this.class.classLoader).parseClass(codigo)
    }

    // ---------- despacho em runtime ----------
    def "@CompileStatic escolhe o método pelo tipo declarado (Object)"() {
        expect:
        new Estatico().chamar('texto') == 'Object'
    }

    def "@TypeChecked NÃO muda o despacho: continua dinâmico (String)"() {
        expect:
        new Checado().chamar('texto') == 'String'
    }

    def "Groovy sem anotação é dinâmico (String)"() {
        expect:
        new Dinamico().chamar('texto') == 'String'
    }

    def "@CompileDynamic dentro de classe estática permite metaprogramação"() {
        expect:
        new MisturaEstaticoDinamico().viaMetaClass() == 'OI'
    }

    // ---------- compile-time ----------
    def "@CompileStatic rejeita método inexistente no build"() {
        when:
        compila '''
            import groovy.transform.CompileStatic
            @CompileStatic class A { def x() { this.naoExiste() } }
        '''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "@TypeChecked também rejeita método inexistente"() {
        when:
        compila '''
            import groovy.transform.TypeChecked
            @TypeChecked class A { def x() { this.naoExiste() } }
        '''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "sem anotação compila, e só falha em runtime"() {
        when:
        compila('class A { def x() { this.naoExiste() } }').newInstance().x()
        then:
        thrown(MissingMethodException)
    }

    // ---------- GORM ----------
    def "@CompileStatic não entende finder dinâmico do GORM"() {
        when:
        compila '''
            import groovy.transform.CompileStatic
            import br.com.murilocb.Book
            @CompileStatic class A { def x() { Book.findByTitle('x') } }
        '''
        then:
        thrown(MultipleCompilationErrorsException)
    }

    def "@GrailsCompileStatic entende finder dinâmico do GORM"() {
        when:
        compila '''
            import grails.compiler.GrailsCompileStatic
            import br.com.murilocb.Book
            @GrailsCompileStatic class A { def x() { Book.findByTitle('x') } }
        '''
        then:
        noExceptionThrown()
    }
}
