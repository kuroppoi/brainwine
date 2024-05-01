<h1 align="center">Brainwine</h1>
<p align="center">
  <a href="https://github.com/kuroppoi/brainwine/actions"><img src="https://github.com/kuroppoi/brainwine/actions/workflows/build.yml/badge.svg" alt="build"/></a>
  <a href="https://github.com/kuroppoi/brainwine/releases/latest"><img src="https://img.shields.io/github/v/release/kuroppoi/brainwine?labelColor=30373D&label=Release&logoColor=959DA5&logo=github" alt="release"/></a>
</p>

Brainwine is a Deepworld private server written in Java, designed to be portable and easy to use.\
It's still a work in progress, so keep in mind that it's not yet feature-complete. (A to-do list can be found [here](https://github.com/kuroppoi/brainwine/projects/1).)\
Brainwine currently supports the following versions of Deepworld:

- Windows: `v3.13.1`
- iOS: `v2.11.0.1`
- MacOS: `v2.11.1`

## Quick Local Setup

- Install [Java 8](https://adoptium.net/temurin/releases/?package=jdk&version=8).
- Download the [latest Brainwine release](https://github.com/kuroppoi/brainwine/releases/latest).
- Run Brainwine, go to the server tab and start the server.
- Go to the game tab and start the game.
  - If this isn't available for you, download a [patching kit](https://github.com/kuroppoi/brainwine/releases/tag/patching-kits-1.0) for your platform and follow the instructions there.
- Register a new account and play the game.

## Building

#### Prerequisites

- Java 8 Development Kit

```sh
git clone --recurse-submodules https://github.com/kuroppoi/brainwine.git
cd brainwine
./gradlew dist
```

The output will be located in the `/build/dist` directory.

## Usage

Execute `brainwine.jar` to start the program. Navigate to the server tab and press the button to start the server.\
It is also possible to start the server immediately with no user interface:

```sh
# This behavior is the default on platforms that do not support Java's Desktop API.
java -jar brainwine.jar disablegui
```

To connect to a local or remote server, download a [patching kit](https://github.com/kuroppoi/brainwine/releases/tag/patching-kits-1.0) for your desired platform.\
Alternatively, Windows users may use the program's user interface to configure the host settings and start the game.
