# Revert Unsold Player Guide

## Overview
This guide explains the best approach to revert a saved unsold player in the SPL application.

## What is `saveUnsoldPlayer`?

The `saveUnsoldPlayer` method marks a player as unsold for a specific season. This is used during the auction process to track players who didn't get sold.

```java
@Transactional
public UnsoldPlayer saveUnsoldPlayer(PlayerTeamRequest request) {
    // Validates required fields
    // Finds player and season by code
    // Creates UnsoldPlayer record
    // Returns saved record
}
```

### What Gets Saved
- **Player**: Reference to the player entity
- **Season**: Reference to the season entity
- **CreatedAt**: Timestamp of when marked as unsold
- **Unique Constraint**: Only one unsold record per player per season

## Why Revert is Needed

Scenarios where you need to revert:
1. **Incorrect Marking**: Player was marked as unsold by mistake
2. **Late Auction**: Player gets sold after being marked unsold
3. **Data Correction**: Need to undo unsold marking for re-auction
4. **Auction Restart**: Restarting auction process requires cleanup

## Revert Strategy

### Core Principle
**Revert = Delete the UnsoldPlayer record**

This is simpler than reverting PlayerTeam because:
- No cascading effects on team statistics
- No dependent records to restore
- Simple one-to-one relationship

### What Gets Reverted

1. **UnsoldPlayer Record Deleted**
   - Removes the unsold marking
   - Player becomes available for normal auction flow

2. **No Side Effects**
   - No team statistics to recalculate
   - No other records affected
   - Clean deletion

## Implementation

### Service Methods

#### Method 1: Revert by IDs
```java
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

#### Method 2: Revert by Codes (Recommended)
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
```

### Controller Endpoint

```java
@DeleteMapping("/unsold")
public ResponseEntity<Void> revertUnsoldPlayer(
        @RequestParam String playerCode,
        @RequestParam String seasonCode) {
    playerService.revertUnsoldPlayerByCode(playerCode, seasonCode);
    return ResponseEntity.noContent().build();
}
```

### Enhanced `saveUnsoldPlayer` Method

The method now includes:
1. **Null Checks**: Validates player and season exist
2. **Duplicate Prevention**: Checks if unsold record already exists
3. **Better Error Handling**: Throws appropriate exceptions

```java
@Transactional
public UnsoldPlayer saveUnsoldPlayer(PlayerTeamRequest request) {
    validateRequiredFields(request);
    Player player = playerRepository.findByCode(request.getPlayerCode());
    Season season = seasonRepository.findByCode(request.getSeasonCode());
    
    // Validate player exists
    if (player == null) {
        throw new ResourceNotFoundException("Player", request.getPlayerCode());
    }
    
    // Validate season exists
    if (season == null) {
        throw new ResourceNotFoundException("Season", request.getSeasonCode());
    }
    
    // Check if already marked as unsold
    UnsoldPlayer existingUnsoldPlayer = unsoldPlayerRepository
        .findBySeasonIdAndPlayerId(season.getId(), player.getId());
    if (existingUnsoldPlayer != null) {
        throw new DuplicateResourceException("UnsoldPlayer", 
            player.getCode() + "-" + season.getCode());
    }
    
    UnsoldPlayer unsoldPlayer = new UnsoldPlayer();
    unsoldPlayer.setPlayer(player);
    unsoldPlayer.setSeason(season);
    
    return unsoldPlayerRepository.save(unsoldPlayer);
}
```

## Usage Examples

### Example 1: Mark Player as Unsold
```bash
POST /api/players/unsold
Content-Type: application/json

{
  "playerCode": "P001",
  "seasonCode": "S2024"
}

# Response: 200 OK
{
  "id": 1,
  "player": { "id": 1, "code": "P001", "name": "Player One" },
  "season": { "id": 1, "code": "S2024", "year": 2024 },
  "createdAt": "2026-01-28T10:30:00"
}
```

