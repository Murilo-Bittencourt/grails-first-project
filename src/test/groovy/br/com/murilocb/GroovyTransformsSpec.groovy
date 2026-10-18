package br.com.murilocb

import groovy.transform.Canonical
import groovy.transform.Immutable
import groovy.transform.Memoized
import spock.lang.Specification

class Calculadora {
    int chamadas = 0

    @Memoized
    int dobro(int n) {
        chamadas++
        n * 2
    }
}

class ComLazy {
    static int inicializacoes = 0

    @Lazy List<String> itens = { inicializacoes++; ['a', 'b'] }()
}

@Immutable
class Assento {
    String fila
    int numero
}

@Canonical
class Ponto {
    int x
    int y
}

class Motor {
    String ligar() { 'ligado' }
}

class Carro {
    @Delegate Motor motor = new Motor()
}

class GroovyTransformsSpec extends Specification {

    def setup() {
        ComLazy.inicializacoes = 0
    }

    def "@Memoized executa o corpo só uma vez por argumento"() {
        given:
        def calc = new Calculadora()

        when:
        def a = calc.dobro(4)
        def b = calc.dobro(4)

        then:
        a == 8
        b == 8
        calc.chamadas == 1

        when: "argumento diferente executa de novo"
        calc.dobro(5)

        then:
        calc.chamadas == 2
    }

    def "@Lazy só inicializa no primeiro acesso"() {
        when:
        def obj = new ComLazy()

        then:
        ComLazy.inicializacoes == 0

        when:
        def a = obj.itens
        def b = obj.itens

        then:
        ComLazy.inicializacoes == 1
        a == ['a', 'b']
        a.is(b)
    }

    def "@Immutable não permite alterar propriedades"() {
        given:
        def assento = new Assento('A', 1)

        when:
        assento.numero = 2

        then:
        thrown(ReadOnlyPropertyException)
        assento.numero == 1
    }

    def "@Immutable gera equals e hashCode por valor"() {
        expect:
        new Assento('A', 1) == new Assento('A', 1)
        new Assento('A', 1) != new Assento('A', 2)
        ([new Assento('B', 3), new Assento('B', 3)] as Set).size() == 1
    }

    def "@Canonical gera construtor, equals, hashCode e toString"() {
        expect:
        new Ponto(1, 2) == new Ponto(1, 2)
        new Ponto(1, 2).hashCode() == new Ponto(1, 2).hashCode()
        new Ponto(1, 2).toString().endsWith('(1, 2)')
        ([new Ponto(1, 2), new Ponto(1, 2)] as Set).size() == 1
    }

    def "@Delegate expõe os métodos do objeto interno"() {
        expect:
        new Carro().ligar() == 'ligado'
    }
}
