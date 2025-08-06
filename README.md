# ptkEssentials

A custom Minecraft server plugin designed to enhance gameplay with economy tools, clan features, and quality-of-life utilities. Built for Spigot API version 1.15.

## Features

### 💰 Economy System
- `/balance` or `/bal` — Check your current balance
- `/baltop` — View the top 10 richest players
- `/pay <player> <amount>` — Send money to other players
- `/donate <player> <amount>` — Donate money (requires permission: `ptk.donate`)

### 🛒 Quality of Life
- `/shop` — Opens the shop GUI
- `/challenge` or `/ch` — View and complete daily challenges
- `/insurance` — Purchase death insurance to retain items on death

### 🎁 Crates, Roulette, and Supply Drops
- `/crate spin` — Spin a mystery crate for random rewards
- `/roulette <red|green|black> <amount>` — Gamble your balance in roulette
- `/supplydrop` — Manually spawn a supply drop (requires `ptk.supplydrop`)

### 🛡️ Clans & PvP
- `/clan <create|join|leave|info|sethome|home>` — Create and manage clans
- Clan chat, PvP rules, and home teleportation included

### 👹 Boss Events
- `/boss <spawn|enable|disable>` — Manage server-wide boss events (requires `boss.admin`)

## Permissions

| Permission Node     | Description                         | Default |
|---------------------|-------------------------------------|---------|
| `boss.admin`        | Access to boss management commands  | OP      |
| `ptk.supplydrop`    | Access to spawn supply drops        | OP      |
| `ptk.donate`        | Use the `/donate` command           | OP      |
| `ptk.set`           | Set player balances                 | OP      |
| `ptk.shopset`       | Add new shop items                  | OP      |
| `ptk.shopremove`    | Remove shop items                   | OP      |

## Installation

1. Place the compiled `ptkEssentials-0.0.5.jar` file into your server's `/plugins` folder.
2. Restart or reload your server.
3. Configure any optional settings or permissions via your permissions plugin.

## Compatibility

- Minecraft version: **1.21.4**
- Server type: **Spigot**

## License

This plugin is proprietary and not open-source. Contact the author for usage rights or contributions.

---

**Author**: Patrick Gadula  
