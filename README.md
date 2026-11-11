# Trampolim

Plataforma que conecta aluno e mentor através de desafios práticos reais. O aluno resolve, o mentor avalia, e isso constrói um portfólio público validado — visível pra qualquer empresa, sem precisar de login.

> Projeto final da Trilha Dev. Full Stack 2026 — +praTi & Codifica

## Stack

- **Front-end:** React
- **Back-end:** Java + Spring Boot
- **Banco de dados:** PostgreSQL (Supabase); H2 em memória para rodar local e nos testes
- **Autenticação:** JWT via Spring Security
- **APIs externas:** GitHub API (valida o repositório enviado pelo aluno), e-mail por SMTP (avisos de inscrição, entrega e avaliação)
- **Infraestrutura:** Docker e Docker Compose, GitHub Actions (testes a cada Pull Request), Render (publicação)

## Estrutura do repositório

```
/backend             → API em Spring Boot
/frontend            → aplicação React
/docs                → plano técnico, modelo de dados, wireframes
docker-compose.yml   → sobe banco, back e front com um comando
render.yaml          → receita de publicação no Render
.github/workflows    → testes automáticos a cada Pull Request
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
  - Aluno: `student@trampolim.local` / `senha-teste-123`
- **Para usar o banco do time (Supabase)**, copie `backend/.env.example` para `backend/.env` e preencha (peça os valores ao time).
- Testes: `./mvnw test` no backend e `npm test` no front-end.
- Rotas da API e quem pode acessar cada uma: [backend/README.md](backend/README.md).
- Cobertura de testes: `npm run coverage` no front-end; no backend, `./mvnw test` gera `target/site/jacoco/index.html`.

## Como rodar com Docker

Sobe o banco PostgreSQL, o back-end e o front-end juntos, do jeito que vão para produção. Precisa do [Docker Desktop](https://www.docker.com/products/docker-desktop/) aberto.

```
cp .env.example .env
```

Abra o `.env` e preencha `POSTGRES_PASSWORD` e `JWT_SECRET`. Para gerar cada valor, use `openssl rand -hex 32` no Git Bash. Depois:

```
docker compose up --build
```

Abra http://localhost:5173. Aqui o banco começa vazio, sem os dados de exemplo: crie uma conta de mentor e uma de aluno pela tela de cadastro. `Ctrl+C` para tudo, e `docker compose down -v` apaga também os dados do banco.

## Publicação (deploy)

O arquivo [render.yaml](render.yaml) descreve os dois serviços no [Render](https://render.com): o back-end, criado a partir do `backend/Dockerfile`, e o front-end, como site estático. O banco é o PostgreSQL do Supabase.

1. No Supabase, clique em **Connect** e copie os dados do **Session pooler**. A URL do banco fica assim: `jdbc:postgresql://HOST:5432/postgres?sslmode=require`. O usuário tem o formato `postgres.xxxxx`.
2. No Render, entre com a conta do GitHub, escolha **New > Blueprint** e selecione este repositório.
3. Preencha o que o Render perguntar:

   | Variável | Valor |
   |---|---|
   | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | dados do Supabase |
   | `CORS_ALLOWED_ORIGINS` | endereço do front, como `https://trampolim.onrender.com` |
   | `VITE_API_URL` | endereço do back, como `https://trampolim-api.onrender.com` |

   A `JWT_SECRET` é sorteada e guardada pelo próprio Render.
4. Depois de criado, confira no painel o endereço real de cada serviço. Se o Render tiver acrescentado letras ao nome, corrija `CORS_ALLOWED_ORIGINS` e `VITE_API_URL` e publique de novo com **Manual Deploy**.
5. Teste: `https://ENDERECO-DO-BACK/actuator/health` deve responder `{"status":"UP"}`.

Variáveis opcionais, que podem ser acrescentadas depois em **Environment** no serviço do back-end: `GITHUB_TOKEN` e as `MAIL_*` dos avisos por e-mail. O plano gratuito do Render bloqueia as portas 25, 465 e 587, então o servidor de e-mail precisa aceitar outra, como a 2525.

No plano gratuito o back-end dorme depois de 15 minutos sem uso, e a primeira chamada seguinte leva cerca de um minuto. Antes de apresentar, abra o site com alguns minutos de antecedência.

## Arquitetura

