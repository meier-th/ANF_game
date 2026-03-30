## ANF backend – current API & flows

This document describes the **current** REST and WebSocket APIs and the main user/game flows of the legacy monolith. It is a reference for modernization and later service extraction.

### REST API overview (by domain)

#### Auth & registration (`AuthController`)

- **GET** `/checkCookies`  
  - Returns JSON indicating whether the current session is authenticated and, if so, the username.
- **POST** `/registration`  
  - Registers a new user from a JSON `User` payload. Creates initial `Stats`, assigns `USER` role, and creates a default `Character`.
- **POST** `/confirm`  
  - Completes registration for OAuth-based users (VK/Google today) by setting a final login/password and linking VK ID or email.
- **GET** `/logout-success`  
  - Simple JSON response after logout; does not itself invalidate the session.

#### Profile, characters, stats & admin (`CharacterController`)

- **GET** `/`  
  - Returns the string `index` (used as a landing route).
- **GET** `/profile`  
  - Returns the full `User` object for the currently authenticated user.
- **POST** `/profile/character/appearance`  
  - Sets the current character’s `Appearance`. Expects request params: `gender`, `skinColour`, `hairColour`, `clothesColour`.
- **GET** `/admin/characters`  
  - Returns all `Character` entities (admin-only).
- **POST** `/profile/character`  
  - Upgrades a single character stat using one upgrade point. Request param `quality` ∈ {`damage`,`hp`,`resistance`,`chakra`}.
- **GET** `/profile/character`  
  - Returns the current user’s `Character`.
- **GET** `/users/{login}/character`  
  - Returns the `Character` for a specific user (404 if user not found).
- **POST** `/admin/users/{login}/grantAdmin`  
  - Grants the `ADMIN` role to the specified user.
- **GET** `/users`  
  - Returns all users.
- **GET** `/users/{login}`  
  - Returns a specific `User` by login.
- **DELETE** `/profile`  
  - Deletes the currently authenticated user.
- **GET** `/users/{login}/stats`  
  - Returns the `Stats` for a given user.
- **GET** `/friends`  
  - Returns the current user’s friends list.
- **GET** `/ready`  
  - Returns a list of usernames currently considered “online” (from `FightDataBean.onlineUsers`).
- **GET** `/profile/online`  
  - Marks the current user as online and broadcasts a WebSocket `/online` notification.
- **GET** `/profile/offline`  
  - Marks the current user as offline and broadcasts a WebSocket `/online` notification.
- **GET** `/profile/pvphistory`  
  - Returns a list of PvP fight records (`pvpRecord`) for the current user.

#### Messaging & friends (`CommunicationController`)

- **POST** `/profile/messages`  
  - Sends a private message. Request params: `message`, `receiver`. Persists a `PrivateMessage` and sends a WebSocket `/msg` notification to the receiver.
- **GET** `/profile/messages/unread`  
  - Returns all unread messages for the current user.
- **GET** `/profile/dialogs`  
  - Returns a list of usernames with whom the current user has dialogs.
- **GET** `/profile/messages/dialog`  
  - Returns all messages between the current user and another user. Request param: `secondName`.
- **DELETE** `/profile/messages/{id}`  
  - Deletes a message by ID, but only if the current user is the sender.
- **POST** `/profile/messages/{id}/read`  
  - Marks a message as read; only the receiver can do this.
- **DELETE** `/profile/friends/requests`  
  - Cancels or declines a friend request. Request params: `username` and `type` (`in` for incoming, anything else for outgoing).
- **GET** `/friends/requests/outgoing`  
  - Returns users whom the current user has requested as friends.
- **GET** `/friends/requests/incoming`  
  - Returns users who requested the current user as a friend.
- **POST** `/profile/friends/requests`  
  - Creates a new friend request to another user (param `username`), and sends WebSocket `/social` updates.
- **POST** `/profile/friends`  
  - Accepts a friend request from `login`, creates the friendship, and sends WebSocket `/social` updates.
- **DELETE** `/profile/friends`  
  - Removes an existing friend relationship with `username`, with corresponding WebSocket `/social` updates.
