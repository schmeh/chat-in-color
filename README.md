# Chat in Color

[![modrinth-badge](https://img.shields.io/modrinth/dt/chat-in-color?label=Modrinth&logo=Modrinth&style=flat-square)](https://modrinth.com/mod/chat-in-color/versions)

Chat In Color is a lightweight client-side Fabric mod that assigns each player a chat color, making it easy to tell each player at a glance.

![Chat](https://cdn.modrinth.com/data/IQQ6ho4I/images/66c9738d75f95172c9d33241638188694f66a278.png)

# Features

- Automatically assigns every player a random chat color

- Customize colors manually per player

- Custom colors persist between sessions

- Purely client-side

# Commands

- `/chatincolor setColor <player> <hex>`: Set a specific color for a player (example: /setColor Steve FF0000)

- `/chatincolor unsetColor`: Remove a set color assignment

- `/chatincolor getSetColors`: List all currently set player colors

- `/chatincolor randomizeColors`: Randomize colors for all players without set colors

- `/chatincolor help`: Displays all available commands

# Building

Requires **Java 21+**.

To build the mod, run the following command:
```bash
gradlew build
```
The compiled JAR will be in `build/libs/`
