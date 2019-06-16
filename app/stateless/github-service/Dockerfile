FROM gradle:jdk11 as build

ENV BUILD_DIR=/src
RUN mkdir -p ${BUILD_DIR}
WORKDIR ${BUILD_DIR}

COPY . ${BUILD_DIR}

RUN gradle clean build -x test

FROM adoptopenjdk/openjdk11:alpine-slim as prod

ENV APP_DIR=/usr/local/app
RUN mkdir -p ${APP_DIR}

COPY --from=build /src/build/libs/github-service.jar ${APP_DIR}/

EXPOSE 8080

ENTRYPOINT [ "java", "-Xmx512M", "-Xms512M", \
             "-jar", "/usr/local/app/github-service.jar"]
