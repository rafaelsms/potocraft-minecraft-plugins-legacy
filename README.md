# (Legacy) PotoCraft Plugin

A legacy plugin for my Minecraft server called PotoCraft.
To minimize maintaince due to time spent in a side-project being unsustainable, it was been replaced by community plugins for what was sufficiently good and [a few custom plugins](https://github.com/rafaelsms/potocraft-minecraft-plugin) to get the exact behavior I wanted.

Some systems provided by these plugins:
* Discord bot for moderation (censoring curse words, blocking spam and logging user behavior to make it easier to ban users that break rules, such as sharing NSFW)
* Allowing players to have in-game pets (used external library to avoid implementing low level network game packet manipulation)
* Per-plugin and per-server player data storage (preferences, properties used by systems)
* Automatic block protection
* In-game moderation and logging (censoring curse words, blocking spam, logging player behavior to identify bugs and issues)
* Mixing online and offline players through a proxy server ([BungeeCord](https://www.spigotmc.org/wiki/bungeecord/)) requiring login for offline players only
(major UX advantage, but passwords were stored in plain text because they were always logged by the proxy console anyway due to the nature of Minecraft's command handling.
Offline players didn't have an official Minecraft account, so their accounts would always be unsafe)
* Cross-server chat messages through the proxy
* Chat message relay in Discord
* Many many gameplay changes to the original Minecraft game logic (make players' lives easier or to rebalance the game)
