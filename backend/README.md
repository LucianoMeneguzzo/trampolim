# Documentação da API - Trampolim

Esta documentação descreve as rotas disponíveis na API do projeto Trampolim, incluindo autenticação, gerenciamento de desafios e candidaturas.

## 🔐 Autenticação Geral

Todas as rotas, exceto `/api/auth/login` e `/api/usuarios`, exigem autenticação via Token JWT.

Como usar:
Inclua o cabeçalho abaixo em todas as requisições protegidas:

    Authorization: Bearer SEU_TOKEN_AQUI

---

## 📌 1. Autenticação e Usuários

### 1.1. Registrar Novo Usuário

    Rota: POST /api/usuarios
    Autenticação: Não necessária

Corpo da Requisição:

    {
      "name": "Nome do Usuário",
      "email": "usuario@exemplo.com",
      "password": "senha123",
      "type": "MENTOR"
    }

Obs: O campo "type" pode ser "MENTOR" ou "STUDENT".

Resposta de Sucesso (201 Criado):
Retorna os dados do usuário cadastrado (sem o campo "password").

### 1.2. Realizar Login

    Rota: POST /api/auth/login
    Autenticação: Não necessária

Corpo da Requisição:

    {
      "email": "usuario@exemplo.com",
      "password": "senha123"
    }

Resposta de Sucesso (200 OK):

    {
      "userId": 1,
      "name": "Nome do Usuário",
      "type": "MENTOR",
      "token": "eyJhbGciOiJIUzUxMiJ9..."
    }

Use o valor do campo "token" no cabeçalho Authorization das próximas requisições.

---

## 🚀 2. Desafios

### 2.1. Criar Novo Desafio

    Rota: POST /api/desafios
    Autenticação: Obrigatória

O criador será vinculado automaticamente ao usuário logado através do token JWT.

Corpo da Requisição:

    {
      "title": "Título do Desafio",
      "description": "Descrição detalhada do desafio.",
      "level": "INICIANTE"
    }

Obs: O campo "level" pode ser "INICIANTE", "INTERMEDIARIO" ou "AVANCADO".

Resposta de Sucesso (201 Criado):

    {
      "id": 1,
      "titulo": "Título do Desafio",
      "descricao": "Descrição detalhada do desafio.",
      "nivel": "INICIANTE",
      "dataCriacao": "2026-10-14T10:00:00.000",
      "nomeCriador": "Nome do Usuário"
    }

### 2.2. Listar Todos os Desafios

    Rota: GET /api/desafios
    Autenticação: Obrigatória

Resposta de Sucesso (200 OK):
Retorna uma lista (array) com todos os desafios no mesmo formato da resposta de criação acima.

---

## 📝 3. Candidaturas

### 3.1. Candidatar-se a um Desafio

    Rota: POST /api/candidaturas
    Autenticação: Obrigatória

O candidato será automaticamente o usuário logado através do token JWT.

Corpo da Requisição:

    {
      "desafioId": 1
    }

Resposta de Sucesso (201 Criado):

    {
      "id": 1,
      "status": "PENDENTE",
      "dataCandidatura": "2026-10-14T10:05:00.000"
    }

### 3.2. Listar Candidaturas de um Desafio

    Rota: GET /api/candidaturas/desafio/{desafioId}
    Autenticação: Obrigatória

Substitua {desafioId} pelo ID numérico do desafio desejado.

Resposta de Sucesso (200 OK):

    [
      {
        "id": 1,
        "status": "PENDENTE",
        "dataCandidatura": "2026-10-14T10:05:00.000",
        "nomeCandidato": "Nome do Usuário",
        "emailCandidato": "usuario@exemplo.com",
        "tituloDesafio": "Título do Desafio"
      }
    ]

### 3.3. Atualizar Status da Candidatura (Aprovar/Rejeitar)

    Rota: PATCH /api/candidaturas/{id}/status
    Autenticação: Obrigatória

Substitua {id} pelo ID numérico da candidatura.

Corpo da Requisição:

    {
      "status": "APROVADO"
    }

Obs: O campo "status" deve ser estritamente "APROVADO" ou "REJEITADO".

Resposta de Sucesso (200 OK):
Retorna a candidatura atualizada com o novo status e todos os dados do candidato e desafio.

---

## ⚠️ Tratamento de Erros Padrão

A API utiliza um tratador global de exceções. Em caso de erro, a resposta será sempre um JSON com o código HTTP apropriado.

Erro de Validação (400 Bad Request):
Ocorre quando campos obrigatórios não são enviados ou estão vazios.

    {
      "desafioId": "O ID do desafio é obrigatório"
    }

Erro de Regra de Negócio (400 Bad Request):
Ocorre quando a operação viola uma regra do sistema (ex: candidatura duplicada, status inválido).

    {
      "erro": "Você já se candidatou para este desafio."
    }

Erro Interno do Servidor (500 Internal Server Error):
Ocorre quando há um problema inesperado no servidor.

    {
      "erro": "Ocorreu um erro interno no servidor. Tente novamente mais tarde."
    }

---

## 📋 Resumo dos Endpoints

    POST   /api/usuarios                         - Registrar novo usuário
    POST   /api/auth/login                       - Realizar login e obter token
    POST   /api/desafios                         - Criar novo desafio
    GET    /api/desafios                         - Listar todos os desafios
    POST   /api/candidaturas                     - Candidatar-se a um desafio
    GET    /api/candidaturas/desafio/{desafioId} - Listar candidaturas de um desafio
    PATCH  /api/candidaturas/{id}/status         - Aprovar ou rejeitar candidatura