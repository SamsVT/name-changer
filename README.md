# Name Changer

Name Changer is a roleplay nickname mod for Minecraft.
It changes how player names are displayed without changing the real account name, UUID, or authentication identity.

## Features

- `/nick <name>` or `/nick name <name>` sets a nickname with a 32 character limit
- `/nick reset` removes your nickname
- `/nick hide on` and `/nick hide off` let OPs hide floating player names server-wide
- Hold `U` while hide mode is enabled to show hidden names temporarily
- Nicknames are shown in overhead names, chat, the tab list, and supported voice overlays
- Real account identity is preserved for auth-dependent mods and systems

## Voice Chat Compatibility

- `Plasmo Voice` overlay names are replaced with nicknames
- Unicode characters in the Plasmo overlay are patched so special characters render correctly
- `Simple Voice Chat` identity is left untouched

## Project Layout

- [1.20.1](./1.20.1) - Architectury version
- [1.21.1](./1.21.1) - Fabric version

## Installation

### Fabric

1. Install [Fabric API](https://modrinth.com/mod/fabric-api)
2. Place the built mod jar in your `mods` folder

### Server

Install the mod on both the server and clients if you want chat, tab list, overhead names, and nickname sync to match.
