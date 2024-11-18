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
- /newbieguard - Reload the plugin (Permission: newbieguard.reload)

## Permissions
- newbieguard.bypass.chat - Bypass the chat blocker for new players
- newbieguard.bypass.commands - Bypass the command blocker for new players
- newbieguard.bypass.coloncommands - Bypass the colon command blocker
- newbieguard.updates - Receive update notifications when joining the server