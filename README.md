# StarSquad Servers

Backend servers for a mobile game project made in Java. Data is stored in a MongoDB database. Redis is used for quick cache.

### Modules
[Login](login) - Server where a player connects to at first. Then authenticates using a token and gets sent to a lobby or a game server.

[Lobby](lobby) - Servers that handles all non game related logic.

[Game](game) - Servers where the games happen.

[Master](master) - Server that all login/lobby/game servers communicates with.
