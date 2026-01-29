# Revert Operations Summary

## Overview
This document provides a quick reference for reverting operations in the SPL application.

## Two Revert Operations

### 1. Revert PlayerTeam Assignment
**File**: `REVERT_PLAYER_TEAM_GUIDE.md`

#### What It Does
- Removes a player from a team
- Restores unsold player record if applicable
- Recalculates team statistics

#### When to Use
- Remove player from team completely
- Undo player assignment
- Clean up incorrect assignments

#### Endpoint
```bash
DELETE /api/player-teams/{playerTeamCode}
```

#### Example
```bash
# Remove player P001 from team T001 in season S2024
DELETE /api/player-teams/P001T001S2024

# Response: 204 No Content
```

#### Complexity
- **High**: Affects team statistics
- Recalculates totals for affected teams
- Restores unsold records if needed
- Handles cascading updates

---

### 2. Revert Unsold Player Marking
**File**: `REVERT_UNSOLD_PLAYER_GUIDE.md`

#### What It Does
- Removes unsold marking from player
- Deletes UnsoldPlayer record
- Makes player available for normal auction

#### When to Use
- Undo unsold marking by mistake
- Player gets sold after being marked unsold
- Restart auction process
- Data correction

#### Endpoint
```bash
DELETE /api/players/unsold?playerCode={code}&seasonCode={code}
```

#### Example
```bash
# Remove unsold marking from player P001 in season S2024
DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024

# Response: 204 No Content
```

#### Complexity
- **Low**: Simple deletion
- No cascading effects
- No statistics recalculation
- Direct database operation

---

## Comparison Matrix

| Aspect | PlayerTeam Revert | Unsold Player Revert |
|--------|-------------------|----------------------|
| **Endpoint** | DELETE /api/player-teams/{code} | DELETE /api/players/unsold |
| **Parameters** | playerTeamCode (path) | playerCode, seasonCode (query) |
| **Complexity** | High | Low |
| **Side Effects** | Yes (team stats) | No |
| **Cascading Updates** | Yes | No |
| **Unsold Restoration** | Yes (if applicable) | N/A |
| **Error Handling** | ResourceNotFoundException | ResourceNotFoundException |
| **Transactional** | Yes | Yes |
| **Use Case** | Remove from team | Undo unsold marking |

---

## Decision Tree

```
Do you want to revert?
│
├─ Player assignment to team?
│  └─ Use: DELETE /api/player-teams/{playerTeamCode}
│     Effect: Remove from team, restore unsold if needed, recalc stats
│
└─ Unsold player marking?
   └─ Use: DELETE /api/players/unsold?playerCode=X&seasonCode=Y
      Effect: Remove unsold marking, make available for auction
```

---

## Common Scenarios

### Scenario 1: Player Assigned to Wrong Team
```bash
# Revert from wrong team
DELETE /api/player-teams/P001T001S2024

# Assign to correct team
POST /api/player-teams
{
  "playerCode": "P001",
  "teamSeasonCode": "T002S2024",
  "soldAmount": 100
}
```

### Scenario 2: Player Marked Unsold by Mistake
```bash
# Revert unsold marking
DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024

# Now player is available for normal auction
GET /api/players/available?seasonId=1&playerLevelId=1
```

### Scenario 3: Unsold Player Gets Sold Later
```bash
# Player was marked unsold
POST /api/players/unsold
{
  "playerCode": "P001",
  "seasonCode": "S2024"
}

# Later, player gets sold
POST /api/player-teams
{
  "playerCode": "P001",
  "teamSeasonCode": "T001S2024",
  "soldAmount": 100,
  "isUnsold": true  # Mark as unsold initially
}

# PlayerTeamService automatically removes unsold record
```

### Scenario 4: Undo Player Transfer
```bash
# Player was transferred from Team A to Team B
# Revert the transfer
DELETE /api/player-teams/P001T002S2024

# Now player is not in any team
# Reassign to Team A if needed
POST /api/player-teams
{
  "playerCode": "P001",
  "teamSeasonCode": "T001S2024",
  "soldAmount": 100
}
```

---

## API Reference

### Revert PlayerTeam
```
DELETE /api/player-teams/{playerTeamCode}

Path Parameters:
  playerTeamCode (String): Generated code (playerCode + teamSeasonCode)
                          Example: P001T001S2024

Response:
  204 No Content (success)
  404 Not Found (if PlayerTeam doesn't exist)
  500 Internal Server Error (if error occurs)
```

