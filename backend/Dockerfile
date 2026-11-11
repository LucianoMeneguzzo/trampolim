# Imagem do back-end em duas etapas: a primeira compila, a segunda só roda.
# Assim a imagem final fica pequena, sem Maven e sem o código-fonte.

# ---------- Etapa 1: compila o projeto ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primeiro só o Maven e o pom.xml: enquanto as dependências não mudarem,
# o Docker reaproveita esta camada e não baixa tudo de novo.
COPY mvnw pom.xml ./
COPY .mvn .mvn
# No Windows o mvnw pode vir com fim de linha CRLF, que o Linux não entende: o sed corrige.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
# Os testes rodam no CI (GitHub Actions), não na criação da imagem
RUN ./mvnw -B -q package -DskipTests

# ---------- Etapa 2: roda o jar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuário sem privilégios: se alguém explorar uma falha, não vira root dentro do contêiner
RUN useradd --system --no-create-home trampolim
USER trampolim

COPY --from=build /app/target/*.jar app.jar

# A imagem é para publicar, então sobe no perfil de produção: sem banco em memória, sem dados
# de exemplo, e só inicia com DB_URL, JWT_SECRET e CORS_ALLOWED_ORIGINS definidos.
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Usa até metade da memória do contêiner para o Java (o plano gratuito do Render tem 512 MB)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50", "-jar", "app.jar"]
