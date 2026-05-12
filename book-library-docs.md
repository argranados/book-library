# Book Library API — Documentación de Aprendizaje

> Proyecto didáctico para aprender GraphQL con Spring Boot, construido fase por fase con énfasis en conceptos de entrevista y mejores prácticas.

---

## Descripción del Proyecto

API de biblioteca de libros construida con Spring Boot y Spring for GraphQL. El objetivo no es solo que funcione — es entender cada decisión de diseño, cada error encontrado, y cada concepto que se pregunta en entrevistas de Java backend con GraphQL en el stack.

### Stack

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 17 | Lenguaje |
| Spring Boot | 3.5.x | Framework principal |
| Spring for GraphQL | (incluido en Boot) | Engine GraphQL |
| Spring Data JPA | (incluido en Boot) | ORM |
| Spring Security | (incluido en Boot) | Autenticación y autorización |
| PostgreSQL | 16 | Base de datos |
| jjwt | 0.12.6 | Generación y validación de JWT |
| Lombok | (incluido en Boot) | Reducción de boilerplate |
| Docker Compose | — | Entorno local de DB |

### Dominio

Tres entidades con relaciones:

```
Author (1) ──── (*) Book (*) ──── (1) Genre
```

- Un `Author` puede tener muchos `Book`
- Un `Genre` puede tener muchos `Book`
- Un `Book` pertenece a exactamente un `Author` y un `Genre`

### Configuración local

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    container_name: book-library-db
    environment:
      POSTGRES_DB: book_library
      POSTGRES_USER: bookuser
      POSTGRES_PASSWORD: bookpass
    ports:
      - "5832:5432"
```

```properties
# application.properties
server.port=8090
spring.datasource.url=jdbc:postgresql://localhost:5832/book_library
spring.graphql.graphiql.enabled=true
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
```

### Operaciones GraphQL disponibles

**Queries públicas (sin token):**

| Query | Descripción |
|---|---|
| `books(filter: BookFilterInput)` | Lista de libros con filtros opcionales |
| `bookById(id: ID!)` | Un libro por ID |
| `booksByGenre(genreId: ID!)` | Libros filtrados por género |
| `booksConnection(first, after, last, before)` | Paginación Relay cursor-based |
| `authors` | Lista de autores |
| `authorById(id: ID!)` | Un autor por ID |
| `genres` | Lista de géneros |

**Mutations protegidas (requieren Bearer token):**

| Mutation | Descripción |
|---|---|
| `createBook(input: CreateBookInput!)` | Crear libro |
| `createAuthor(input: CreateAuthorInput!)` | Crear autor |
| `createGenre(input: CreateGenreInput!)` | Crear género |
| `deleteBook(id: ID!)` | Eliminar libro |

**Mutations públicas:**

| Mutation | Descripción |
|---|---|
| `register(input: RegisterInput!)` | Registrar usuario, devuelve JWT |
| `login(input: LoginInput!)` | Login, devuelve JWT |

---

## Estructura del Proyecto

```
src/main/java/com/ciberaccion/booklibrary/
├── BookLibraryApplication.java
├── author/
│   ├── Author.java
│   ├── AuthorController.java
│   ├── AuthorRepository.java
│   ├── AuthorService.java
│   └── CreateAuthorInput.java
├── book/
│   ├── Book.java
│   ├── BookController.java
│   ├── BookFilterInput.java
│   ├── BookRepository.java
│   ├── BookService.java
│   ├── BooksConnection.java
│   └── CreateBookInput.java
├── genre/
│   ├── Genre.java
│   ├── GenreController.java
│   ├── GenreRepository.java
│   ├── GenreService.java
│   └── CreateGenreInput.java
├── user/
│   ├── User.java
│   ├── UserRepository.java
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── AuthPayload.java
│   ├── LoginInput.java
│   ├── RegisterInput.java
│   └── Role.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   └── CustomUserDetailsService.java
├── config/
│   ├── GraphQLConfig.java
│   └── SecurityConfig.java
└── exception/
    ├── BookNotFoundException.java
    ├── AuthorNotFoundException.java
    └── GraphQLExceptionResolver.java

src/test/java/com/ciberaccion/booklibrary/
├── author/
│   └── AuthorControllerTest.java
└── book/
    ├── BookQueryTest.java
    └── BookMutationSecurityTest.java
