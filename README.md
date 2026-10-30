# Trampolim

Plataforma que conecta aluno e mentor através de desafios práticos reais. O aluno resolve, o mentor avalia, e isso constrói um portfólio público validado — visível pra qualquer empresa, sem precisar de login.

> Projeto final da Trilha Dev. Full Stack 2026 — +praTi & Codifica

## Stack

- **Front-end:** React
- **Back-end:** Java + Spring Boot
- **Banco de dados:** PostgreSQL (Supabase); H2 em memória para rodar local e nos testes
- **Autenticação:** JWT via Spring Security
- **APIs externas:** GitHub API (valida o repositório enviado pelo aluno), serviço de e-mail (notificações)

## Estrutura do repositório

```
/backend    → API em Spring Boot
/frontend   → aplicação React
/docs       → plano técnico, modelo de dados, wireframes
```

## Como rodar localmente

Precisa de **Java 21+**, **Node 20.19+** e **Git**. Use dois terminais (no Windows, o Git Bash):

```
# Terminal 1 — backend (http://localhost:8080)
cd backend
./mvnw spring-boot:run

# Terminal 2 — front-end (http://localhost:5173)
cd frontend
npm install
npm run dev
```

- **Sem o arquivo `backend/.env`** o backend sobe com banco em memória (H2) e já cria dados de exemplo. Os dados somem quando ele para.
  - Mentor: `mentor@trampolim.local` / `senha-teste-123`
  - Aluna: `aluna@trampolim.local` / `senha-teste-123`
- **Para usar o banco do time (Supabase)**, copie `backend/.env.example` para `backend/.env` e preencha (peça os valores ao time).
- Testes: `./mvnw test` no backend e `npm test` no front-end.
- Rotas da API e quem pode acessar cada uma: [backend/README.md](backend/README.md).

## Funcionalidades

- Home — landing page explicando a proposta
- Autenticação — cadastro e login (aluno/mentor)
- Desafios — mentor cria, edita e fecha desafios
- Inscrição e envio — aluno se inscreve e envia o link do repositório (validado via API do GitHub)
- Avaliação — mentor avalia com nota e feedback (dispara e-mail pro aluno)
- Perfil do aluno — página pública, funciona como portfólio
- Painel de métricas — números agregados de participação e conclusão

## Documentação

- [Plano técnico completo](docs/plano_tecnico_desafios.md) — funcionalidades, banco de dados, rotas da API, divisão de tarefas
- [Modelo de dados](docs/modelo_dados_trampolim.svg)

## Time

| Pessoa | Frente |
|---|---|
| Pelegrino | Banco de dados + API de Desafios |
| Ismael | API de Inscrições, Avaliação e integração GitHub |
| João Lucas | Autenticação + infraestrutura |
| Luiz | Telas de Desafios |
| Silvia | Telas de Autenticação e Perfil |
| Andrius | Tela de Envio |
| Rogério | Tela de Avaliação |
| Luciano | Home/landing + Painel de métricas + coordenação |

## Critérios de avaliação

Este projeto segue os "Critérios de Continuidade e Avaliação — Trilha Dev. Full Stack 2026" do +praTi/Codifica: CRUD completo, autenticação e autorização, controle de versão com histórico de commits, testes com cobertura mínima de 70% (JUnit no back, Jest no front), e documentação técnica completa.