```
Navegador (React + Vite)  --HTTP/JSON + token JWT-->  API (Spring Boot)  --JPA-->  PostgreSQL / H2
                                                          |
                                                          +--HTTPS-->  API do GitHub
```

- **Front-end:** cada tela fica em `frontend/src/pages`. Toda chamada à API passa por `services/api.js`, que anexa o token e padroniza os erros. O `ProtectedRoute` separa as rotas de aluno e de mentor.
- **Back-end:** em camadas. `controller` recebe a requisição, `service` aplica as regras, `repository` fala com o banco e `dto` define o que entra e sai da API.
- **Autenticação:** o login devolve um JWT assinado, válido por 24h. A cada requisição, o `JwtAuthenticationFilter` confere o token e o `SecurityConfig` decide quem acessa o quê pelo perfil. Senhas são gravadas só como hash BCrypt.
- **Banco:** 4 tabelas (`usuarios`, `desafios`, `candidaturas`, `avaliacoes`); veja o [modelo de dados](docs/modelo_dados_trampolim.svg).

## Funcionalidades

- **Home:** apresenta a proposta e leva para os desafios e as métricas.
- **Cadastro e login:** contas de aluno e de mentor, com volta para a tela de onde a pessoa veio.
- **Desafios:** lista com busca e filtro, e detalhe, abertos sem login. O mentor publica, edita, fecha, reabre e apaga.
- **Inscrição e envio:** o aluno se inscreve e envia o link do repositório, validado na API do GitHub (existe, é público, linguagem e último commit).
- **Avaliação:** o mentor dono do desafio dá nota de 0 a 10 e feedback.
- **Perfil do aluno:** página pública em `/student/profile/:id`, sem login e sem e-mail, que funciona como portfólio. O próprio aluno edita os dados e acompanha as inscrições.
- **Perfil do mentor:** desafios publicados, inscrições de cada um, aprovação ou recusa de inscrição e atalho para avaliar.
- **Painel de métricas:** totais, inscrições por mês e funil da inscrição à avaliação, em `/metricas`.
- **Avisos por e-mail:** o mentor é avisado de cada inscrição e de cada entrega; o aluno, da aprovação ou recusa e da avaliação. Sem servidor de e-mail configurado, os avisos só aparecem no log do back-end.

## Aspectos relevantes

- **Só dois perfis (aluno e mentor):** o valor para empresas vem do portfólio público, que dispensa conta e painel de empresa.
- **Validar no GitHub, sem bloquear por ele:** repositório inexistente é recusado, mas se o GitHub estiver fora do ar o envio é aceito, porque uma instabilidade externa não pode impedir a entrega.
- **Permissão em duas camadas:** o `SecurityConfig` confere o perfil e os services conferem se o desafio é mesmo daquele mentor, o que impede um mentor de avaliar a entrega de outro.
- **Roda sem configuração:** sem o `backend/.env`, o backend usa H2 com dados de exemplo, então qualquer pessoa do time testa sem precisar da senha do banco.
- **Testes sem dependência externa:** usam banco em memória e um servidor local no lugar do GitHub.

## Segurança

- **Senhas e segredos:** senhas gravadas só como hash BCrypt. Senha do banco e chave do JWT ficam em variáveis de ambiente, nunca no repositório.
- **Freio contra força bruta:** cinco senhas erradas bloqueiam o login daquele e-mail por 15 minutos, e o cadastro tem limite por IP. Quem passa do limite recebe `429`.
- **Perfil de produção:** com `SPRING_PROFILES_ACTIVE=prod`, o back-end desliga os dados de exemplo, o console do H2 e o log de SQL. Nesse perfil o back-end se recusa a subir sem banco PostgreSQL, sem `JWT_SECRET` ou sem `CORS_ALLOWED_ORIGINS`.
- **Content-Security-Policy no front:** o navegador só executa scripts do próprio site e só conversa com a API. Isso protege o token de login, que fica no `localStorage`.
- **Cabeçalhos de segurança** na API e no site, e erros que nunca mostram detalhes internos.
- **Testes automáticos a cada Pull Request:** o GitHub Actions roda os testes do back, o lint, os testes e o build do front, e monta as imagens Docker.

Limites conhecidos: o token vale 24 horas e não pode ser cancelado antes; não há recuperação de senha nem confirmação de e-mail; qualquer pessoa pode se cadastrar como mentor.

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