```

---

## Fase 1 — Fundamentos de GraphQL

### Objetivo

Construir la base del proyecto: entidades, schema SDL, primeras queries y mutations. Entender las diferencias fundamentales con REST.

### Conceptos aprendidos

**Schema-first vs Code-first**
Spring for GraphQL usa schema-first: primero defines el contrato en `.graphqls`, luego implementas los resolvers en Java. El schema es la fuente de verdad.

**Diferencias clave con REST**

| REST | GraphQL |
|---|---|
| Múltiples endpoints (`/books`, `/authors`) | Un solo endpoint (`/graphql`) |
| El servidor decide qué campos devuelve | El cliente decide qué campos pide |
| `@RestController` + `@GetMapping` | `@Controller` + `@QueryMapping` |
| Over-fetching frecuente | Solo los campos pedidos |
| HTTP status codes para errores | Siempre HTTP 200, errores en body |

**Organización por dominio, no por capa**
A diferencia del patrón REST típico (`controllers/`, `services/`, `repositories/`), en GraphQL se organiza por dominio: todo lo de `Author` vive en el paquete `author/`.

**`record` para inputs**
Los tipos `input` del schema mapean a `record` Java. Son perfectos porque los inputs son inmutables por naturaleza.

```java
// El schema define:
input CreateAuthorInput {
    firstName: String!
    lastName: String!
    bio: String
}

// Java lo recibe como:
public record CreateAuthorInput(String firstName, String lastName, String bio) {}
```

**`@Controller` no `@RestController`**
`@RestController` agrega `@ResponseBody` que serializa a JSON para HTTP directamente. En GraphQL esa serialización la maneja el engine internamente — solo se necesita `@Controller`.

### Decisiones de diseño

**Scalar `Date` custom**
El schema usa el scalar `Date` para fechas. GraphQL no tiene un scalar de fecha incorporado — se usa la librería `graphql-java-extended-scalars` y se registra en `GraphQLConfig`:

```java
@Bean
public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder.scalar(ExtendedScalars.Date);
}
```

**`FetchType.EAGER` temporal en Fase 1**
Las relaciones `Book → Author` y `Book → Genre` se configuraron inicialmente con `EAGER` para que las queries funcionaran. Esto fue explícitamente una solución temporal — el problema real de N+1 se resuelve en Fase 2.

### Errores encontrados

**Error: INTERNAL_ERROR al hacer query con relaciones**

Al ejecutar una query que pedía `author.firstName` de un libro, la respuesta era:
```json
{
  "errors": [{ "message": "INTERNAL_ERROR", "path": ["books", 0, "author", "firstName"] }]
}
```

**Causa:** `Book` tenía `FetchType.LAZY` en las relaciones. Cuando GraphQL intentaba resolver `author.firstName`, la sesión de Hibernate ya estaba cerrada.

**Solución temporal (Fase 1):** cambiar a `FetchType.EAGER`.
**Solución definitiva (Fase 2):** `@BatchMapping` con `FetchType.LAZY`.

---

## Fase 2 — Relaciones y el Problema N+1

### Objetivo

Resolver el problema de rendimiento más importante en GraphQL: el N+1. Implementar `@BatchMapping` como solución y entender por qué es la forma correcta.

### Conceptos aprendidos

**El problema N+1**
Con `FetchType.EAGER`, por cada libro Hibernate lanzaba queries individuales:

```sql
-- 1 query para traer los libros
SELECT * FROM books

-- 1 query POR CADA libro para su author
SELECT * FROM authors WHERE id = 1
SELECT * FROM authors WHERE id = 2
SELECT * FROM authors WHERE id = 2  -- duplicada

-- 1 query POR CADA libro para su genre
SELECT * FROM genres WHERE id = 1
SELECT * FROM genres WHERE id = 2
```

Con 3 libros: 7 queries. Con 100 libros: 201 queries. El patrón es `1 + N + N`.

**`@BatchMapping` — la solución**
En lugar de resolver `author` libro por libro, Spring GraphQL agrupa todos los libros y llama al método una sola vez:

```java
@BatchMapping
public Map<Book, Author> author(List<Book> books) {
    return bookService.findBooksWithAuthors(books)
            .stream()
            .collect(Collectors.toMap(book -> book, Book::getAuthor));
}
```

El resultado: siempre 3 queries sin importar cuántos libros haya.

```sql
SELECT * FROM books                                    -- Query 1
SELECT * FROM books JOIN authors WHERE id IN (1,2,3)  -- Query 2
SELECT * FROM books JOIN genres WHERE id IN (1,2,3)   -- Query 3
```

### Errores encontrados

**Error: `LazyInitializationException` al implementar `@BatchMapping`**

Al cambiar de `EAGER` a `LAZY` e implementar `@BatchMapping`, al construir el `Map<Book, Author>` con un stream, Lombok intentaba acceder a los campos lazy de `Book` para calcular `hashCode()` y reventaba.

**Causa raíz:**
```
HashMap.put(book, author)
  → book.hashCode()         ← Lombok usa TODOS los campos
    → accede a book.author  ← proxy lazy
      → sesión cerrada 💥
