# projetoSpring

API REST de **gestão escolar** (estudantes, professores, turmas e usuários), construída passo a passo em um curso de
**Desenvolvimento de Backend com Spring Boot**. Cada pasta é o projeto completo no ponto em que uma aula termina,
então dá para comparar uma etapa com a seguinte.

## Material de estudo

A pasta [`Material/`](Material) reúne, em PDF, as 22 aulas (slides `SB01` a `SB22`) e os dois documentos de apoio com o
código completo (`SB21.1` e `SB22.1`).

## Tecnologias

- Java 25 (LTS) e Spring Boot 4.1.2 (Spring Framework 7)
- Spring Web MVC, Spring Data JPA (Hibernate), Bean Validation
- H2 (desenvolvimento) e MySQL (produção)
- Lombok, ModelMapper, springdoc (Swagger UI)
- Spring Security (HTTP Basic e, depois, JWT), Flyway, JUnit 5, Mockito

## Projetos por etapa do curso

| Pasta | Aulas | O que tem |
|---|---|---|
| `projeto-referencia-aula16/` | 1 a 16 | CRUD de estudante, professor e turma; camadas (entity, repository, service, resource); DTO e ModelMapper; tratamento de erros; Swagger; perfis `dev` e `prod` |
| `projeto-referencia/` | 17 e 18 | Relacionamentos (1:1, 1:N, N:N) e Spring Security com HTTP Basic |
| `projeto-referencia-aula19/` | 19 | Testes automatizados (38 testes) |
| `projeto-referencia-aula20/` | 20 | Migrações com Flyway (42 testes) |
| `projeto-referencia-aula21/` | 21 | JPQL, Specification, N+1, transações, `@Version`, auditoria e exclusão lógica (56 testes) |
| `projeto-referencia-aula22/` | 22 | Login com JWT, papéis e permissões em tabelas, cadastro de usuários e CORS (75 testes) |

A última pasta (`projeto-referencia-aula22/`) é a versão mais completa.

## Como executar

Requisitos: JDK 25. O Maven vem junto, pelo wrapper (`mvnw`).

```bash
cd projeto-referencia-aula22
./mvnw spring-boot:run        # no Windows: mvnw.cmd spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Console do H2: http://localhost:8080/h2-console (URL `jdbc:h2:mem:testdb;MODE=MySQL`, usuário `sa`, senha em branco)

Por padrão o perfil ativo é `dev` (H2 em memória). Ao iniciar, a classe `Init` carrega dados de exemplo.

### Usuários de teste (somente didáticos)

| Usuário | Senha | Papel |
|---|---|---|
| `admin` | `admin123` | ADMIN (leitura, escrita e gestão de usuários) |
| `aluno` | `aluno123` | USER (somente leitura) |

Na aula 22, faça login em `POST /auth/login` e use o token no cabeçalho `Authorization: Bearer <token>`.
Nas aulas 18 a 21, a autenticação é HTTP Basic.

### Testes

```bash
./mvnw test
```

## Perfil de produção (MySQL)

O perfil `prod` usa as variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD` e, a partir da aula 22, `JWT_SECRET`
(sem valor padrão). Ative com `spring.profiles.active=prod` ou `SPRING_PROFILES_ACTIVE=prod`.

> Atenção: o perfil `prod` ainda não foi executado contra um MySQL real; os testes rodam no H2 (em modo MySQL).
> Os usuários de teste e a chave JWT de desenvolvimento não devem ser usados em produção.

## Autor

Jefferson Brandão
