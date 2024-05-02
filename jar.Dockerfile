#? Builder
FROM --platform=$BUILDPLATFORM debian:bookworm-slim AS builder
RUN apt update -y && apt upgrade -y && apt autoremove -y
RUN apt install -y git gradle
WORKDIR /src
COPY . /src
RUN git submodule init && git submodule update
RUN chmod +x gradlew && ./gradlew dist
ENTRYPOINT [ "/src/build/dist" ]

FROM scratch AS export
COPY --from=builder /src/build/dist /