- **POST** `/admin/chat`  
  - Sends a system-wide admin warning message via WebSocket `/chat`.

#### Fight entities & abilities (`FightEntitiesController`)

- **GET** `/fight/boss`  
  - Returns details for a boss by numeric `id`.
- **GET** `/fight/spell`  
  - Returns all available `Spell` definitions.
- **GET** `/fight/spell/my/all`  
  - Returns all `SpellKnowledge` (spell levels) for the current character.
- **GET** `/fight/spell/my`  
  - Returns the `SpellKnowledge` for a specific spell (param `spellname`) or HTTP 423 (LOCKED) if not yet learned.
- **POST** `/fight/spell/my`  
  - Learns or levels up a spell’s `SpellKnowledge` for the current character, consuming one upgrade point.
- **GET** `/fight/animals/my`  
  - Returns the currently available `NinjaAnimal` for the player’s race and level (one of two per race, depending on level).
- **GET** `/fight/animals`  
  - Returns all defined `NinjaAnimal` companions.
- **POST** `/fight/animals/my`  
  - Sets the Ninja Animal race for the current character (one-time choice). Param: `racename` (`NinjaAnimalRace` enum name).

#### Fight lobbies & actions (`FightController`)

Base path: **`/fight`**.

- **POST** `/fight/lobbies`  
  - Creates a new lobby for current user. Param: `mode` (`PVP`, `SOLO_PVE`, `TEAM_PVE`).
- **GET** `/fight/lobbies/{lobbyUuid}`  
  - Returns lobby details (`lobbyUuid`, `fightMode`, leader, players).
- **POST** `/fight/lobbies/{lobbyUuid}/join`  
  - Current user joins an existing lobby.
- **POST** `/fight/lobbies/{lobbyUuid}/leave`  
  - Current user leaves lobby; lobby is closed automatically when empty.
- **DELETE** `/fight/lobbies/{lobbyUuid}`  
  - Closes lobby explicitly.
- **POST** `/fight/lobbies/{lobbyUuid}/start`  
  - Starts a fight from lobby and returns `fightUuid`; requires `bossId` for PvE.
- **POST** `/fight/info`  
  - Returns ongoing fight state by `fightUuid`, including remaining time.
- **GET** `/fight/attack`  
  - Performs an attack in a fight. Params: `enemy`, `fightUuid`, `spellName`.
- **POST** `/fight/summonPvp`  
  - Summons player’s Ninja Animal into a PvP fight. Param: `fightUuid`.
- **POST** `/fight/summonPve`  
  - Summons player’s Ninja Animal into a PvE fight. Param: `fightUuid`.

### WebSocket/STOMP API

#### Endpoints and broker configuration

- **STOMP endpoint**:  
  - `/socket` (SockJS enabled, `allowedOrigins("*")` in current config).  
  - The handshake associates the STOMP `Principal` with the Spring Security username via `StompPrincipal`.
- **Application destination prefix**:  
  - `/app` – client messages sent here are routed to `@MessageMapping` handlers.
- **Simple broker destinations** (subscriptions):  
  - `/chat`, `/msg`, `/online`, `/social`, `/fightState`, `/admin/admins`, `/invite`, `/approval`, `/start`, `/switch`, `/summon`.

#### Message mappings (`WebSocketsController`)

- **Client → Server**
  - **SEND** to `/app/send/message`  
    - Broadcasts a chat message. If the payload ends with `"test"`, a test `State` fight update is also sent to a specific user via `/fightState`.

- **Server → Client (broadcast / user-specific)**  
  - `/chat` – public chat channel (messages appended with server timestamp).  
  - `/online` – presence events such as `username:online`, `username:offline`, or `new:username`.  
  - `/admin/admins` – notifies admins when someone gets `ADMIN` role.  
  - `/user/{username}/msg` – private messages (chat contents between two users).  
  - `/user/{username}/social` – friend-request and friendship status updates.  
  - `/user/{username}/fightState` – detailed fight state updates (`State` object) after an action.  
  - `/user/{username}/switch` – indicates whose turn it is next in a fight.  
  - `/user/{username}/start` – fight start notifications with `fightUuid`.  
  - `/user/{username}/summon` – Ninja Animal summon notifications with serialized animal data.

