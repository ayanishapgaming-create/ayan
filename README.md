# Server Manager +
**by Azor Studio** — Minecraft 1.21.1 Fabric Mod

Enhances the vanilla Multiplayer server list and World selection screen with:

- 🔍 **Search bar** — filter servers by name or IP, filter worlds by name
- 📌 **Pin servers & worlds** — pinned entries float to the top
- ★ **Pinned-only toggle** — show only your pinned servers/worlds
- 🌍 **Country flags** — each server shows a flag emoji for its host country
- 🗨️ **Flag tooltip** — hover over the flag to see the full country name
- Right-click any server or world entry to open the **Pin/Unpin context menu**

---

## Installation

1. Install [Fabric Loader ≥ 0.16.0](https://fabricmc.net/use/installer/) for Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `server-manager-plus-1.0.0.jar` into your `mods/` folder
4. Launch the game

---

## Building from Source

```bash
./gradlew build
```

Output jar will be at `build/libs/server-manager-plus-1.0.0.jar`

**Requirements:** Java 21, Gradle 8.x

---

## How It Works

### Multiplayer Screen
```
┌──────────────────────────────────────────────────────────┐
│  Multiplayer                                             │
│  [🔍 Search by name or IP...      ]  [☆ All Servers]   │
│  ────────────────────────────────────────────────────    │
│  📌 mc.hypixel.net           🇺🇸  ████ 12ms             │
│     Hypixel Network          ─────────────              │
│  play.mineplex.com           🇨🇦  ████ 80ms             │
│  ...                                                     │
└──────────────────────────────────────────────────────────┘
         Right-click any entry → [📌 Pin] [✕ Cancel]
```

### World Selection Screen
```
┌──────────────────────────────────────────────────────────┐
│  Select World                                            │
│  [🔍 Search worlds by name...     ]  [☆ All Worlds]    │
│  ────────────────────────────────────────────────────    │
│  📌 My Survival World       Last played: Today          │
│     Creative Build           Last played: Yesterday     │
└──────────────────────────────────────────────────────────┘
         Right-click any entry → [📌 Pin] [✕ Cancel]
```

### Flag System
Country codes are resolved by querying [ip-api.com](http://ip-api.com) (free, no key required).
Results are cached in `config/servermanagerplus/data.json` so each IP is only looked up once.

Hovering over the flag emoji shows a tooltip: `🇩🇪 Germany`

### Pinning
Pin data is stored in `config/servermanagerplus/data.json`. Pinned servers/worlds:
- Always sorted to the **top** of the list
- Marked with a **📌** icon on their entry
- Can be filtered via the **★ Pinned Only** toggle

---

## File Structure

```
server-manager-plus/
├── build.gradle
├── gradle.properties               ← minecraft 1.21.1, fabric-api
├── settings.gradle
├── README.md
└── src/main/
    ├── java/com/azorstudio/servermanagerplus/
    │   ├── ServerManagerPlusClient.java       ← Client entrypoint
    │   ├── data/
    │   │   └── ServerDataManager.java         ← Persistence (pins, country codes)
    │   ├── gui/
    │   │   ├── EnhancedMultiplayerScreen.java ← Injects search UI into multiplayer
    │   │   ├── EnhancedWorldScreen.java       ← Injects search UI into world select
    │   │   ├── SmpMultiplayerScreen.java      ← Full enhanced multiplayer screen
    │   │   ├── SmpSelectWorldScreen.java      ← Full enhanced world screen
    │   │   └── PinContextMenu.java            ← Right-click pin/unpin popup
    │   ├── mixin/
    │   │   ├── MultiplayerScreenMixin.java    ← Hooks into multiplayer screen init
    │   │   ├── SelectWorldScreenMixin.java    ← Hooks into world screen init
    │   │   └── ServerEntryMixin.java          ← Draws flag + pin icon per entry
    │   └── util/
    │       └── CountryLookupUtil.java         ← Async IP→country lookup + flag emoji
    └── resources/
        ├── fabric.mod.json
        ├── servermanagerplus.mixins.json
        └── assets/servermanagerplus/
            └── lang/en_us.json
```

---

## Data File

`config/servermanagerplus/data.json`:
```json
{
  "pinnedServers": ["mc.hypixel.net", "play.cubecraft.net"],
  "pinnedWorlds":  ["My Survival World"],
  "countryCodes":  {
    "mc.hypixel.net":      "US",
    "play.cubecraft.net":  "NL"
  }
}
```

---

## Compatibility

| Mod | Status |
|-----|--------|
| Sodium | ✅ Compatible |
| Iris | ✅ Compatible |
| Essential | ⚠️ May conflict on server list mixins |

---

## License
MIT © Azor Studio
