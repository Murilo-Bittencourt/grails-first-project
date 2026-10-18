package br.com.murilocb

import groovy.transform.Synchronized
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class ContadorInseguro {
    int total = 0

    void incrementar() { total++ }
}

class ContadorSynchronized {
    int total = 0

    @Synchronized
    void incrementar() { total++ }
}

class ContadorComLock {
    int total = 0

    @WithWriteLock
    void incrementar() { total++ }

    @WithReadLock
    int ler() { total }
}

/**
 * Importante: esses locks só protegem dentro de UMA JVM.
 * Com várias instâncias da aplicação (ex.: reserva de assentos), é preciso
 * um lock distribuído, que é onde o Redis entra.
 */
class ConcurrencySpec extends Specification {

    static final int THREADS = 50
    static final int VEZES = 2000

    private static void rodarEmParalelo(Closure acao) {
        def pool = Executors.newFixedThreadPool(THREADS)
        def largada = new CountDownLatch(1)
        try {
            def futuros = (1..THREADS).collect {
                pool.submit({
                    largada.await()
                    VEZES.times { acao() }
                } as Callable)
            }
            largada.countDown()
            futuros*.get()
        } finally {
            pool.shutdown()
        }
    }

    def "@Synchronized não perde incrementos"() {
        given:
        def contador = new ContadorSynchronized()

        when:
        rodarEmParalelo { contador.incrementar() }

        then:
        contador.total == THREADS * VEZES
    }

    def "@WithWriteLock não perde incrementos"() {
        given:
        def contador = new ContadorComLock()

        when:
        rodarEmParalelo { contador.incrementar() }

        then:
        contador.ler() == THREADS * VEZES
    }

    def "sem proteção o total pode ficar errado (nunca passa do esperado)"() {
        given:
        def contador = new ContadorInseguro()

        when:
        rodarEmParalelo { contador.incrementar() }

        then: "não afirmamos '<' porque a corrida não é determinística; veja o valor no relatório"
        contador.total <= THREADS * VEZES
        println "ContadorInseguro: ${contador.total} de ${THREADS * VEZES}"
    }
}
