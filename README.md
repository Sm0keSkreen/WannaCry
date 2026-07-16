# WannaCry

A Fabric cheat client module for Minecraft 1.21.11.

## Features

**Overtick** Queues knockback, explosions, windcharges, and pings from the server. Release the queue whenever you want to apply them all at once.

- dummy entities appear at player positions while the blink is active so you can target the player and use it as reach that works even on grim
- Live players are set to spectator mode during a blink so they cannot be interacted with
- Hitting a ghost redirects the attack to the real player
- Multiple release modes: One-by-One, Tick, Up-Down, Jetpack

## Installation

1. Install [Fabric Loader](https://fabricmc.net) for Minecraft 1.21.11
2. Drop `wannacry.jar` into your mods folder
3. Launch the game

## Usage

Enable Overtick in the module list. Knockback and explosions will be held until you press your release key. Set a bind under ReleaseKey in the module settings.

## Building

Requires JDK 21 or newer.

```
./gradlew build
```

Output is in `build/libs/`.