```

**Solución:** `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en `Book`:

```java
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {
    @EqualsAndHashCode.Include
    @Id
    private Long id;  // solo el id, nunca toca proxies lazy
}
```

**Lección para entrevistas:** en JPA con Lombok, siempre usar `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en entidades con relaciones lazy. Es una best practice, no un workaround.

---

## Fase 3 — Filtros, Paginación y Manejo de Errores

### Objetivo

Agregar capacidades avanzadas de consulta: filtros combinables, paginación cursor-based (estándar Relay), y manejo de errores con clasificación propia.

### Conceptos aprendidos

**Filtros con `input` types**
Se agregó `BookFilterInput` para permitir filtros combinables opcionales:

```graphql
input BookFilterInput {
    title: String
    authorId: ID
    genreId: ID
}
```

La query JPQL usa `IS NULL` para hacer cada filtro opcional:

```java
@Query("""
    SELECT b FROM Book b
    WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
    AND (:authorId IS NULL OR b.author.id = :authorId)
    AND (:genreId IS NULL OR b.genre.id = :genreId)
""")
```

**Paginación Relay (Cursor-based)**
El estándar de facto en GraphQL. Lo usan GitHub, Shopify, Twitter. La estructura `Connection` define un contrato explícito:

```graphql
type BooksConnection {
    edges: [BookEdge!]!
    pageInfo: PageInfo!
}

type BookEdge {
    node: Book!
    cursor: String!    # posición opaca en Base64
}

type PageInfo {
    hasNextPage: Boolean!
    hasPreviousPage: Boolean!
    startCursor: String
    endCursor: String
}
```

El cursor se implementa como el `id` del libro en Base64 — opaco para el cliente pero decodificable en el servidor.

**Diferencia con paginación offset:**

| Offset (`page=2&size=10`) | Cursor (`after="cursor"&first=10`) |
|---|---|
| Inestable si se insertan registros | Estable — siempre el mismo punto |
| Salta registros en inserciones concurrentes | No se salta registros |
| Simple de implementar | Más complejo pero correcto |

**Manejo de errores en GraphQL**
GraphQL siempre devuelve HTTP 200 — los errores van dentro del body:

```json
{
  "data": { "bookById": null },
  "errors": [{
    "message": "Book not found: 999",
    "path": ["bookById"],
    "extensions": { "classification": "DataFetchingException" }
  }]
}
```

Se implementó `DataFetcherExceptionResolverAdapter` para clasificar errores de dominio:

```java
@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof BookNotFoundException || ex instanceof AuthorNotFoundException) {
            return GraphQLError.newError()
                    .errorType(ErrorType.DataFetchingException)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .build();
        }
        return null; // null = Spring maneja por default
    }
}
```

---

## Fase 4 — Seguridad con JWT

### Objetivo

Proteger mutations con JWT. Implementar registro, login, y el flujo completo de autenticación stateless. Entender por qué la seguridad en GraphQL funciona diferente a REST.

### Conceptos aprendidos

**Por qué GraphQL + Security es diferente a REST**

En REST, Spring Security protege por URL: `GET /books` es público, `POST /books` requiere token. En GraphQL **todo entra por `/graphql`** — no se puede distinguir por URL. La solución es `@PreAuthorize` a nivel de método:

```java
@QueryMapping
public List<Book> books() { ... }         // público — sin anotación

@PreAuthorize("isAuthenticated()")
@MutationMapping
public Book createBook(...) { ... }        // protegido
```

Esto requiere `@EnableMethodSecurity` en `SecurityConfig`.

**Flujo JWT implementado**

```
1. POST /graphql con mutation register/login
   → AuthService genera JWT con JwtService
   → Devuelve { token, username }

2. Requests posteriores incluyen header:
   Authorization: Bearer <token>

3. JwtAuthFilter (OncePerRequestFilter) intercepta cada request:
   → Extrae y valida el JWT
   → Si válido: pone UsernamePasswordAuthenticationToken en SecurityContext
   → Si inválido/ausente: continúa sin autenticar (Spring rechazará si necesita auth)

