# Backend - Trampolim API

API REST para o projeto Trampolim, responsável por gerenciar desafios de código, candidaturas de alunos e avaliações de mentores.

## 🚀 Como rodar localmente

O backend foi projetado para funcionar **sem configuração** em ambiente de desenvolvimento, usando um banco H2 em memória. Para usar um banco PostgreSQL real (como Supabase), basta criar um arquivo `.env`.

### Opção 1: Rodar com banco em memória (H2) — Sem configuração

Basta executar:

```bash
cd backend
./mvnw spring-boot:run
```

A aplicação sobe na porta `8080` e cria automaticamente:
- 1 mentor: `mentor@trampolim.local` / `senha-teste-123`
- 1 aluno: `student@trampolim.local` / `senha-teste-123`
- 3 desafios de exemplo
- 1 entrega já enviada pelo aluno, pronta para avaliar em `/business/assessment/1`
- 1 trabalho já avaliado, para o perfil público do aluno não começar vazio

Sem o `.env`, a chave do JWT é sorteada a cada vez que o backend sobe: os logins caem ao reiniciar.

O console do H2 fica disponível em: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:trampolimdb`
- Usuário: `sa`
- Senha: (deixe em branco)

### Opção 2: Rodar com Supabase (PostgreSQL)

1. Copie o arquivo `.env.example` para `.env`:

```bash
cp .env.example .env
```

2. Edite o `.env` com suas credenciais do Supabase:

```env
DB_URL=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
JWT_SECRET=uma_chave_muito_segura_aqui
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://localhost:5173
GITHUB_TOKEN=opcional
```

3. Rode a aplicação:

```bash
./mvnw spring-boot:run
```

> ⚠️ O arquivo `.env` está no `.gitignore` e **nunca deve ser commitado**.

## 🔐 Autenticação

Todas as rotas protegidas exigem um token JWT enviado no cabeçalho `Authorization`:

```
Authorization: Bearer SEU_TOKEN_AQUI
```

Para obter o token, use a rota `POST /api/auth/login`.

| Situação | Resposta |
|---|---|
| Sem token, token inválido ou expirado em rota protegida | `401 { "erro": "Faça login para continuar." }` |
| Logado, mas com o perfil errado (ex.: aluno criando desafio) | `403 { "erro": "Seu perfil não tem permissão para esta ação." }` |

O front em `http://localhost:5173` já pode chamar a API. Para liberar outros endereços, use a variável `CORS_ALLOWED_ORIGINS`.

## 📋 Rotas da API

### 👤 Usuários e Autenticação

#### Registrar novo usuário (público)
```http
POST /api/auth/cadastro
```

**Corpo da requisição:**
```json
{
  "name": "Nome do Usuário",
  "email": "usuario@exemplo.com",
  "password": "senha-com-8-ou-mais",
  "type": "MENTOR",
  "bio": "Sua bio",
  "professionalTitle": "Seu cargo",
  "github": "https://github.com/usuario",
  "linkedin": "https://linkedin.com/in/usuario"
}
```

O campo `type` aceita apenas: `MENTOR` ou `STUDENT`. A senha precisa ter de 8 a 72 caracteres e o e-mail é salvo em minúsculas.

- `201` com o usuário criado (sem a senha)
- `400` campo inválido, ex.: `{ "password": "A senha precisa ter entre 8 e 72 caracteres" }`
- `409` `{ "erro": "Este e-mail já está cadastrado." }`

---

#### Realizar login (público)
```http
POST /api/auth/login
```

**Corpo da requisição:**
```json
{
  "email": "usuario@exemplo.com",
  "password": "senha123"
}
```

