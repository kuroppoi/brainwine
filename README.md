# Brainwine

Brainwine is a private server written in Java for a game called Deepworld, made with user-friendliness and portability in mind.
Due to the time it will take for this project to be complete (and my inconsistent working on it,) brainwine has been prematurely open-sourced
and is free for all to use. Keep in mind, though, that this server is nowhere near finished. Expect to encounter bad code, bugs and missing features!
Brainwine is currently only compatible with the Steam version of Deepworld.

## Setup

### Setting up your client

Before you can connect to your (or someone elses) server, you must first let Deepworld know to where it should connect.
To do this, open the Registry Editor and navigate to `HKEY_CURRENT_USER\SOFTWARE\Bytebin LLC\Deepworld`.
Look for a String Value called `gateway` or create it if it doesn't exist. Change the value to `local` if you wish to connect to an instance running on your PC, otherwise
enter the host address and port of the server you wish to join. For example: `12.345.67.89:5001`
If you did everything correctly, your client should now be ready.

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