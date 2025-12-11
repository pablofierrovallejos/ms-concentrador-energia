# Dockerfile simple - usa JAR pre-compilado
# Compilar primero: .\mvnw.cmd clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR pre-compilado
COPY target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar app.jar

# Variable de entorno para el perfil
ENV SPRING_PROFILES_ACTIVE=local

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} /app/app.jar"]
