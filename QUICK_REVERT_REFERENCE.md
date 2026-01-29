# Quick Revert Reference

## Revert Unsold Player - Quick Guide

### What It Does
Removes the unsold marking from a player, making them available for normal auction flow.

### When to Use
- Player was marked unsold by mistake
- Player gets sold after being marked unsold
- Need to restart auction process
- Data correction needed

### API Endpoint
```bash
DELETE /api/players/unsold?playerCode={playerCode}&seasonCode={seasonCode}
```

### Usage Examples

#### Example 1: Basic Revert
```bash
curl -X DELETE "http://localhost:8080/api/players/unsold?playerCode=P001&seasonCode=S2024"

# Response: 204 No Content
```

#### Example 2: Using cURL with Variables
```bash
PLAYER_CODE="P001"
SEASON_CODE="S2024"

curl -X DELETE "http://localhost:8080/api/players/unsold?playerCode=${PLAYER_CODE}&seasonCode=${SEASON_CODE}"
```

#### Example 3: Using Postman
```
Method: DELETE
URL: http://localhost:8080/api/players/unsold
Query Params:
  - playerCode: P001
  - seasonCode: S2024
```

### Response Codes
- **204 No Content**: Success - unsold marking removed
- **404 Not Found**: UnsoldPlayer doesn't exist
- **500 Internal Server Error**: Server error

### Error Response Example
```json
{
  "error": {
    "status": 404,
    "code": "RESOURCE_NOT_FOUND",
    "message": "UnsoldPlayer not found with identifier: seasonId=1, playerId=1",
    "details": "The requested UnsoldPlayer with identifier 'seasonId=1, playerId=1' could not be found",
    "timestamp": "2026-01-28T10:30:00.123456",
    "path": "/api/players/unsold"
  }
}
```

### Service Methods

#### Method 1: By IDs (Internal Use)
```java
playerService.revertUnsoldPlayer(Long seasonId, Long playerId);
```

#### Method 2: By Codes (Recommended)
```java
playerService.revertUnsoldPlayerByCode(String playerCode, String seasonCode);
```

### Key Features
✓ Transactional (atomic operation)
✓ Simple and efficient
✓ No cascading effects
✓ Proper error handling
✓ RESTful API

### Related Operations

#### Save Unsold Player
```bash
POST /api/players/unsold
Content-Type: application/json

{
  "playerCode": "P001",
  "seasonCode": "S2024"
}
```

#### Get Unsold Players
```bash
GET /api/players/unsold?seasonId=1
```

#### Assign Unsold Player to Team
```bash
POST /api/player-teams
Content-Type: application/json

{
  "playerCode": "P001",
  "teamSeasonCode": "T001S2024",
  "soldAmount": 100,
  "isUnsold": true
}
```

### Common Workflows

#### Workflow 1: Undo Unsold Marking
```
1. Player marked as unsold
   POST /api/players/unsold
   
2. Realize it was a mistake
   DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024
   
3. Player available for normal auction
   GET /api/players/available?seasonId=1&playerLevelId=1
```

#### Workflow 2: Unsold Player Gets Sold
```
1. Player marked as unsold
   POST /api/players/unsold
   
2. Player gets sold later
   POST /api/player-teams
   {
     "playerCode": "P001",
     "teamSeasonCode": "T001S2024",
     "soldAmount": 100,
     "isUnsold": true
   }
   
3. Unsold record automatically removed by PlayerTeamService
```

#### Workflow 3: Restart Auction
```
1. Revert all unsold markings
   DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024
   DELETE /api/players/unsold?playerCode=P002&seasonCode=S2024
   DELETE /api/players/unsold?playerCode=P003&seasonCode=S2024
   
2. All players available for re-auction
```

### Comparison with PlayerTeam Revert

| Aspect | Unsold Player | PlayerTeam |
|--------|---------------|-----------|
| Endpoint | DELETE /api/players/unsold | DELETE /api/player-teams/{code} |
| Parameters | Query params | Path param |
| Complexity | Low | High |
| Side Effects | None | Yes (stats) |
| Use Case | Undo unsold marking | Remove from team |

### Troubleshooting

