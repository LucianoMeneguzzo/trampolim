# Trampolim

Plataforma que conecta aluno e mentor através de desafios práticos reais. O aluno resolve, o mentor avalia, e isso constrói um portfólio público validado — visível pra qualquer empresa, sem precisar de login.

> Projeto final da Trilha Dev. Full Stack 2026 — +praTi & Codifica

## Stack

- **Front-end:** React
- **Back-end:** Java + Spring Boot
- **Banco de dados:** MySQL
- **Autenticação:** JWT via Spring Security
- **APIs externas:** GitHub API (valida o repositório enviado pelo aluno), serviço de e-mail (notificações)

## Estrutura do repositório

```
/backend    → API em Spring Boot
/frontend   → aplicação React
/docs       → plano técnico, modelo de dados, wireframes
```

## Como rodar localmente

> ⏳ Em construção — cada parte ganha instrução própria conforme o ambiente for configurado (backend, frontend, banco). Essa seção precisa estar completa antes da entrega final (é critério de avaliação).

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
