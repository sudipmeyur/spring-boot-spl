# Revert Unsold Player - Visual Guide

## Operation Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    REVERT UNSOLD PLAYER                         │
└─────────────────────────────────────────────────────────────────┘

DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024
                    ↓
        ┌───────────────────────┐
        │  PlayerController     │
        │  revertUnsoldPlayer() │
        └───────────┬───────────┘
                    ↓
        ┌───────────────────────────────────┐
        │  PlayerService                    │
        │  revertUnsoldPlayerByCode()       │
        │  ├─ Find Player by code           │
        │  ├─ Find Season by code           │
        │  └─ Call revertUnsoldPlayer()     │
        └───────────┬───────────────────────┘
                    ↓
        ┌───────────────────────────────────┐
        │  PlayerService                    │
        │  revertUnsoldPlayer()             │
        │  ├─ Find UnsoldPlayer by IDs      │
        │  ├─ Validate exists               │
        │  └─ Delete from database          │
        └───────────┬───────────────────────┘
                    ↓
        ┌───────────────────────┐
        │  UnsoldPlayerRepository│
        │  delete()             │
        └───────────┬───────────┘
                    ↓
        ┌───────────────────────┐
        │  Database             │
        │  DELETE unsold_player │
        └───────────┬───────────┘
                    ↓
        ┌───────────────────────┐
        │  Response             │
        │  204 No Content       │
        └───────────────────────┘
```

## State Transition Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    PLAYER STATES                             │
└──────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │  AVAILABLE      │
                    │  (Not assigned) │
                    └────────┬────────┘
                             │
                    POST /api/players/unsold
                             │
                             ↓
                    ┌─────────────────┐
                    │  UNSOLD         │
                    │  (Marked unsold)│
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
DELETE /api/players/unsold   │    POST /api/player-teams
        │                    │    (with isUnsold=true)
        ↓                    ↓
┌─────────────────┐  ┌─────────────────┐
│  AVAILABLE      │  │  ASSIGNED       │
│  (Reverted)     │  │  (In team)      │
└─────────────────┘  └─────────────────┘
```

## Data Model Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    UNSOLD PLAYER ENTITY                      │
└──────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────┐
    │      UnsoldPlayer               │
    ├─────────────────────────────────┤
    │ id (PK)                         │
    │ player_id (FK) ──────┐          │
    │ season_id (FK) ──┐   │          │
    │ createdAt        │   │          │
    └─────────────────────────────────┘
                       │   │
        ┌──────────────┘   └──────────────┐
        │                                 │
        ↓                                 ↓
    ┌─────────────┐              ┌─────────────┐
    │   Player    │              │   Season    │
    ├─────────────┤              ├─────────────┤
    │ id (PK)     │              │ id (PK)     │
    │ code        │              │ code        │
    │ name        │              │ year        │
    │ ...         │              │ ...         │
    └─────────────┘              └─────────────┘

Unique Constraint: (player_id, season_id)
```

## API Request/Response Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    SAVE UNSOLD PLAYER                        │
└──────────────────────────────────────────────────────────────┘

REQUEST:
┌─────────────────────────────────────────────────────────────┐
│ POST /api/players/unsold                                    │
│ Content-Type: application/json                              │
│                                                             │
│ {                                                           │
│   "playerCode": "P001",                                     │
│   "seasonCode": "S2024"                                     │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘

RESPONSE (200 OK):
┌─────────────────────────────────────────────────────────────┐
│ {                                                           │
│   "id": 1,                                                  │
│   "player": {                                               │
│     "id": 1,                                                │
│     "code": "P001",                                         │
│     "name": "Player One"                                    │
│   },                                                        │
│   "season": {                                               │
│     "id": 1,                                                │
│     "code": "S2024",                                        │
│     "year": 2024                                            │
│   },                                                        │
│   "createdAt": "2026-01-28T10:30:00"                        │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    REVERT UNSOLD PLAYER                      │
└──────────────────────────────────────────────────────────────┘

REQUEST:
┌─────────────────────────────────────────────────────────────┐
│ DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024 │
└─────────────────────────────────────────────────────────────┘

RESPONSE (204 No Content):
┌─────────────────────────────────────────────────────────────┐
│ (Empty body)                                                │
└─────────────────────────────────────────────────────────────┘

ERROR RESPONSE (404 Not Found):
┌─────────────────────────────────────────────────────────────┐
│ {                                                           │
│   "error": {                                                │
│     "status": 404,                                          │
│     "code": "RESOURCE_NOT_FOUND",                           │
│     "message": "UnsoldPlayer not found with identifier: ...",
│     "timestamp": "2026-01-28T10:35:00",                     │
│     "path": "/api/players/unsold"                           │
│   }                                                         │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
```

## Method Call Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│              REVERT UNSOLD PLAYER FLOW                       │
└─────────────────────────────────────────────────────────────┘

PlayerController
    │
    └─→ revertUnsoldPlayer(playerCode, seasonCode)
            │
            └─→ PlayerService.revertUnsoldPlayerByCode()
                    │
                    ├─→ playerRepository.findByCode(playerCode)
                    │       └─→ Returns: Player or null
                    │
                    ├─→ seasonRepository.findByCode(seasonCode)
                    │       └─→ Returns: Season or null
                    │
                    ├─→ Validate Player exists
                    │       └─→ Throw ResourceNotFoundException if null
                    │
                    ├─→ Validate Season exists
                    │       └─→ Throw ResourceNotFoundException if null
                    │
                    └─→ PlayerService.revertUnsoldPlayer(seasonId, playerId)
                            │
                            ├─→ unsoldPlayerRepository.findBySeasonIdAndPlayerId()
                            │       └─→ Returns: UnsoldPlayer or null
                            │
                            ├─→ Validate UnsoldPlayer exists
                            │       └─→ Throw ResourceNotFoundException if null
                            │
                            └─→ unsoldPlayerRepository.delete(unsoldPlayer)
                                    └─→ Database DELETE operation