#### Issue: 404 Not Found
**Cause**: UnsoldPlayer doesn't exist
**Solution**: 
- Verify playerCode is correct
- Verify seasonCode is correct
- Check if player was actually marked as unsold

#### Issue: 500 Internal Server Error
**Cause**: Server error
**Solution**:
- Check server logs
- Verify database connection
- Contact support if persists

### Implementation Details

#### Service Layer
```java
@Transactional
public void revertUnsoldPlayerByCode(String playerCode, String seasonCode) {
    Player player = playerRepository.findByCode(playerCode);
    Season season = seasonRepository.findByCode(seasonCode);
    
    if (player == null) {
        throw new ResourceNotFoundException("Player", playerCode);
    }
    
    if (season == null) {
        throw new ResourceNotFoundException("Season", seasonCode);
    }
    
    revertUnsoldPlayer(season.getId(), player.getId());
}

@Transactional
public void revertUnsoldPlayer(Long seasonId, Long playerId) {
    UnsoldPlayer unsoldPlayer = unsoldPlayerRepository
        .findBySeasonIdAndPlayerId(seasonId, playerId);
    
    if (unsoldPlayer == null) {
        throw new ResourceNotFoundException("UnsoldPlayer", 
            "seasonId=" + seasonId + ", playerId=" + playerId);
    }
    
    unsoldPlayerRepository.delete(unsoldPlayer);
}
```

#### Controller Layer
```java
@DeleteMapping("/unsold")
public ResponseEntity<Void> revertUnsoldPlayer(
        @RequestParam String playerCode,
        @RequestParam String seasonCode) {
    playerService.revertUnsoldPlayerByCode(playerCode, seasonCode);
    return ResponseEntity.noContent().build();
}
```

### Testing

#### Unit Test
```java
@Test
void testRevertUnsoldPlayer() {
    // Setup
    PlayerTeamRequest request = new PlayerTeamRequest();
    request.setPlayerCode("P001");
    request.setSeasonCode("S2024");
    
    // Save unsold player
    UnsoldPlayer saved = playerService.saveUnsoldPlayer(request);
    assertNotNull(saved.getId());
    
    // Revert
    playerService.revertUnsoldPlayerByCode("P001", "S2024");
    
    // Verify deleted
    UnsoldPlayer deleted = unsoldPlayerRepository
        .findBySeasonIdAndPlayerId(saved.getSeason().getId(), saved.getPlayer().getId());
    assertNull(deleted);
}
```

#### Integration Test
```bash
# Save unsold player
curl -X POST "http://localhost:8080/api/players/unsold" \
  -H "Content-Type: application/json" \
  -d '{"playerCode":"P001","seasonCode":"S2024"}'

# Verify saved
curl -X GET "http://localhost:8080/api/players/unsold?seasonId=1"

# Revert
curl -X DELETE "http://localhost:8080/api/players/unsold?playerCode=P001&seasonCode=S2024"

# Verify reverted
curl -X GET "http://localhost:8080/api/players/unsold?seasonId=1"
```

### Best Practices

1. **Always verify before reverting**
   - Check if unsold player exists
   - Confirm it's the right player and season

2. **Use consistent codes**
   - Use same playerCode and seasonCode format
   - Match the format used in save operation

3. **Handle errors gracefully**
   - Catch ResourceNotFoundException
   - Provide user-friendly error messages

4. **Log operations**
   - Track who reverted and when
   - Useful for audit trail

5. **Test thoroughly**
   - Test happy path
   - Test error scenarios
   - Test edge cases

### Related Documentation
- See `REVERT_UNSOLD_PLAYER_GUIDE.md` for detailed guide
- See `REVERT_PLAYER_TEAM_GUIDE.md` for PlayerTeam revert
- See `REVERT_OPERATIONS_SUMMARY.md` for comparison
- See `ERROR_HANDLING_GUIDE.md` for error handling

### Summary
The revert unsold player operation is a simple, efficient way to undo unsold markings. It's transactional, error-safe, and production-ready.

**Use it when**: You need to remove unsold marking from a player
**Endpoint**: `DELETE /api/players/unsold?playerCode={code}&seasonCode={code}`
**Response**: 204 No Content on success
