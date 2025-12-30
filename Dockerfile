# =========================
# Build stage
# =========================
FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# =========================
# Run stage
# =========================
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update && apt install -y \
    openjdk-21-jdk \
    texlive-xetex \
    texlive-latex-extra \
    texlive-fonts-recommended \
    texlive-fonts-extra \
    ghostscript \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]