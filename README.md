## Name Changer is mod that adds a simple nickname system Works on both client and server.

### Features
Players can set their own nickname using /nick
Reset back to the real name
OP can toggle “hide all player names” server-wide with /nick hide on/off

Hold U to temporarily reveal real names while hide mode is enabled

### ⌨️Commands
```
/nick name <name> or /nick <name> — set nickname (max 32 chars)

/nick reset — reset nickname

/nick hide on — hide all names (OP only)

/nick hide off — disable hide all names (OP only)
```

## 📥Installation  

### Fabric  
1. Install [Fabric API](https://modrinth.com/mod/fabric-api)  
2. Place `name-changer-x.x.x.jar` into your `mods` folder  

### Forge  
1. Install [Forgified Fabric API](https://modrinth.com/mod/forgified-fabric-api)  
2. Install [Sinytra Connector](https://modrinth.com/mod/connector)  
3. Place `name-changer-x.x.x.jar` into your `mods` folder  


## ℹ️Notes
If the nickname is longer than 32 characters, it won’t change and will warn the player
Hide mode removes floating name labels completely until you hold U to reveal temporarily