**Resposta de sucesso (200):**
```json
{
  "userId": 1,
  "name": "Nome do Usuário",
  "type": "MENTOR",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

E-mail ou senha errados: `401 { "erro": "E-mail ou senha inválidos." }`

---

#### Meus dados (autenticado)
```http
GET /api/usuarios/me
```

---

#### Editar o próprio perfil (autenticado)
```http
PUT /api/usuarios/me
```

**Corpo da requisição:**
```json
{
  "name": "Ana Martins",
  "professionalTitle": "Dev em formação",
  "bio": "Construo APIs.",
  "github": "github.com/ana-martins",
  "linkedin": "linkedin.com/in/ana-martins"
}
```

Só `name` é obrigatório. Os links precisam ser do GitHub e do LinkedIn: como viram links no perfil público, outros endereços são recusados com `400`.

---

#### Perfil público (público)
```http
GET /api/usuarios/{id}
```

Dados do perfil, sem o e-mail, e `trabalhos`: as entregas avaliadas por mentores, que formam o portfólio.

---

### 📊 Métricas

#### Painel de métricas (público)
```http
GET /api/metricas
```

**Resposta de sucesso (200):**
```json
{
  "desafios": 3,
  "inscricoes": 10,
  "enviados": 7,
  "avaliados": 5,
  "taxaConclusao": 50,
  "inscricoesPorMes": [{ "mes": "2026-11", "total": 4 }]
}
```

`inscricoesPorMes` sempre traz os últimos 6 meses.

---

### 🎯 Desafios

#### Listar todos os desafios (público)
```http
GET /api/desafios
```

**Resposta de sucesso (200):**
```json
[
  {
    "id": 1,
    "titulo": "API REST com Spring Boot",
    "descricao": "Desenvolver uma API REST completa...",
    "tecnologias": "Java, Spring Boot, PostgreSQL",
    "nivel": "INTERMEDIARIO",
    "prazo": "2026-11-30",
    "vagas": 5,
    "empresa": "TechCorp",
    "dataCriacao": "2026-10-31T09:00:00",
    "nomeCriador": "Mentor Trampolim",
    "criadorId": 1,
    "status": "ABERTO"
  }
]
```

---

#### Buscar desafio por ID (público)
```http
GET /api/desafios/{id}
```

**Resposta de sucesso (200):** Mesma estrutura do objeto acima, mais `inscritos`.

---

#### Meus desafios (apenas MENTOR)
```http
GET /api/desafios/meus
```

Os desafios do mentor logado, com `inscritos` e `entregas` de cada um.

---

#### Criar novo desafio (apenas MENTOR)
```http
POST /api/desafios
```

**Corpo da requisição:**
```json
{
  "titulo": "Título do Desafio",
  "descricao": "Descrição detalhada do desafio.",
  "tecnologias": "Java, Spring Boot, PostgreSQL",
  "nivel": "INTERMEDIARIO",
  "prazo": "2026-12-31",
  "vagas": 5,
  "empresa": "Nome da Empresa"
}
```

O criador é vinculado automaticamente ao mentor autenticado.

---

#### Editar desafio (apenas MENTOR que publicou)
```http
PUT /api/desafios/{id}
```

Mesmo corpo da criação.

---

#### Abrir ou fechar as inscrições (apenas MENTOR que publicou)
```http
PATCH /api/desafios/{id}/status
```

**Corpo da requisição:** `{ "status": "FECHADO" }` (ou `ABERTO`). As inscrições e avaliações que já existem continuam valendo.

---

#### Apagar desafio (apenas MENTOR que publicou)
```http
DELETE /api/desafios/{id}
```

Responde `204`. Desafio com inscrições não pode ser apagado (`409`): feche as inscrições em vez disso.

---

### 📝 Candidaturas

#### Candidatar-se a um desafio (apenas STUDENT)
```http
POST /api/candidaturas
```

**Corpo da requisição:**
```json
{
  "desafioId": 1
}
```

⚠️ Inscrições repetidas no mesmo desafio retornam **409 Conflict**, assim como desafio sem vagas. Desafio fechado ou com o prazo vencido retorna **400**.

---

#### Minhas inscrições (apenas STUDENT)
```http
GET /api/candidaturas/minhas
```

As inscrições do aluno logado, cada uma com `avaliacao` quando já houver.

---

#### Buscar candidatura por ID (mentor dono do desafio ou o próprio aluno)
```http
GET /api/candidaturas/{id}
```

**Resposta de sucesso (200):**
```json
{
  "id": 1,
  "status": "PENDENTE",
  "dataCandidatura": "2026-10-31T09:05:00",
  "nomeCandidato": "Aluno Trampolim",
  "emailCandidato": "student@trampolim.local",
  "candidatoId": 2,
  "desafioId": 1,
  "tituloDesafio": "API REST com Spring Boot",
  "linkEnvio": null,
  "dataEnvio": null,
  "repoLinguagem": null,
  "repoUltimoCommit": null,
  "avaliacao": null
}
```

Depois de avaliada, `avaliacao` traz a nota e o feedback, no mesmo formato da resposta de `POST /api/candidaturas/{id}/avaliacao`. É esta rota que alimenta a tela `/business/assessment/:id`.

---

#### Listar candidaturas de um desafio (apenas MENTOR dono do desafio)
```http
GET /api/candidaturas/desafio/{desafioId}
```

Retorna um array com todas as candidaturas do desafio, no mesmo formato acima.

---

#### Enviar projeto (apenas STUDENT dono da candidatura)
```http
PUT /api/candidaturas/{id}/envio
```

**Corpo da requisição:**
```json
{
  "linkEnvio": "https://github.com/aluno/meu-projeto"
}
```

O aluno só pode enviar o projeto das suas próprias candidaturas. O status muda para `ENVIADO`. O link precisa ser de um repositório do GitHub, e candidaturas `REJEITADO` ou `AVALIADO` não aceitam novo envio.

O back consulta a API do GitHub: repositório inexistente ou privado responde `400`; quando existe, `repoLinguagem` e `repoUltimoCommit` são preenchidos. Se o GitHub estiver fora do ar ou no limite de consultas, o envio é aceito sem esses dados.

---

#### Aprovar ou rejeitar candidatura (apenas MENTOR dono do desafio)
```http
PATCH /api/candidaturas/{id}/status
```

**Corpo da requisição:**
```json
{
  "status": "APROVADO"
}
```

O campo `status` aceita apenas: `APROVADO` ou `REJEITADO`.

---

#### Avaliar candidatura (apenas MENTOR criador do desafio)
```http
POST /api/candidaturas/{id}/avaliacao
```

**Corpo da requisição:**
```json
{
  "nota": 8,
  "feedback": "Excelente trabalho! Código limpo e bem estruturado."
}
```

Apenas o mentor que **criou o desafio** pode avaliar a candidatura. A nota deve ser um número inteiro entre 0 e 10. A candidatura precisa ter projeto enviado (senão `400`) e só pode ser avaliada uma vez (senão `409`).

**Resposta de sucesso (201):**
```json
{
  "id": 1,
  "nota": 8,
  "feedback": "Excelente trabalho! Código limpo e bem estruturado.",
  "dataAvaliacao": "2026-10-31T10:00:00",
  "nomeMentor": "Mentor Trampolim",
  "emailMentor": "mentor@trampolim.local",
  "candidaturaId": 1,
  "nomeCandidato": "Aluno Trampolim",
  "tituloDesafio": "API REST com Spring Boot"
}
```

---

## ⚠️ Formato dos erros

Validação de campos (`400`):

```json
{ "desafioId": "O ID do desafio é obrigatório" }
```

Demais erros (`400`, `401`, `403`, `404`, `409`, `500`):

```json
{ "erro": "Mensagem explicando o problema" }
```

## 🧪 Executando testes

```bash
cd backend
./mvnw test
```

O projeto possui 40 testes automatizados cobrindo as principais regras de negócio. O relatório de cobertura fica em `target/site/jacoco/index.html`.

## 📂 Estrutura do projeto

```
backend/
├── src/main/java/com/trampolim/api/
│   ├── config/          # SecurityConfig e DataSeeder
│   ├── controller/      # Controllers REST
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # Entidades JPA
│   ├── exception/       # Tratamento global de exceções
│   ├── repository/      # Repositórios Spring Data
│   ├── security/        # Filtros JWT e provedores
│   ├── service/         # Lógica de negócio
│   └── Application.java # Classe principal
├── src/main/resources/
│   └── application.properties
├── .env.example         # Modelo de variáveis de ambiente
└── pom.xml
```

## 🛠️ Tecnologias

- Java 21
- Spring Boot 3.3.4
- Spring Security com JWT
- Spring Data JPA
- PostgreSQL (produção) / H2 (desenvolvimento)
- Bean Validation (Jakarta Validation)