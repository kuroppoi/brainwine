# Brainwine

Brainwine is a Deepworld private server written in Java, made with user-friendliness and portability in mind.
Due to the time it will take for this project to be complete (and my inconsistent working on it), brainwine has been prematurely open-sourced
and is free for all to use. Keep in mind, though, that this server is nowhere near finished. Expect to encounter bad code, bugs and missing features!
Brainwine is currently compatible with the latest Steam and iOS versions of Deepworld.

## Setup

### Setting up your client

Before you can connect to your (or someone else's) server, you must first let Deepworld know to where it should connect.
The exact process differs per platform. You may download an installation package for your desired platform [here.](https://github.com/kuroppoi/brainwine/releases)

### Setting up the server

Setting up your own server is as easy as downloading this repository and running `gradlew build` in a command prompt.
Alternatively, you can run the provided `build.bat` file. After the build task has finished, the output jar will be located in the `build` directory.
To start the server, simply run the jar file with a simple command line such as `java -jar brainwine.jar -Xms128m -Xmx512m`.
Be aware that Java 8 or newer is required to run Brainwine.

## Contributions

Disagree with how I did something? Found a potential error? See some room for improvement? Or just want to add a feature?
Glad to hear it! Feel free to make a pull request anytime. Just make sure you follow the code style!
And, apologies in advance for the lack of documentation. Haven't gotten around to do it yet. Sorry!

## Issues

Found a bug? Before posting an issue, make sure your build is up-to-date and your issue has not already been posted before.
Provide a detailed explanation of the issue, and how to reproduce it. I'll get to it ASAP!