### Example 2: Revert Unsold Marking
```bash
DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024

# Response: 204 No Content
```

### Example 3: Revert Multiple Players
```bash
# Revert P001
DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024

# Revert P002
DELETE /api/players/unsold?playerCode=P002&seasonCode=S2024

# Revert P003
DELETE /api/players/unsold?playerCode=P003&seasonCode=S2024
```

## Key Features

### 1. **Transactional Safety**
- Entire revert operation is atomic
- If any step fails, transaction rolls back
- No partial reverts

### 2. **Simple & Efficient**
- Direct deletion without complex logic
- No cascading updates needed
- Minimal database operations

### 3. **Proper Error Handling**
- Throws `ResourceNotFoundException` if unsold player doesn't exist
- Throws `ResourceNotFoundException` if player doesn't exist
- Throws `ResourceNotFoundException` if season doesn't exist
- Returns appropriate HTTP status codes

### 4. **Duplicate Prevention**
- `saveUnsoldPlayer` now checks for existing records
- Prevents duplicate unsold markings
- Throws `DuplicateResourceException` if already unsold

### 5. **Flexible Revert Methods**
- By IDs: `revertUnsoldPlayer(seasonId, playerId)`
- By Codes: `revertUnsoldPlayerByCode(playerCode, seasonCode)`
- Choose based on your use case

## Comparison: Save vs Revert

| Operation | Method | Input | Output | Effect |
|-----------|--------|-------|--------|--------|
| Save | POST /api/players/unsold | PlayerTeamRequest | UnsoldPlayer | Creates unsold record |
| Revert | DELETE /api/players/unsold | playerCode, seasonCode | 204 No Content | Deletes unsold record |

## Error Scenarios

### Scenario 1: Revert Non-Existent Unsold Player
```bash
DELETE /api/players/unsold?playerCode=P999&seasonCode=S2024

# Response: 404 Not Found
{
  "error": {
    "status": 404,
    "code": "RESOURCE_NOT_FOUND",
    "message": "UnsoldPlayer not found with identifier: seasonId=1, playerId=999",
    "timestamp": "2026-01-28T10:35:00"
  }
}
```

### Scenario 2: Save Duplicate Unsold Player
```bash
POST /api/players/unsold
{
  "playerCode": "P001",
  "seasonCode": "S2024"
}

# First call: 200 OK (created)
# Second call: 409 Conflict
{
  "error": {
    "status": 409,
    "code": "DUPLICATE_RESOURCE",
    "message": "UnsoldPlayer already exists with identifier: P001-S2024",
    "timestamp": "2026-01-28T10:36:00"
  }
}
```

### Scenario 3: Save with Invalid Player
```bash
POST /api/players/unsold
{
  "playerCode": "INVALID",
  "seasonCode": "S2024"
}

# Response: 404 Not Found
{
  "error": {
    "status": 404,
    "code": "RESOURCE_NOT_FOUND",
    "message": "Player not found with identifier: INVALID",
    "timestamp": "2026-01-28T10:37:00"
  }
}
```

## Data Flow

### Save Flow
```
POST /api/players/unsold
    ↓
PlayerController.saveUnsoldPlayer()
    ↓
PlayerService.saveUnsoldPlayer()
    ├─ Validate required fields
    ├─ Find Player by code
    ├─ Find Season by code
    ├─ Check for duplicates
    ├─ Create UnsoldPlayer
    └─ Save to database
    ↓
Return UnsoldPlayer (201 Created)
```

### Revert Flow
```
DELETE /api/players/unsold?playerCode=P001&seasonCode=S2024
    ↓
PlayerController.revertUnsoldPlayer()
    ↓
PlayerService.revertUnsoldPlayerByCode()
    ├─ Find Player by code
    ├─ Find Season by code
    └─ Call revertUnsoldPlayer()
        ├─ Find UnsoldPlayer by IDs
        ├─ Validate exists
        └─ Delete from database
    ↓
Return 204 No Content
```

