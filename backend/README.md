# Documentação da API - Trampolim

Esta documentação descreve as rotas disponíveis na API do projeto Trampolim: autenticação (cadastro/login com JWT), desafios, candidaturas e avaliações.

## ⚙️ Como rodar

Precisa do **Java 21 ou mais novo** (`java -version`). O Maven não precisa ser instalado: o `./mvnw` baixa sozinho.

1. Na pasta `backend`:

        ./mvnw spring-boot:run

   No Windows (PowerShell ou CMD) também funciona `.\mvnw spring-boot:run`.

2. **Sem o `backend/.env`** a API sobe com banco em memória (H2) e uma chave de JWT aleatória. Isso basta para testar e revisar, mas os dados e os logins somem quando o backend para. O console avisa com `Banco em memória (H2)`. Nesse modo o backend já cria dados de exemplo: entre como `mentor@trampolim.local` / `senha-teste-123` e abra `/business/assessment/1` (a aluna é `aluna@trampolim.local`, mesma senha).
3. **Para usar o Supabase**, copie `backend/.env.example` para `backend/.env` e preencha a senha do banco e a chave do JWT (peça ao time). O `.env` é ignorado pelo git.
4. Testes (usam banco H2 em memória, não mexem no Supabase):

        ./mvnw test

5. Para ver a tela de avaliação sem ter a tela do aluno pronta, `scripts/criar-dados-de-teste.ps1` cria uma aluna inscrita no desafio mais recente, com repositório enviado, e mostra o link. Use só com o banco em memória (H2), nunca com o Supabase.

A API sobe em `http://localhost:8080` e aceita chamadas do front em `http://localhost:5173` (CORS configurável em `CORS_ALLOWED_ORIGINS`).

---

## 🗂️ Modelo de dados

    usuarios      id, nome, email (único), senha (hash BCrypt), tipo (STUDENT | MENTOR), bio, github, linkedin, titulo_profissional, criado_em
    desafios      id, criador_id → usuarios, titulo, descricao, nivel, tecnologias, empresa_nome, prazo_dias, vagas, status (ABERTO | FECHADO), data_criacao
    candidaturas  id, candidato_id → usuarios, desafio_id → desafios, status, link_repositorio, repo_linguagem, repo_ultimo_commit, data_candidatura, data_envio
    avaliacoes    id, candidatura_id → candidaturas (única), mentor_id → usuarios, nota (0.0–10.0), feedback, data_avaliacao

Status da candidatura: `PENDENTE` → (`APROVADO` | `REJEITADO`) → `ENVIADO` (aluno mandou o repositório) → `AVALIADO` (mentor deu nota).

---

## 🔐 Autenticação (JWT)

1. `POST /api/auth/cadastro` cria a conta. A senha é gravada só como hash BCrypt.
2. `POST /api/auth/login` confere e-mail + senha e devolve um token JWT assinado (HS256), válido por 24h.
3. Nas rotas protegidas, mande o token no cabeçalho:

        Authorization: Bearer SEU_TOKEN_AQUI

4. O `JwtAuthenticationFilter` valida o token a cada requisição e identifica o usuário. O tipo do usuário vira o papel `ROLE_STUDENT` ou `ROLE_MENTOR`, usado no `SecurityConfig` para liberar cada rota.

Payload do token: `sub` (e-mail), `userId`, `type`, `iat`, `exp`.

| Situação | Resposta |
|---|---|
| Sem token, token inválido ou expirado em rota protegida | `401 { "erro": "Faça login para continuar." }` |
| Logado, mas com o perfil errado (ex.: aluno criando desafio) | `403 { "erro": "Seu perfil não tem permissão para esta ação." }` |

### Quem acessa o quê

| Rota | Acesso |
|---|---|
| `POST /api/auth/cadastro`, `POST /api/auth/login` | Público |
| `GET /api/desafios`, `GET /api/desafios/{id}` | Público |
| `POST /api/desafios` | MENTOR |
| `GET /api/candidaturas/desafio/{id}`, `PATCH /api/candidaturas/{id}/status`, `POST /api/candidaturas/{id}/avaliacao` | MENTOR dono do desafio |
| `POST /api/candidaturas`, `PUT /api/candidaturas/{id}/envio` | STUDENT (no envio, só o dono da inscrição) |
| `GET /api/candidaturas/{id}` | Mentor dono do desafio ou o próprio aluno |
| `GET /api/usuarios/me` | Qualquer usuário logado |

---

## 📌 1. Autenticação e Usuários

### 1.1. Cadastrar

    POST /api/auth/cadastro

    {
      "name": "Nome do Usuário",
      "email": "usuario@exemplo.com",
      "password": "senha-com-8-ou-mais",
      "type": "MENTOR"
    }

`type`: `MENTOR` ou `STUDENT`. Senha: 8 a 72 caracteres. O e-mail é salvo em minúsculas.

- `201` com o usuário criado (sem senha)
- `400` campo inválido, ex.: `{ "password": "A senha precisa ter entre 8 e 72 caracteres" }`
- `409` `{ "erro": "Este e-mail já está cadastrado." }`