4. @PreAuthorize("isAuthenticated()") verifica el SecurityContext
```

**JWT stateless vs Cognito**
Conceptualmente, `JwtService` + `SecurityConfig` hacen lo mismo que Cognito: ser el Authorization Server. La diferencia:

| Cognito | JWT propio |
|---|---|
| Servicio externo gestionado | Código propio que gestionar |
| Alta disponibilidad garantizada | Responsabilidad tuya |
| Rotación de claves automática | Manual |
| Ideal para producción | Ideal para aprender el mecanismo |

**BCrypt y passwords**
Los passwords nunca se guardan en plain text. BCrypt aplica un salt aleatorio antes del hash, lo que significa que el mismo password produce hashes distintos cada vez — imposible de revertir.

### Errores encontrados

**Error 1: `Keys.hmacShaKey()` no existe**

Al usar jjwt 0.12.x con código de 0.11.x:
```
NoSuchMethodError: Keys.hmacShaKey()
```

**Causa:** breaking change entre versiones. En 0.12.x el método se renombró.

**Solución:**
```java
// 0.11.x (viejo)
Keys.hmacShaKey(keyBytes)

// 0.12.x (correcto)
Keys.hmacShaKeyFor(keyBytes)
```

**Error 2: Dependencia circular en Spring Context**

```
Error creating bean 'jwtAuthFilter': Circular reference
JwtAuthFilter → UserDetailsService
SecurityConfig → JwtAuthFilter
SecurityConfig → define UserDetailsService como @Bean
```

**Causa:** `SecurityConfig` definía `UserDetailsService` como `@Bean` dentro de sí mismo, y `JwtAuthFilter` necesitaba ese bean para construirse — creando un ciclo que Spring no puede resolver.

**Solución:** extraer `UserDetailsService` a su propia clase `CustomUserDetailsService` con `@Service`. Al ser un bean independiente, Spring puede inicializarlo antes de `SecurityConfig` y `JwtAuthFilter`, rompiendo el ciclo.

```java
@Service  // ← bean independiente, sin ciclo
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    // ...
}
```

**Lección para entrevistas:** los ciclos de dependencia en Spring son errores de diseño — la solución siempre es separar responsabilidades en beans independientes, nunca usar `@Lazy` como workaround.

---

## Fase 5 — Testing con Spring GraphQL Test

### Objetivo

Escribir tests unitarios y de slice para queries y mutations GraphQL, incluyendo tests de seguridad. Entender los límites de cada tipo de test.

### Conceptos aprendidos

**`@GraphQlTest` vs `@SpringBootTest`**

| Anotación | Qué carga | Velocidad | Uso |
|---|---|---|---|
| `@GraphQlTest` | Engine GraphQL + controllers indicados | ~2s | Unit/slice tests |
| `@SpringBootTest` | Todo el contexto Spring | ~6s+ | Integration tests |

`@GraphQlTest` es el equivalente a `@WebMvcTest` para GraphQL. No levanta base de datos — los services se mockean con `@MockitoBean`.

**`@Import` para configuraciones custom**
`@GraphQlTest` es un slice que excluye beans de `@Configuration` personalizados. Cuando el schema usa scalars custom, hay que importar explícitamente la config:

```java
@GraphQlTest(AuthorController.class)
@Import(GraphQLConfig.class)  // necesario para registrar ExtendedScalars.Date
class AuthorControllerTest { ... }
```

**`GraphQlTester` — API fluida**
```java
graphQlTester.document("""
    query {
        authorById(id: "1") {
            firstName
        }
    }
    """)
    .execute()
    .path("authorById.firstName")    // JsonPath-style navigation
    .entity(String.class)
    .isEqualTo("Robert");