### Core user & game flows (current behavior)

#### Registration and login

1. **Direct registration**: Client calls `POST /registration` with a `User` payload → backend creates `Stats`, `Character`, assigns `USER` role, and notifies via WebSocket `SYSTEM` message.
2. **OAuth-based registration** (current VK/Google flow): external login assigns temporary roles (`NEWVK`/`NEWGoogle`), then `POST /confirm` finalizes the account with a chosen login/password and sets email or VK ID.
3. **Session check**: Frontend calls `GET /checkCookies` to determine if the current cookie/session is authenticated and obtain the username.

#### Profile, stats and upgrades

1. Client fetches profile via `GET /profile` and character via `GET /profile/character`.  
2. Stats (level, XP, rating, upgrade points) are retrieved through `GET /users/{login}/stats`.  
3. Character upgrades:  
   - `POST /profile/character` with `quality` updates damage / HP / resistance / chakra, consuming one upgrade point.  
   - `POST /fight/spell/my` learns or levels up a spell, also consuming one upgrade point.

#### Messaging and friends

1. **Private chat**:
   - REST: `POST /profile/messages` sends a message and persists it.  
   - WebSocket: server sends to `/user/{receiver}/msg` so the recipient’s UI updates in real time.
2. **Dialogs and unread count**:
   - `GET /profile/dialogs` lists conversation partners.  
   - `GET /profile/messages/unread` and `POST /profile/messages/{id}/read` manage unread state.
3. **Friend lifecycle**:
   - Outgoing/incoming requests via `/profile/friends/requests` (POST/DELETE) and `/friends/requests/*`.  
   - Accepting a request via `POST /profile/friends`.  
   - Removing a friend via `DELETE /profile/friends`.  
   - WebSocket `/social` notifications keep both sides’ UIs synchronized.

#### PvP flow (lobby → fight → result)

1. A player calls `POST /fight/lobbies?mode=PVP` to create a lobby.  
2. The second player joins via `POST /fight/lobbies/{lobbyUuid}/join`.  
3. Fight starts via `POST /fight/lobbies/{lobbyUuid}/start`, which:
   - Prepares both characters (`prepareForFight`), computes rating-change parameters, stores runtime fight state in Redis, and notifies players via `/start`.  
   - Schedules the first attacker and subsequent turns.
4. Players take turns calling `GET /fight/attack` with `enemy`, `fightUuid`, `spellName` (or `"Physical attack"`).  
5. After each action:
   - The backend updates HP/chakra, determines death, sends `State` updates on `/fightState`, and switches attacker (with `/switch` notifications).  
   - When a player (or animal) dies, rating and stats are updated, a `FightPVP` record is persisted, and both users are removed from `usersInFight`.

#### PvE flow (lobby → boss fight → result)

1. A group forms in a lobby via `POST /fight/lobbies?mode=TEAM_PVE` and `/fight/lobbies/{lobbyUuid}/join`, then starts with `POST /fight/lobbies/{lobbyUuid}/start?bossId=...`.  
2. The backend:
   - Creates a `FightVsAI` with multiple fighters and a `Boss`, prepares all entities, and notifies each player via `/start`.  
   - Manages a turn order that includes players, boss, and summoned animals.
3. Players attack the boss via `GET /fight/attack` with `fightUuid` and `spellName`; the boss and animals may act automatically based on timers.  
4. When the boss dies or all players die, the service:
   - Persists a `FightVsAI` record and associated `UserAIFight` entries.  
   - Updates `Stats` (fights, wins/losses/deaths, XP), and clears in-memory fight state.

#### Presence and online users

1. Frontend periodically calls `GET /profile/online` to refresh presence and send an `/online` WebSocket event like `username:online`.  
2. `FightDataBean` maintains a static list of `(username, lastSeen)` pairs and periodically prunes entries older than 5 minutes, sending `username:offline` notifications over `/online`.  
3. `GET /ready` exposes the current set of online users to the frontend as a plain list of usernames.

