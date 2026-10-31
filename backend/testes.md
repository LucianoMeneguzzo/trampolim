# Guia Completo de Testes da API - Trampolim

Este guia contém todos os comandos para testar os endpoints da API.

> **💡 Dica de Ambiente:**
> - Se NÃO houver um arquivo `.env` na pasta `backend`, a API sobe automaticamente com banco H2 em memória e dados de exemplo (Mentor: `mentor@trampolim.local` / Aluno: `student@trampolim.local`, senha para ambos: `senha-teste-123`).
> - Se houver um arquivo `.env` configurado, a API conecta ao seu banco PostgreSQL (ex: Supabase).
> - Os comandos `curl` abaixo estão em linha única para facilitar a cópia em qualquer terminal (Bash, Zsh, Fish, PowerShell).

---

## 📋 Resumo Rápido dos Endpoints e Permissões

| Método | Rota | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/usuarios` | Registrar novo usuário | Público |
| `POST` | `/api/auth/login` | Login e obter token | Público |
| `POST` | `/api/desafios` | Criar desafio | Apenas `MENTOR` |
| `GET` | `/api/desafios` | Listar todos os desafios | Autenticado |
| `GET` | `/api/desafios/{id}` | Buscar desafio por ID | Autenticado |
| `POST` | `/api/candidaturas` | Candidatar-se | Apenas `STUDENT` |
| `GET` | `/api/candidaturas/{id}` | Buscar candidatura por ID | Autenticado |
| `GET` | `/api/candidaturas/desafio/{id}` | Listar candidaturas de um desafio | Autenticado |
| `PUT` | `/api/candidaturas/{id}/envio` | Aluno envia link do projeto | Apenas `STUDENT` (dono da candidatura) |
| `PATCH` | `/api/candidaturas/{id}/status` | Aprovar ou rejeitar candidatura | Apenas `MENTOR` |
| `POST` | `/api/candidaturas/{id}/avaliacao` | Mentor avalia a entrega | Apenas `MENTOR` (criador do desafio) |

---

## 🔐 PASSO 1: Gerar Tokens de Acesso

Como as rotas possuem permissões específicas, precisamos de tokens para ambos os perfis.

**Login do Mentor:**

```bash
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email": "mentor@trampolim.local", "password": "senha-teste-123"}'
```

Copie o valor do campo `token` da resposta e use como `TOKEN_MENTOR` nos próximos passos.

**Login do Aluno (Student):**

```bash
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email": "student@trampolim.local", "password": "senha-teste-123"}'
```

Copie o valor do campo `token` da resposta e use como `TOKEN_STUDENT` nos próximos passos.

**Resposta esperada do login:**

```json
{
  "userId": 1,
  "name": "Mentor Trampolim",
  "type": "MENTOR",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

---

## 🎯 PASSO 2: Criar um Novo Desafio (Apenas MENTOR)

Substitua `TOKEN_MENTOR` pelo token gerado no Passo 1.

```bash
curl -i -X POST http://localhost:8080/api/desafios -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"titulo": "API REST com Spring Boot", "descricao": "Desenvolver um backend completo usando Java, Spring Boot e PostgreSQL.", "tecnologias": "Java, Spring Boot, PostgreSQL, JWT", "nivel": "INTERMEDIARIO", "prazo": "2026-12-31", "vagas": 5, "empresa": "TechCorp"}'
```

*Esperado: HTTP 201 Created com o desafio criado e o campo `nomeCriador` preenchido.*

---

## 🔍 PASSO 3: Listar e Buscar Desafios

**Listar todos os desafios:**

```bash
curl -i -X GET http://localhost:8080/api/desafios -H "Authorization: Bearer TOKEN_MENTOR"
```

**Buscar desafio específico por ID (rota nova):**

```bash
curl -i -X GET http://localhost:8080/api/desafios/1 -H "Authorization: Bearer TOKEN_MENTOR"
```

*Esperado: HTTP 200 com o JSON do desafio, incluindo `tecnologias`, `prazo`, `vagas` e `empresa`.*

---

## 📝 PASSO 4: Candidatar-se a um Desafio (Apenas STUDENT)

Substitua `TOKEN_STUDENT` pelo token do aluno.

```bash
curl -i -X POST http://localhost:8080/api/candidaturas -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_STUDENT" -d '{"desafioId": 2}'
```

*Esperado: HTTP 201 Created com `status: "PENDENTE"`. Use o desafio 2, pois o aluno de exemplo já está inscrito no desafio 1.*

---

## ⚙️ PASSO 5: Gerenciar Candidaturas

**Listar candidaturas de um desafio específico:**

```bash
curl -i -X GET http://localhost:8080/api/candidaturas/desafio/1 -H "Authorization: Bearer TOKEN_MENTOR"
```

**Buscar candidatura específica por ID (rota nova):**

```bash
curl -i -X GET http://localhost:8080/api/candidaturas/1 -H "Authorization: Bearer TOKEN_STUDENT"
```

**Aprovar candidatura (Apenas MENTOR):**

```bash
curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"status": "APROVADO"}'
```

**Rejeitar candidatura (teste alternativo):**

```bash
curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"status": "REJEITADO"}'
```

---

## 🚀 PASSO 6: Novas Rotas de Fluxo (Envio e Avaliação)

**Aluno envia o link do projeto (Apenas STUDENT dono da candidatura):**

```bash
curl -i -X PUT http://localhost:8080/api/candidaturas/1/envio -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_STUDENT" -d '{"linkEnvio": "https://github.com/aluno/projeto-final"}'
```

*Esperado: HTTP 200 com `status: "ENVIADO"`, `linkEnvio` e `dataEnvio` preenchidos.*

**Mentor avalia a candidatura (Apenas MENTOR criador do desafio):**

```bash
curl -i -X POST http://localhost:8080/api/candidaturas/1/avaliacao -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"nota": 9, "feedback": "Excelente trabalho, código muito limpo e bem estruturado!"}'
```

*Esperado: HTTP 201 Created. Resposta:*

```json
{
  "id": 1,
  "nota": 9,
  "feedback": "Excelente trabalho, código muito limpo e bem estruturado!",
  "dataAvaliacao": "2026-10-31T09:49:54.190197",
  "nomeMentor": "Mentor Trampolim",
  "emailMentor": "mentor@trampolim.local",
  "candidaturaId": 1,
  "nomeCandidato": "Aluno Trampolim",
  "tituloDesafio": "API REST com Spring Boot"
}
```

---

## 🧪 PASSO 7: Testes de Erro e Segurança

### Teste A: Candidatura duplicada

Rode o comando abaixo duas vezes seguidas com o mesmo `desafioId`. A segunda deve ser rejeitada:

```bash
curl -i -X POST http://localhost:8080/api/candidaturas -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_STUDENT" -d '{"desafioId": 1}'
```

*Resposta esperada:*

```json
{
  "erro": "Você já se candidatou para este desafio."
}
```

### Teste B: Aluno tenta avaliar (deve retornar 403 Forbidden)

Use o `TOKEN_STUDENT` na rota de avaliação para provar que o `SecurityConfig` bloqueia:

```bash
curl -i -X POST http://localhost:8080/api/candidaturas/1/avaliacao -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_STUDENT" -d '{"nota": 10, "feedback": "Tentativa de invasão"}'
```

*Resposta esperada:*

```json
{
  "timestamp": "2026-10-31T12:50:08.956+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/candidaturas/1/avaliacao"
}
```

### Teste C: Mentor tenta se candidatar (deve retornar 403 Forbidden)

```bash
curl -i -X POST http://localhost:8080/api/candidaturas -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"desafioId": 2}'
```

*Esperado: HTTP 403, pois a rota exige `ROLE_STUDENT`.*

### Teste D: Aluno tenta criar desafio (deve retornar 403 Forbidden)

```bash
curl -i -X POST http://localhost:8080/api/desafios -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_STUDENT" -d '{"titulo": "Teste", "descricao": "Teste", "tecnologias": "Java", "nivel": "INICIANTE", "prazo": "2026-12-31", "vagas": 1, "empresa": "Teste"}'
```

*Esperado: HTTP 403, pois a rota exige `ROLE_MENTOR`.*

### Teste E: Validação de campos obrigatórios (deve retornar 400)

```bash
curl -i -X POST http://localhost:8080/api/desafios -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"titulo": "", "descricao": "Teste", "tecnologias": "Java", "nivel": "INICIANTE", "prazo": "2026-12-31", "vagas": 1, "empresa": "Teste"}'
```

*Resposta esperada:*

```json
{
  "titulo": "O título é obrigatório"
}
```

### Teste F: Nota fora do intervalo 0 a 10 (deve retornar 400)

```bash
curl -i -X POST http://localhost:8080/api/candidaturas/1/avaliacao -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"nota": 15, "feedback": "Nota inválida"}'
```

*Resposta esperada:*

```json
{
  "nota": "A nota deve ser no máximo 10"
}
```

### Teste G: Avaliação duplicada

Tente avaliar a mesma candidatura duas vezes com o `TOKEN_MENTOR`:

```bash
curl -i -X POST http://localhost:8080/api/candidaturas/1/avaliacao -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"nota": 8, "feedback": "Segunda avaliação"}'
```

*Resposta esperada:*

```json
{
  "erro": "Esta candidatura já foi avaliada."
}
```

### Teste H: Status inválido (deve retornar 400)

```bash
curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN_MENTOR" -d '{"status": "BANANA"}'
```

*Resposta esperada:*

```json
{
  "erro": "Status inválido. Use apenas 'APROVADO' ou 'REJEITADO'."
}
```

### Teste I: Acesso sem token (deve retornar 403)

```bash
curl -i -X GET http://localhost:8080/api/desafios
```

*Esperado: HTTP 403 Forbidden.*

---

## 🖥️ Console do H2 (apenas quando rodando sem `.env`)

Acesse http://localhost:8080/h2-console para inspecionar as tabelas:

- **JDBC URL:** `jdbc:h2:mem:trampolimdb`
- **Usuário:** `sa`
- **Senha:** (deixe em branco)

Tabelas disponíveis: `usuarios`, `desafios`, `candidaturas`, `avaliacoes`.

---

## ✅ Checklist de Validação

- [ ] Login do mentor retorna token e `type: "MENTOR"`
- [ ] Login do aluno retorna token e `type: "STUDENT"`
- [ ] Mentor cria desafio com os novos campos (tecnologias, prazo, vagas, empresa)
- [ ] `GET /api/desafios/{id}` retorna o desafio correto
- [ ] Aluno se candidata e recebe `status: "PENDENTE"`
- [ ] `GET /api/candidaturas/{id}` retorna a candidatura correta
- [ ] Aluno envia projeto e o status muda para `"ENVIADO"`
- [ ] Mentor avalia e a avaliação é registrada com nota e feedback
- [ ] Aluno é bloqueado (403) ao tentar avaliar
- [ ] Mentor é bloqueado (403) ao tentar se candidatar
- [ ] Candidatura duplicada é rejeitada
- [ ] Avaliação duplicada é rejeitada
- [ ] Nota fora de 0 a 10 é rejeitada