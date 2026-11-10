# Trampolim — front-end

Aplicação React (Vite) do Trampolim, ligada à API em Spring Boot.

## Rodando localmente

Com o backend no ar (veja o [README principal](../README.md)):

```
npm install
npm run dev
```

A API é lida de `VITE_API_URL` (padrão: `http://localhost:8080`). Para outro endereço, copie `.env.example` para `.env`.

O valor entra no momento do `npm run build`: o Vite grava o endereço dentro do JavaScript gerado. Ao publicar, defina `VITE_API_URL` antes do build.

## Comandos

| Comando | O que faz |
|---|---|
| `npm run dev` | Sobe o front em http://localhost:5173 |
| `npm test` | Roda os testes em modo observação |
| `npm run coverage` | Roda os testes e mede a cobertura (mínimo de 70%) |
| `npm run lint` | Confere o padrão do código |
| `npm run build` | Gera a versão de produção em `dist/` |

## Rotas

| Rota | Tela | Acesso |
|---|---|---|
| `/` | Home | Público |
| `/login`, `/register` | Login e cadastro | Público |
| `/metricas` | Painel de métricas | Público |
| `/student/search` | Lista de desafios | Público |
| `/student/challenge/:id` | Detalhe do desafio e inscrição | Público (inscrição exige aluno) |
| `/student/profile/:id` | Portfólio do aluno | Público |
| `/student/profile` | Meu perfil, com edição e inscrições | Aluno |
| `/student/challenge/:id/send-challenge` | Envio do projeto | Aluno |
| `/business/profile` | Meu perfil, desafios e inscrições | Mentor |
| `/business/post-challenge` | Publicar desafio | Mentor |
| `/business/challenge/:id/edit` | Editar desafio | Mentor |
| `/business/assessment/:id` | Avaliar entrega | Mentor |

## Publicação e segurança

- `npm run build` gera o site em `dist/`. No mesmo passo, o `vite.config.js` escreve no `index.html` uma Content-Security-Policy: o navegador só executa scripts do próprio site e só conversa com o endereço de `VITE_API_URL`.

Se uma tela nova precisar carregar algo de outro site (uma fonte, uma imagem, outra API), inclua o endereço na lista do `vite.config.js`. Sem isso o navegador bloqueia, e o erro aparece no console.

## Organização

- `src/pages`: uma pasta por tela, com o `.jsx`, o `.css` e o teste.
- `src/components`: partes reaproveitadas, como `ProtectedRoute` e as barras do topo.
- `src/services`: toda chamada à API. `api.js` anexa o token e padroniza os erros.
- `src/utils`: formatação de datas, tecnologias e links.
- `src/styles/theme.css`: cores, botões e estruturas comuns.

O CSS do Vite é global: uma classe criada numa tela vale em todas. Por isso as telas novas usam um prefixo próprio (`detalhe-`, `painel-`, `area-`).
