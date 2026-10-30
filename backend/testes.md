# Comandos de Teste da API - Trampolim

Este guia contém todos os comandos para testar os endpoints da API.

> **Nota:** Os comandos `curl` abaixo estão formatados com quebras de linha (`\`) para melhor legibilidade. Funcionam nativamente em Bash, Zsh e PowerShell.

---

## PASSO 1: Gerar Token (Login)

Execute este comando e copie o valor que aparecer no campo `"token"` da resposta. Substitua `SEU_TOKEN_AQUI` nos próximos comandos por esse valor.

    curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "email": "luciano@trampolim.com",
      "password": "senha123"
    }'

---

## PASSO 2: Criar um Novo Desafio

Substitua `SEU_TOKEN_AQUI` pelo token gerado no Passo 1.

    curl -i -X POST http://localhost:8080/api/desafios \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer SEU_TOKEN_AQUI" \
    -d '{
      "title": "Criar API REST com Spring Boot",
      "description": "Desenvolver um backend completo usando Java, Spring Boot, JPA e PostgreSQL.",
      "level": "INTERMEDIARIO"
    }'

---

## PASSO 3: Listar Todos os Desafios

    curl -i -X GET http://localhost:8080/api/desafios \
    -H "Authorization: Bearer SEU_TOKEN_AQUI"

---

## PASSO 4: Candidatar-se a um Desafio

> Use o token de uma conta **STUDENT**. Criar desafio (PASSO 2) e aprovar/rejeitar (PASSOS 6 e 7) pedem token de **MENTOR**.

Use o ID do desafio criado no Passo 2. Se for o primeiro, provavelmente é o ID 1 ou 2.

    curl -i -X POST http://localhost:8080/api/candidaturas \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer SEU_TOKEN_AQUI" \
    -d '{
      "desafioId": 1
    }'

---

## PASSO 5: Listar Candidaturas de um Desafio Específico

Substitua o `1` no final da URL pelo ID do desafio desejado.

    curl -i -X GET http://localhost:8080/api/candidaturas/desafio/1 \
    -H "Authorization: Bearer SEU_TOKEN_AQUI"

---

## PASSO 6: Aprovar uma Candidatura

Substitua o `1` na URL pelo ID da candidatura que você quer aprovar.

    curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer SEU_TOKEN_AQUI" \
    -d '{
      "status": "APROVADO"
    }'

---

## PASSO 7: Rejeitar uma Candidatura (Teste Alternativo)

    curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer SEU_TOKEN_AQUI" \
    -d '{
      "status": "REJEITADO"
    }'

---

## 🧪 TESTES DE ERRO (Tratamento Global de Exceções)

### Teste A: Candidatura duplicada (deve retornar erro 409)

Rode o comando do PASSO 4 duas vezes seguidas (com o mesmo `desafioId`). A segunda vez deve retornar um JSON limpo:

    {
      "erro": "Você já se candidatou para este desafio."
    }

### Teste B: Status inválido (deve retornar erro 400)

    curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer SEU_TOKEN_AQUI" \
    -d '{
      "status": "BANANA"
    }'

Deve retornar:

    {
      "erro": "Status inválido. Use apenas 'APROVADO' ou 'REJEITADO'."
    }

### Teste C: Campo obrigatório faltando no Desafio (deve retornar erro 400)

    curl -i -X POST http://localhost:8080/api/desafios \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer SEU_TOKEN_AQUI" \
    -d '{
      "title": "",
      "description": "Teste",
      "level": "INICIANTE"
    }'

Deve retornar algo como:

    {
      "title": "O título é obrigatório"
    }

---

## 📋 Resumo Rápido dos Endpoints

    POST   /api/auth/cadastro                    - Registrar novo usuário (sem auth)
    POST   /api/auth/login                       - Login e obter token (sem auth)
    POST   /api/desafios                         - Criar desafio (token de MENTOR)
    GET    /api/desafios                         - Listar desafios (sem auth)
    POST   /api/candidaturas                     - Candidatar-se (token de STUDENT)
    GET    /api/candidaturas/desafio/{id}        - Listar candidaturas de um desafio (token do MENTOR dono)
    PATCH  /api/candidaturas/{id}/status         - Aprovar ou rejeitar candidatura (token do MENTOR dono)
    PUT    /api/candidaturas/{id}/envio          - Enviar repositório (token do STUDENT dono)
    POST   /api/candidaturas/{id}/avaliacao      - Avaliar entrega (token do MENTOR dono)

Detalhes de cada rota e de quem pode acessar: ver backend/README.md.
Testes automáticos de tudo isso: ./mvnw test

    