### Revert Unsold Player
```
DELETE /api/players/unsold

Query Parameters:
  playerCode (String): Player code
                      Example: P001
  seasonCode (String): Season code
                      Example: S2024

Response:
  204 No Content (success)
  404 Not Found (if UnsoldPlayer doesn't exist)
  500 Internal Server Error (if error occurs)
```

---

## Error Handling

### PlayerTeam Revert Errors
```json
{
  "error": {
    "status": 404,
    "code": "RESOURCE_NOT_FOUND",
    "message": "PlayerTeam not found with identifier: P001T001S2024",
    "timestamp": "2026-01-28T10:30:00",
    "path": "/api/player-teams/P001T001S2024"
  }
}
```

### Unsold Player Revert Errors
```json
{
  "error": {
    "status": 404,
    "code": "RESOURCE_NOT_FOUND",
    "message": "UnsoldPlayer not found with identifier: seasonId=1, playerId=1",
    "timestamp": "2026-01-28T10:30:00",
    "path": "/api/players/unsold"
  }
}
```

---

## Implementation Details

### PlayerTeam Revert Flow
```
1. Find PlayerTeam by code
2. Get affected team seasons
3. Delete PlayerTeam from database
4. Remove from team season's player list
5. Restore UnsoldPlayer if wasUnsold=true
6. Recalculate team statistics:
   - Total amount spent
   - RTM count
   - Free player count
   - Total player count
   - Player level summaries
7. Save updated TeamSeason
```

### Unsold Player Revert Flow
```
1. Find Player by code
2. Find Season by code
3. Find UnsoldPlayer by IDs
4. Validate exists
5. Delete from database
```

---

## Best Practices

### For PlayerTeam Revert
1. Use the generated code (playerCode + teamSeasonCode)
2. Verify player is in the team before reverting
3. Check team statistics after revert
4. Consider audit logging for compliance

### For Unsold Player Revert
1. Use player code and season code
2. Verify unsold marking exists before reverting
3. Check if player needs to be reassigned
4. Consider audit logging for compliance

---

## Testing Checklist

### PlayerTeam Revert Tests
- [ ] Revert new assignment
- [ ] Revert unsold player assignment
- [ ] Revert updated assignment
- [ ] Verify statistics recalculated
- [ ] Verify unsold record restored
- [ ] Handle non-existent PlayerTeam
- [ ] Verify transaction rollback on error

### Unsold Player Revert Tests
- [ ] Revert unsold marking
- [ ] Verify record deleted
- [ ] Handle non-existent UnsoldPlayer
- [ ] Handle invalid player code
- [ ] Handle invalid season code
- [ ] Verify transaction rollback on error

---

## Audit Trail Recommendations

### For PlayerTeam Revert
```java
auditLogService.log(
    "PLAYER_TEAM_REVERTED",
    playerTeamCode,
    userId,
    "Removed player from team"
);
```

### For Unsold Player Revert
```java
auditLogService.log(
    "UNSOLD_PLAYER_REVERTED",
    playerCode + "-" + seasonCode,
    userId,
    "Removed unsold marking"
);
```

---

## Future Enhancements

### Batch Revert Operations
```bash
# Revert multiple players at once
POST /api/batch/revert
{
  "playerTeamCodes": ["P001T001S2024", "P002T001S2024"],
  "unsoldPlayers": [
    {"playerCode": "P003", "seasonCode": "S2024"}
  ]
}
```

### Revert with Reason
```bash
DELETE /api/player-teams/{playerTeamCode}?reason=incorrect_assignment

DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024&reason=sold_later
```

### Revert History
```bash
GET /api/revert-history?type=player-team&limit=10
GET /api/revert-history?type=unsold-player&limit=10
```

---

## Summary

| Operation | Complexity | Use Case | Endpoint |
|-----------|-----------|----------|----------|
| Revert PlayerTeam | High | Remove from team | DELETE /api/player-teams/{code} |
| Revert Unsold | Low | Undo unsold marking | DELETE /api/players/unsold |

Both operations are:
- Transactional (atomic)
- Error-safe (proper exception handling)
- RESTful (standard HTTP methods)
- Production-ready

Choose the appropriate revert operation based on your use case!
