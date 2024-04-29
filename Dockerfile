#? Builder
FROM --platform=$BUILDPLATFORM amazoncorretto:22-alpine-full as builder
RUN apk update && apk upgrade
RUN apk add git
WORKDIR /src
COPY . /src

RUN git submodule init && git submodule update
RUN chmod +x gradlew && ./gradlew dist --scan

#? Runner
FROM --platform=$BUILDPLATFORM amazoncorretto:22-alpine-jdk as runner
RUN apk update && apk upgrade
COPY --from=builder /src/build/libs /app
CMD java -jar brainwine.jar

LABEL org.opencontainers.image.title="Brainwine"
LABEL org.opencontainers.image.description=" A portable private server for Deepworld."
LABEL org.opencontainers.image.source="https://github.com/kuroppoi/brainwine"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.documentation="https://github.com/kuroppoi/brainwine"
LABEL org.opencontainers.image.vendor=""