```

## Error Handling Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    ERROR SCENARIOS                           │
└──────────────────────────────────────────────────────────────┘

DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024
                    ↓
        ┌───────────────────────┐
        │  Find Player by code  │
        └───────────┬───────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
    Found                   Not Found
        │                       │
        ↓                       ↓
    Continue        ┌─────────────────────────┐
                    │ ResourceNotFoundException│
                    │ "Player not found"      │
                    │ HTTP 404                │
                    └─────────────────────────┘
        │
        ↓
    ┌───────────────────────┐
    │  Find Season by code  │
    └───────────┬───────────┘
                │
    ┌───────────┴───────────┐
    │                       │
Found                   Not Found
    │                       │
    ↓                       ↓
Continue        ┌─────────────────────────┐
                │ ResourceNotFoundException│
                │ "Season not found"      │
                │ HTTP 404                │
                └─────────────────────────┘
    │
    ↓
┌───────────────────────────────────┐
│  Find UnsoldPlayer by IDs         │
└───────────┬───────────────────────┘
            │
┌───────────┴───────────┐
│                       │
Found               Not Found
│                       │
↓                       ↓
Delete      ┌─────────────────────────┐
│           │ ResourceNotFoundException│
↓           │ "UnsoldPlayer not found"│
Success     │ HTTP 404                │
│           └─────────────────────────┘
↓
┌─────────────────────┐
│ 204 No Content      │
└─────────────────────┘
```

## Comparison Matrix

```
┌──────────────────────────────────────────────────────────────┐
│         SAVE vs REVERT UNSOLD PLAYER                         │
└──────────────────────────────────────────────────────────────┘

SAVE UNSOLD PLAYER:
┌─────────────────────────────────────────────────────────────┐
│ POST /api/players/unsold                                    │
│                                                             │
│ Input:  PlayerTeamRequest                                   │
│         ├─ playerCode: "P001"                               │
│         └─ seasonCode: "S2024"                              │
│                                                             │
│ Process:                                                    │
│ 1. Validate required fields                                 │
│ 2. Find Player by code                                      │
│ 3. Find Season by code                                      │
│ 4. Check for duplicates                                     │
│ 5. Create UnsoldPlayer                                      │
│ 6. Save to database                                         │
│                                                             │
│ Output: UnsoldPlayer (201 Created)                          │
│ ├─ id: 1                                                    │
│ ├─ player: Player                                           │
│ ├─ season: Season                                           │
│ └─ createdAt: timestamp                                     │
└─────────────────────────────────────────────────────────────┘

REVERT UNSOLD PLAYER:
┌─────────────────────────────────────────────────────────────┐
│ DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024 │
│                                                             │
│ Input:  Query Parameters                                    │
│         ├─ playerCode: "P001"                               │
│         └─ seasonCode: "S2024"                              │
│                                                             │
│ Process:                                                    │
│ 1. Find Player by code                                      │
│ 2. Find Season by code                                      │
│ 3. Find UnsoldPlayer by IDs                                 │
│ 4. Validate exists                                          │
│ 5. Delete from database                                     │
│                                                             │
│ Output: 204 No Content                                      │
│ (Empty body)                                                │
└─────────────────────────────────────────────────────────────┘
```

## Workflow Timeline

```
┌──────────────────────────────────────────────────────────────┐
│                    AUCTION WORKFLOW                          │
└──────────────────────────────────────────────────────────────┘

Time ──────────────────────────────────────────────────────────→

T0: Auction Starts
    │
    ├─ Player P001 available
    │
T1: Player P001 not sold
    │
    ├─ POST /api/players/unsold
    │  └─ Mark P001 as unsold
    │
T2: Player P001 marked unsold
    │
    ├─ Realize it was a mistake
    │
T3: Undo unsold marking
    │
    ├─ DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024
    │  └─ Revert unsold marking
    │
T4: Player P001 available again
    │
    ├─ Player P001 can be re-auctioned
    │
T5: Player P001 sold to Team T001
    │
    └─ POST /api/player-teams
       └─ Assign P001 to T001
```

## Integration Points

```
┌──────────────────────────────────────────────────────────────┐
│              INTEGRATION WITH OTHER COMPONENTS               │
└──────────────────────────────────────────────────────────────┘

PlayerService
    │
    ├─→ PlayerRepository
    │   └─ findByCode(code)
    │
    ├─→ SeasonRepository
    │   └─ findByCode(code)
    │
    └─→ UnsoldPlayerRepository
        ├─ findBySeasonIdAndPlayerId(seasonId, playerId)
        └─ delete(unsoldPlayer)

PlayerController
    │
    └─→ PlayerService
        ├─ saveUnsoldPlayer(request)
        ├─ revertUnsoldPlayerByCode(playerCode, seasonCode)
        └─ getUnsoldPlayersShuffled(seasonId)

PlayerTeamService
    │
    └─→ UnsoldPlayerRepository
        └─ Automatically removes unsold record when player assigned
```

## Summary

The revert unsold player operation:
- **Removes** unsold marking from a player
- **Deletes** UnsoldPlayer record from database
- **Makes** player available for normal auction flow
- **Validates** all inputs and handles errors gracefully
- **Transactional** for data consistency
- **RESTful** with standard HTTP methods

Use it when you need to undo unsold markings!
