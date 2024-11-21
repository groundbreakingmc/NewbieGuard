# NewbieGuard
A plugin to block new players from using commands or writing in the chat.

## Features
- Two time-based modes: counting from first login or total playtime
- Minimessages with hex color support
- Updates checker and notifier
- Supports two types of databases: SQLite and MariaDB
- Time formatting for messages (displays time as "dd hh mm ss", removing zero values)
- Blacklist and whitelist support for blocked commands
- Colon command blocker for specific command restrictions

## Commands
- /newbieguard reload - Reload the plugin (Permission: newbieguard.reload)
- /newbieguard help - See allowed commands (Permission: newbieguard.help)
- /newbieguard update - Update the plugin if possible (Console only)
- /newbieguard checkupdate - Check for updates (Console only)

## Permissions
- newbieguard.bypass.messages - Bypass the chat blocker for new players
- newbieguard.bypass.commands.<\group> - Bypass the command blocker in specified group for new players
- newbieguard.bypass.coloncommands - Bypass the colon command blocker