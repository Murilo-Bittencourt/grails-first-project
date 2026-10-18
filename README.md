# grails-first-project

Primeiro projeto em **Grails** feito para estudo: uma API REST simples (CRUD de livros) em arquitetura **MVC**, mais um conjunto de testes Spock usados para entender na prática como o Groovy se comporta (tipagem estática x dinâmica, transformações AST, concorrência).

## Stack

- Grails 8.0.0 (perfil `rest-api`) · Groovy · Java 21
- GORM (Hibernate 5) com H2 em memória
- Views JSON com `.gson`
- Gradle 9.8 (wrapper incluso) · Spock para testes

## Estrutura (MVC)

| Camada | Arquivo |
| --- | --- |
| Model (domain/GORM) | `grails-app/domain/br/com/murilocb/Book.groovy` |
| Service (regras e acesso ao banco) | `grails-app/services/br/com/murilocb/BookService.groovy` |
| Controller | `grails-app/controllers/br/com/murilocb/BookController.groovy` |
| View (JSON) | `grails-app/views/book/*.gson` |
| Rotas REST | `grails-app/controllers/br/com/murilocb/UrlMappings.groovy` |
| Dados iniciais | `grails-app/init/br/com/murilocb/BootStrap.groovy` |

`grails-app/` guarda os artefatos que o Grails descobre por convenção (domain, controllers, services, views). `src/` segue o padrão do Gradle: classes de apoio em `src/main`, testes em `src/test` e `src/integration-test`.

## Como rodar

```bash
./gradlew bootRun        # Windows: .\gradlew.bat bootRun
```

A aplicação sobe em http://localhost:8080. O banco é H2 em memória (`create-drop`), então os dados somem ao reiniciar. O `BootStrap` insere dois livros de exemplo.

## API

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/book` | Lista os livros |
| GET | `/book/{id}` | Busca um livro (404 se não existir) |
| POST | `/book` | Cria (201; 422 se inválido) |
| PUT | `/book/{id}` | Atualiza (200; 404; 422) |
| DELETE | `/book/{id}` | Remove (204; 404) |

```bash
curl -X POST http://localhost:8080/book \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","publishedYear":2008,"price":89.90}'
```

Campos do `Book`: `title` e `author` (obrigatórios), `publishedYear`, `price`; `dateCreated` e `lastUpdated` são preenchidos pelo GORM.

## Testes

```bash
./gradlew test                 # specs unitárias
./gradlew integrationTest      # specs de integração
./gradlew test --tests "br.com.murilocb.CompileAnnotationsSpec"
```

| Spec | O que explora |
| --- | --- |
| `CompileAnnotationsSpec` | `@CompileStatic`, `@TypeChecked`, `@CompileDynamic`, `@GrailsCompileStatic`: erros em compile-time e despacho em runtime |
| `GroovyTransformsSpec` | `@Memoized`, `@Lazy`, `@Immutable`, `@Canonical`, `@Delegate` |
| `ConcurrencySpec` | `@Synchronized`, `@WithWriteLock` com várias threads |
| `BookServiceIntegrationSpec` | `BookService` com H2 real, `@Rollback` e `@Stepwise` |

## Aprendizados

- **Convenção sobre configuração.** No Grails a pasta onde o arquivo está define o que ele é: não há registro manual de controller, service ou domain. Isso deixa o MVC enxuto: domain (GORM) → service (`@Transactional`) → controller (`respond`) → view `.gson`.
- **Tipagem estática x dinâmica não é só "checar tipos".** `@CompileStatic` rejeita código inválido no build *e* escolhe o método pelo tipo declarado. `@TypeChecked` também rejeita no build, mas o despacho continua dinâmico, escolhendo pelo tipo real do objeto. `@GrailsCompileStatic` é o que entende finders e outras construções do GORM.
- **Flow typing pega de surpresa.** Em `Object x = 'texto'`, o compilador estático lembra que `x` recebeu uma `String` e chama a sobrecarga de `String`. Para observar o despacho pelo tipo declarado, o valor precisa vir de um parâmetro `Object`. Foi um teste meu que falhou e me mostrou isso.
- **Dá para testar o compilador.** Compilar código em `String` com `GroovyClassLoader.parseClass` e esperar `MultipleCompilationErrorsException` permite testar o que o build rejeita sem quebrar o próprio build.
- **Transformações AST reduzem boilerplate, mas têm regras.** `@Memoized` cacheia por argumento, `@Lazy` adia a inicialização, `@Immutable` impede alterações e gera `equals`/`hashCode` por valor, `@Delegate` repassa métodos.
- **Lock local não resolve concorrência distribuída.** `@Synchronized` e `@WithWriteLock` protegem uma única JVM. Com várias instâncias da aplicação (por exemplo, reserva de assentos de cinema), é preciso um lock distribuído, como o do Redis. Esse é o gancho para o próximo projeto.

## Referências

- [Guia do Grails 8](https://grails.apache.org/docs/8.0.0/guide/index.html)
- [API Reference](https://grails.apache.org/docs/8.0.0/api/index.html)
- [Grails JSON Views](https://grails.apache.org/docs/8.0.0/guide/theWebLayer.html)
- [Spock](https://spockframework.org/)
