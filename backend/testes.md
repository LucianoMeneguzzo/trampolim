# Comandos de Teste da API - Trampolim

Execute estes comandos em sequência no terminal para testar todas as funcionalidades.

## ⚠️ Importante
Antes de começar, garanta que o servidor está rodando (botão verde no IntelliJ).
Se algum comando retornar "403 Forbidden", gere um novo token no Passo 1 e substitua nos próximos.

---

## PASSO 1: Gerar Token (Login)

curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{
"email": "luciano@trampolim.com",
"password": "senha123"
}'

Copie o valor do campo "token" da resposta e substitua em TODOS os comandos abaixo onde aparecer SEU_TOKEN_AQUI.

---

## PASSO 2: Criar um Novo Desafio

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

curl -i -X POST http://localhost:8080/api/candidaturas \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SEU_TOKEN_AQUI" \
-d '{
"desafioId": 1
}'

---

## PASSO 5: Listar Candidaturas de um Desafio Específico

curl -i -X GET http://localhost:8080/api/candidaturas/desafio/1 \
-H "Authorization: Bearer SEU_TOKEN_AQUI"

---

## PASSO 6: Aprovar uma Candidatura

curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SEU_TOKEN_AQUI" \
-d '{
"status": "APROVADO"
}'

---

## PASSO 7: Rejeitar uma Candidatura (teste alternativo)

curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SEU_TOKEN_AQUI" \
-d '{
"status": "REJEITADO"
}'

---

## 🧪 TESTES DE ERRO (Bônus - Tratamento Global de Exceções)

### Teste 1: Candidatura duplicada (deve retornar erro 400)
Rode o comando do PASSO 4 DUAS VEZES seguidas. A segunda deve retornar:
{ "erro": "Você já se candidatou para este desafio." }

### Teste 2: Status inválido (deve retornar erro 400)
curl -i -X PATCH http://localhost:8080/api/candidaturas/1/status \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SEU_TOKEN_AQUI" \
-d '{
"status": "BANANA"
}'

Deve retornar:
{ "erro": "Status inválido. Use apenas 'APROVADO' ou 'REJEITADO'." }

### Teste 3: Campo obrigatório faltando (deve retornar erro 400)
curl -i -X POST http://localhost:8080/api/desafios \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SEU_TOKEN_AQUI" \
-d '{
"title": "",
"description": "Teste",
"level": "INICIANTE"
}'

Deve retornar:
{ "title": "O título é obrigatório" }

---

## 📋 Resumo Rápido dos Endpoints

POST   /api/usuarios                         - Registrar novo usuário (sem auth)
POST   /api/auth/login                       - Login e obter token (sem auth)
POST   /api/desafios                         - Criar desafio (com auth)
GET    /api/desafios                         - Listar desafios (com auth)
POST   /api/candidaturas                     - Candidatar-se (com auth)
GET    /api/candidaturas/desafio/{id}        - Listar candidaturas (com auth)
PATCH  /api/candidaturas/{id}/status         - Aprovar/Rejeitar (com auth)