FROM maven:3.5.4-jdk-8-alpine AS BUILD
COPY src     /usr/src/myapp/src
COPY pom.xml /usr/src/myapp
RUN mvn -f   /usr/src/myapp/pom.xml clean install

FROM openjdk:8-jre-alpine

LABEL maintainer="Héctor Ventura <hventura@syneteksolutions.com>" \
      nti.melissa.project="melissa-service" \
      nti.melissa.project.version="v1.00"

WORKDIR /app

COPY docker/*.* ./
COPY --from=BUILD /usr/src/myapp/target/melissa-service-fat.jar ./app.jar
RUN chmod +x ./entrypoint.sh

ENV DATABASE_USER=root
ENV DATABASE_PASSWORD=melissa
ENV DATABASE_URL=jdbc:mysql://mysql/melissa

EXPOSE 8095
VOLUME /app/etc

CMD ["./entrypoint.sh"]