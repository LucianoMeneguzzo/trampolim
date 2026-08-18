# Como contribuir — fluxo de Git do time

Guia rápido pra evitar os problemas mais comuns quando 8 pessoas mexem no mesmo repositório.

## Regra de ouro

Nunca commitar direto na `main`. Toda mudança entra por Pull Request, mesmo as pequenas.

## Passo a passo por tarefa

1. Atualiza a tua cópia local:
   ```
   git checkout main
   git pull origin main
   ```
2. Cria uma branch nova, a partir do nome do card do Trello:
   ```
   git checkout -b feature/nome-da-tarefa
   ```
   Exemplos: `feature/api-desafios`, `feature/tela-envio`, `fix/login-nao-salva`
3. Trabalha e commita em pedaços pequenos, com mensagem clara:
   ```
   git add .
   git commit -m "feat: cria endpoint de listagem de desafios"
   ```
4. Sobe a branch:
   ```
   git push origin feature/nome-da-tarefa
   ```
5. Abre um Pull Request no GitHub, da tua branch pra `main`.
6. Pelo menos uma pessoa da mesma frente revisa antes de aprovar (back revisa back, front revisa front).
7. Depois de aprovado, mergeia e apaga a branch.

## Prefixos de commit

- `feat:` — funcionalidade nova
- `fix:` — correção de bug
- `docs:` — documentação
- `test:` — testes
- `chore:` — configuração, dependências, ajustes que não mudam comportamento

Isso mantém o histórico organizado — é literalmente um dos critérios de avaliação do projeto final.

## Em caso de conflito

1. `git pull origin main` com frequência evita a maioria dos conflitos.
2. Quando acontecer, o Git marca assim no arquivo:
   ```
   <<<<<<< HEAD
   sua versão
   =======
   versão que veio do main
   >>>>>>> main
   ```
3. Decide o que fica (às vezes é um pouco de cada), apaga as marcações (`<<<<<<<`, `=======`, `>>>>>>>`), salva, `git add .`, `git commit`.
4. Na dúvida, chama quem mexeu no mesmo arquivo antes de decidir sozinho.

## Quem revisa o quê

| Frente | Revisores sugeridos |
|---|---|
| Backend (Pelegrino, Ismael, João Lucas) | Um dos outros dois do backend |
| Frontend (Luiz, Silvia, Andrius, Rogério, Luciano) | Um dos outros do frontend |

Como o projeto é monorepo (`/backend` e `/frontend` na mesma raiz), PR de front e de back raramente muda os mesmos arquivos — conflito entre as duas frentes deve ser raro.
