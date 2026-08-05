# CoopShield SOC - Backend Dockerfile
# Projeto independente, educacional e de portfolio. Dados sinteticos.

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copia primeiro apenas os poms, para aproveitar o cache de dependencias
# do Docker entre builds quando so o codigo-fonte muda.
COPY pom.xml ./
COPY sharedkernel/pom.xml sharedkernel/pom.xml
COPY identity/pom.xml identity/pom.xml
COPY accesscontrol/pom.xml accesscontrol/pom.xml
COPY eventingestion/pom.xml eventingestion/pom.xml
COPY eventnormalization/pom.xml eventnormalization/pom.xml
COPY dataprotection/pom.xml dataprotection/pom.xml
COPY detection/pom.xml detection/pom.xml
COPY risk/pom.xml risk/pom.xml
COPY alert/pom.xml alert/pom.xml
COPY incident/pom.xml incident/pom.xml
COPY playbook/pom.xml playbook/pom.xml
COPY audit/pom.xml audit/pom.xml
COPY observability/pom.xml observability/pom.xml
COPY simulation/pom.xml simulation/pom.xml
COPY app/pom.xml app/pom.xml

RUN mvn -q -B dependency:go-offline || true

COPY . .
RUN mvn -q -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --no-create-home --shell /usr/sbin/nologin coopshield

WORKDIR /app
COPY --from=build /workspace/app/target/coopshield-soc.jar app.jar

USER coopshield
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
