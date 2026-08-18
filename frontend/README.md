# Trampolim — front-end

Esqueleto do projeto React, com rotas prontas entre as 9 telas e o design system
extraído do protótipo (`telas.html`) já compartilhado com o grupo.

## Rodando localmente

```
npm install
npm run dev
```

## O que já está pronto

- Roteamento entre as 9 telas (`src/App.jsx`)
- Navegação lateral funcionando (`src/components/Layout.jsx`)
- Design system compartilhado — cores, fontes, botões, topbar (`src/styles/theme.css`)
- 3 telas já convertidas por completo, como referência de padrão: **Home**, **Login** e **Desafios**
  (essa última mostra como transformar o filtro de busca do protótipo, que era JS puro, em estado do React)

## O que falta converter

Cada tela abaixo já existe como arquivo, só falta trazer o conteúdo do `telas.html`
(ou do protótipo já enviado ao grupo) pra dentro, seguindo o padrão das 3 já prontas.

| Arquivo | Tela | Responsável | Linhas de referência no telas.html |
|---|---|---|---|
| `src/pages/DesafioDetalhe.jsx` | Detalhe do desafio | Luiz | ~861–909 |
| `src/pages/Envio.jsx` | Envio do projeto | Andrius | ~910–951 |
| `src/pages/CriarDesafio.jsx` | Criar desafio | **sem dono ainda — falta o Luciano definir** | ~952–987 |
| `src/pages/Avaliar.jsx` | Avaliar envio | Rogério | ~988–1024 |
| `src/pages/Perfil.jsx` | Perfil público | Silvia | ~1025–1055 |
| `src/pages/Painel.jsx` | Painel de métricas | Luciano | ~1056–1092 |

Estilos específicos de cada tela (ex: `.challenge-card`, `.field`, `.score-control`)
não estão no `theme.css` de propósito — só o que é reaproveitado em todas as telas
está lá (cores, botões, navegação, topbar). Ao converter sua tela, copie o CSS
correspondente do `telas.html` original pra dentro do seu componente.

## Pendente, não esquecer

- Configurar Jest (já é card separado no Trello, de Pelegrino/Luiz)
- Trocar os dados de exemplo de `Desafios.jsx` pela chamada real em `GET /api/desafios`
  assim que a API estiver de pé
