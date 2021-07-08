# Brainwine

Brainwine is a Deepworld private server written in Java, made with user-friendliness and portability in mind.
Due to the time it will take for this project to be complete (and my inconsistent working on it), brainwine has been prematurely open-sourced
and is free for all to use. Keep in mind, though, that this server is nowhere near finished. Expect to encounter bad code, bugs and missing features!\
Brainwine is currently compatible with the latest Steam and iOS versions of Deepworld:
- Steam: `v3.13.1`
- iOS: `v2.11.0.1`

## Setup

### Setting up the client

Before you can connect to a server, a few modifications need to be made to the Deepworld game client.\
The exact process of this differs per platform.\
You may download an installation package for your desired platform [here.](https://github.com/kuroppoi/brainwine/releases)

### Setting up the server

#### Prerequisites

- Java 8 or newer

To set up the server, clone or download this repository and run `gradlew build`.
After the build process has finished, a distribution archive should have generated in `build/distributions`.
To start the server, simply extract this archive wherever you want and run the startup script for your OS.

#### Configurations

On first-time startup, configuration files will be generated which you may modify however you like:
- `api.json` Configuration file for news & API connectivity information.
- `generators.json` Configuration file for world generation rules per biome.
- `loottables.json` Configuration file for which loot may be obtained from containers.

## Contributions

Disagree with how I did something? Found a potential error? See some room for improvement? Or just want to add a feature?
Glad to hear it! Feel free to make a pull request anytime. Just make sure you follow the code style!
And, apologies in advance for the lack of documentation. Haven't gotten around to do it yet. Sorry!

## Issues

Found a bug? Before posting an issue, make sure your build is up-to-date and your issue has not already been posted before.
Provide a detailed explanation of the issue, and how to reproduce it. I'll get to it ASAP!