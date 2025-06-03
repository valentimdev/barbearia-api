# Stage 1: Build
FROM openjdk:21-jdk AS build
WORKDIR /app

# Copiar pom.xml e código fonte
COPY pom.xml .
COPY src src

# Copiar Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Permissão para executar Maven wrapper
RUN chmod +x ./mvnw

# Build do projeto sem testes
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:21-jdk
VOLUME /tmp

# Copiar JAR gerado do build para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Porta exposta
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app.jar"]
