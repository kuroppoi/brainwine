# Brainwine
[![build](https://github.com/kuroppoi/brainwine/actions/workflows/build.yml/badge.svg)](https://github.com/kuroppoi/brainwine/actions)

Brainwine is a Deepworld private server written in Java, made with user-friendliness and portability in mind.
Due to the time it will take for this project to be complete (and my inconsistent working on it), brainwine has been prematurely open-sourced
and is free for all to use.\
Keep in mind, though, that this server is not finished yet. Expect to encounter bad code, bugs and missing features!\
Brainwine is currently compatible with the following versions of Deepworld:
- Steam: `v3.13.1`
- iOS: `v2.11.0.1`
- MacOS: `v2.11.1`

## Features
A list of all planned, in-progress and finished features can be found [here.](https://github.com/kuroppoi/brainwine/projects/1)

## Setup

### Setting up the client

Before you can connect to a server, a few modifications need to be made to the Deepworld game client.\
The exact process of this differs per platform.\
You may download an installation package for your desired platform [here.](https://github.com/kuroppoi/brainwine/releases/tag/patching-kits-1.0)

### Setting up the server

#### Prerequisites

- Java 8 or newer

You can download the latest release [here.](https://github.com/kuroppoi/brainwine/releases/latest)\
Alternatively, if you wish to build from source, clone this repository with the `--recurse-submodules` flag\
and run `gradlew dist` in the root directory of the repository.\
After the build has finished, the output jar will be located in `build/libs`.\
You may then start the server through the gui, or start it directly by running the jar with the `disablegui` flag.

#### Configurations

On first-time startup, configuration files will be generated which you may modify however you like:
- `api.json` Configuration file for news & API connectivity information.
- `loottables.json` Configuration file for which loot may be obtained from containers.
- `spawning.json` Configuration file for entity spawns per biome.
- `generators` Folder containing configuration files for zone generators.

## Contributions

Disagree with how I did something? Found a potential error? See some room for improvement? Or just want to add a feature?
Glad to hear it! Feel free to make a pull request anytime. Just make sure you follow the code style!
And, apologies in advance for the lack of documentation. Haven't gotten around to do it yet. Sorry!

## Issues

Found a bug? Before posting an issue, make sure your build is up-to-date and your issue has not already been posted before.
Provide a detailed explanation of the issue, and how to reproduce it. I'll get to it ASAP!
