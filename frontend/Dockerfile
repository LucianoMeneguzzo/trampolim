# Imagem do front-end em duas etapas: o Node gera os arquivos e o nginx só os entrega.

# ---------- Etapa 1: gera o site (pasta dist) ----------
FROM node:22-alpine AS build
WORKDIR /app

# Primeiro só a lista de dependências: enquanto ela não mudar, o Docker reaproveita esta camada
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# O endereço da API entra AGORA, no build: o Vite grava o valor dentro do JavaScript gerado.
# Para outro endereço: docker build --build-arg VITE_API_URL=https://minha-api.onrender.com .
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ---------- Etapa 2: nginx entregando o site ----------
# Versão do nginx que roda sem root e escuta na porta 8080
FROM nginxinc/nginx-unprivileged:stable-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080