```

**`@WithMockUser` para tests de seguridad**
Pone un `UsernamePasswordAuthenticationToken` falso en el `SecurityContextHolder` — simula un usuario autenticado sin generar JWT real ni tocar la DB:

```java
@Test
@WithMockUser    // simula usuario autenticado
void createBook_conAutenticacion_retornaBook() { ... }
```

### Estructura de tests implementados

**`AuthorControllerTest`** — queries públicas

```java
@GraphQlTest(AuthorController.class)
@Import(GraphQLConfig.class)
class AuthorControllerTest {
    // authors() → lista de autores
    // authorById() con id existente → devuelve autor
    // authorById() con id inexistente → devuelve null
}
```

**`BookQueryTest`** — queries de books con BatchMapping

```java
@GraphQlTest(BookController.class)
@Import(GraphQLConfig.class)
class BookQueryTest {
    // books() → requiere mockear findWithFilters, findBooksWithAuthors, findBooksWithGenres
}
```

**`BookMutationSecurityTest`** — mutations sin autenticación

```java
@GraphQlTest(BookController.class)
@Import({GraphQLConfig.class, BookMutationSecurityTest.SecurityTestConfig.class})
class BookMutationSecurityTest {
    @Configuration
    @EnableMethodSecurity
    static class SecurityTestConfig {}
    // createBook sin auth → devuelve error
}
```

### Errores encontrados y lecciones

**Error 1: scalar `Date` no encontrado en tests**

```
errors=[There is no scalar implementation for the named 'Date' scalar type]
```

**Causa:** `@GraphQlTest` carga el schema pero no incluye `GraphQLConfig` donde se registra el scalar.
**Solución:** `@Import(GraphQLConfig.class)` en la clase de test.

**Error 2: `@EnableMethodSecurity` interfiere con Mockito**

Al agregar `@EnableMethodSecurity` al slice para testear `@PreAuthorize`, los proxies AOP creados por Spring interferían con cómo Mockito interceptaba las llamadas — los mocks devolvían null aunque estuvieran configurados.

**Conclusión:** para testear seguridad real en GraphQL el enfoque correcto es `@SpringBootTest` con contexto completo. `@GraphQlTest` es ideal para lógica de negocio, no para tests de seguridad.

**Lo que se puede hacer con `@GraphQlTest` + Security:**
- Verificar que una mutation sin auth devuelve error ✅
- Verificar que una query pública funciona sin auth ✅

**Lo que requiere `@SpringBootTest`:**
- Verificar el flujo completo con JWT real ✅
- Tests de integración end-to-end ✅

---

## Conceptos Clave para Entrevistas

### GraphQL

**¿Cuándo usar GraphQL sobre REST?**
Cuando el cliente necesita flexibilidad en los campos que pide, cuando hay múltiples clientes (mobile/web) con necesidades distintas, o cuando hay relaciones complejas entre entidades que causarían múltiples roundtrips en REST.

**¿Qué es el problema N+1?**
Por cada entidad en una lista, se lanza una query extra para resolver sus relaciones. Con N libros = N+1 queries para authors + N+1 para genres. Se resuelve con `@BatchMapping` en Spring GraphQL o DataLoader en otros ecosistemas.

**¿Por qué `@PreAuthorize` en lugar de URL-based security?**
En GraphQL todo entra por `/graphql` — no es posible distinguir operaciones por URL. La seguridad debe estar a nivel de método para poder proteger mutations específicas dejando queries públicas.

**¿Qué es cursor-based pagination vs offset?**
Offset (`page=2&size=10`) es inestable en inserciones concurrentes — puede saltar registros. Cursor-based usa un puntero opaco a un elemento específico, garantizando consistencia. Es el estándar Relay que usan GitHub, Shopify y Twitter.

### Spring Security + JWT

**¿Qué hace `OncePerRequestFilter`?**
Garantiza que el filtro se ejecute exactamente una vez por request HTTP, incluso si hay forwards o includes internos. `JwtAuthFilter` extiende de esto para interceptar y validar el token en cada request.

**¿Por qué JWT es stateless?**
El servidor no necesita guardar el token en ningún lado — toda la información (usuario, expiración, firma) está en el token mismo. Cada request es autosuficiente.

**¿Cómo se resuelve una dependencia circular en Spring?**
Separando responsabilidades en beans independientes. Nunca con `@Lazy` — es un parche, no una solución.

---

## Referencia Rápida de Queries

```graphql
# Todas las queries públicas
query { books { id title author { firstName } genre { name } } }
query { bookById(id: "1") { title } }
query { authors { id firstName lastName } }
query { genres { id name } }

# Filtros
query { books(filter: { title: "dune", genreId: "2" }) { title } }

# Paginación
query {
    booksConnection(first: 5) {
        edges { node { title } cursor }
        pageInfo { hasNextPage endCursor }
    }
}

# Autenticación
mutation { register(input: { username: "user", password: "pass" }) { token } }
mutation { login(input: { username: "user", password: "pass" }) { token } }

# Mutations protegidas (requieren Authorization: Bearer <token>)
mutation { createAuthor(input: { firstName: "Robert" lastName: "Martin" }) { id } }
mutation { createBook(input: { title: "..." isbn: "..." authorId: "1" genreId: "1" }) { id } }
mutation { deleteBook(id: "1") }
```