## Relationship with PlayerTeam

### Important Note
When a player is marked as unsold and later assigned to a team:

1. **Save PlayerTeam with `wasUnsold=true`**
   ```java
   PlayerTeamRequest request = new PlayerTeamRequest();
   request.setIsUnsold(true);  // Mark as unsold
   playerTeamService.savePlayerTeam(request);
   ```

2. **PlayerTeamService automatically cleans up**
   ```java
   if(result.getWasUnsold() != null && result.getWasUnsold()) {
       UnsoldPlayer existingUnsoldPlayer = unsoldPlayerRepository
           .findBySeasonIdAndPlayerId(season.getId(), player.getId());
       if(existingUnsoldPlayer != null) {
           unsoldPlayerRepository.delete(existingUnsoldPlayer);
       }
   }
   ```

3. **Reverting PlayerTeam restores UnsoldPlayer**
   ```java
   if (playerTeam.getWasUnsold() != null && playerTeam.getWasUnsold()) {
       UnsoldPlayer unsoldPlayer = new UnsoldPlayer();
       unsoldPlayer.setPlayer(player);
       unsoldPlayer.setSeason(season);
       unsoldPlayerRepository.save(unsoldPlayer);
   }
   ```

## Testing

### Test Case 1: Save and Revert Unsold Player
```java
@Test
void testSaveAndRevertUnsoldPlayer() {
    // Save unsold player
    PlayerTeamRequest request = new PlayerTeamRequest();
    request.setPlayerCode("P001");
    request.setSeasonCode("S2024");
    UnsoldPlayer saved = playerService.saveUnsoldPlayer(request);
    
    assertNotNull(saved.getId());
    assertEquals("P001", saved.getPlayer().getCode());
    
    // Revert
    playerService.revertUnsoldPlayerByCode("P001", "S2024");
    
    // Verify deleted
    UnsoldPlayer deleted = unsoldPlayerRepository
        .findBySeasonIdAndPlayerId(saved.getSeason().getId(), saved.getPlayer().getId());
    assertNull(deleted);
}
```

### Test Case 2: Prevent Duplicate Unsold
```java
@Test
void testPreventDuplicateUnsoldPlayer() {
    PlayerTeamRequest request = new PlayerTeamRequest();
    request.setPlayerCode("P001");
    request.setSeasonCode("S2024");
    
    // First save succeeds
    playerService.saveUnsoldPlayer(request);
    
    // Second save throws exception
    assertThrows(DuplicateResourceException.class, () -> {
        playerService.saveUnsoldPlayer(request);
    });
}
```

### Test Case 3: Revert Non-Existent Unsold Player
```java
@Test
void testRevertNonExistentUnsoldPlayer() {
    assertThrows(ResourceNotFoundException.class, () -> {
        playerService.revertUnsoldPlayerByCode("INVALID", "S2024");
    });
}
```

## Best Practices

1. **Always Use Codes for Revert**
   - Use `revertUnsoldPlayerByCode()` in controllers
   - More user-friendly than IDs
   - Matches the save request format

2. **Validate Before Reverting**
   - Check if unsold player exists
   - Provide clear error messages
   - Handle 404 gracefully

3. **Use Transactions**
   - All operations are `@Transactional`
   - Ensures data consistency
   - Automatic rollback on errors

4. **Log Operations**
   - Consider adding audit logging
   - Track who reverted and when
   - Useful for debugging

5. **Handle Errors Gracefully**
   - Catch `ResourceNotFoundException`
   - Return appropriate HTTP status
   - Provide meaningful error messages

## Summary

The `revertUnsoldPlayer` method provides a clean, efficient way to undo unsold player markings. It:
- Deletes the UnsoldPlayer record
- Validates player and season exist
- Maintains data consistency through transactions
- Provides clear error handling
- Works seamlessly with PlayerTeam operations

This is the recommended approach for reverting unsold player markings in the SPL application.