### 1.2. Login

    POST /api/auth/login

    { "email": "usuario@exemplo.com", "password": "senha-com-8-ou-mais" }

- `200`

        {
          "userId": 1,
          "name": "Nome do Usuário",
          "type": "MENTOR",
          "token": "eyJhbGciOiJIUzI1NiJ9..."
        }

- `401` `{ "erro": "E-mail ou senha inválidos." }`

### 1.3. Meus dados

    GET /api/usuarios/me     (com token)

---

## 🚀 2. Desafios

### 2.1. Criar desafio (tela `/business/post-challenge`)

    POST /api/desafios     (MENTOR)

    {
      "title": "API de pedidos para uma pequena loja",
      "description": "A loja precisa sair da planilha...",
      "level": "INTERMEDIARIO",
      "technologies": ["Java", "Spring Boot", "MySQL"],
      "deadlineDays": 7,
      "slots": 20,
      "companyName": "Loja Horizonte"
    }

`level`: `INICIANTE`, `INTERMEDIARIO` ou `AVANCADO`. `technologies`, `deadlineDays` (padrão 7), `slots` e `companyName` são opcionais.

Resposta `201`:

    {
      "id": 1,
      "titulo": "API de pedidos para uma pequena loja",
      "descricao": "A loja precisa sair da planilha...",
      "nivel": "INTERMEDIARIO",
      "tecnologias": ["Java", "Spring Boot", "MySQL"],
      "empresaNome": "Loja Horizonte",
      "prazoDias": 7,
      "vagas": 20,
      "status": "ABERTO",
      "dataCriacao": "2026-10-27T10:00:00",
      "nomeCriador": "Nome do Mentor"
    }

### 2.2. Listar e detalhar

    GET /api/desafios          (público)
    GET /api/desafios/{id}     (público, 404 se não existir)

---

## 📝 3. Candidaturas

### 3.1. Candidatar-se

    POST /api/candidaturas     (STUDENT)
    { "desafioId": 1 }

`409` se já estiver inscrito.

### 3.2. Enviar o projeto

    PUT /api/candidaturas/{id}/envio     (STUDENT dono da inscrição)
    { "linkRepositorio": "github.com/usuario/projeto" }

Muda o status para `ENVIADO`. (A consulta à API do GitHub para preencher linguagem e último commit ainda vai entrar aqui.)

### 3.3. Detalhe de uma entrega (tela `/business/assessment/:id`)

    GET /api/candidaturas/{id}

    {
      "id": 1,
      "status": "ENVIADO",
      "dataCandidatura": "...",
      "dataEnvio": "...",
      "linkRepositorio": "https://github.com/usuario/projeto",
      "repoLinguagem": null,
      "repoUltimoCommit": null,
      "candidatoId": 2,
      "nomeCandidato": "Ana Martins",
      "githubCandidato": null,
      "desafioId": 1,
      "tituloDesafio": "API de pedidos para uma pequena loja",
      "prazoDias": 7,
      "avaliacao": null
    }

### 3.4. Listar candidaturas de um desafio

    GET /api/candidaturas/desafio/{desafioId}     (MENTOR dono do desafio)

### 3.5. Aprovar/Rejeitar

    PATCH /api/candidaturas/{id}/status     (MENTOR dono do desafio)
    { "status": "APROVADO" }

---

## ⭐ 4. Avaliação

    POST /api/candidaturas/{id}/avaliacao     (MENTOR dono do desafio)

    {
      "nota": 8.5,
      "feedback": "A estrutura da API está clara... (20 a 3000 caracteres)"
    }

- `201` `{ "id", "candidaturaId", "nota", "feedback", "dataAvaliacao", "nomeMentor" }` e a candidatura passa para `AVALIADO`
- `400` nota fora de 0–10, feedback curto, ou projeto ainda não enviado
- `403` mentor que não publicou o desafio
- `409` entrega já avaliada

---

## ⚠️ Formato dos erros

Validação de campos (`400`):

    { "desafioId": "O ID do desafio é obrigatório" }

Demais erros (`400`, `401`, `403`, `404`, `409`, `500`):

    { "erro": "Mensagem explicando o problema" }

---

## 📋 Resumo dos Endpoints

    POST   /api/auth/cadastro                    - Cadastrar usuário (público)
    POST   /api/auth/login                       - Login, devolve o token (público)
    GET    /api/usuarios/me                      - Dados do usuário logado
    GET    /api/desafios                         - Listar desafios (público)
    GET    /api/desafios/{id}                    - Detalhe do desafio (público)
    POST   /api/desafios                         - Criar desafio (MENTOR)
    POST   /api/candidaturas                     - Candidatar-se (STUDENT)
    GET    /api/candidaturas/{id}                - Detalhe da entrega (mentor dono ou aluno)
    PUT    /api/candidaturas/{id}/envio          - Enviar repositório (STUDENT)
    GET    /api/candidaturas/desafio/{desafioId} - Candidaturas de um desafio (MENTOR dono)
    PATCH  /api/candidaturas/{id}/status         - Aprovar ou rejeitar (MENTOR dono)
    POST   /api/candidaturas/{id}/avaliacao      - Avaliar com nota e feedback (MENTOR dono)
