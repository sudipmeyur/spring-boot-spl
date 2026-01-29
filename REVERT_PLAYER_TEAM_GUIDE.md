# Player Team Revert Guide

## Overview
This guide explains the best approach to revert a saved player team assignment in the SPL application.

## Why Revert is Complex

The `savePlayerTeam` method handles three scenarios:
1. **New Assignment**: Creates a new PlayerTeam record
2. **Player Transfer**: Deletes old assignment and creates new one in different team
3. **Update Existing**: Modifies fields on existing assignment

Reverting must handle all three cases correctly.

## Revert Strategy

### Core Principle
**Revert = Delete the PlayerTeam assignment and restore the previous state**

### What Gets Reverted

#### 1. **PlayerTeam Record**
- Deletes the PlayerTeam from database
- Removes from team season's player list
- Recalculates team statistics

#### 2. **Unsold Player Record** (if applicable)
- If the player was marked as `wasUnsold=true`, restores the UnsoldPlayer record
- This allows the player to be available for re-auction

#### 3. **Team Season Statistics**
- Recalculates total amount spent
- Recalculates RTM count
- Recalculates free player count
- Recalculates total player count
- Updates TeamSeasonPlayerLevel summaries by player level

## Implementation

### Service Method: `revertPlayerTeam(String playerTeamCode)`

```java
@Transactional
public void revertPlayerTeam(String playerTeamCode) {
    // 1. Find the PlayerTeam record
    PlayerTeam playerTeam = playerTeamRepository.findByCode(playerTeamCode);
    
    if (playerTeam == null) {
        throw new ResourceNotFoundException("PlayerTeam", playerTeamCode);
    }
    
    // 2. Gather context
    List<TeamSeason> affectedTeamSeasons = new ArrayList<>();
    TeamSeason teamSeason = playerTeam.getTeamSeason();
    Season season = teamSeason.getSeason();
    Player player = playerTeam.getPlayer();
    
    affectedTeamSeasons.add(teamSeason);
    
    // 3. Delete PlayerTeam assignment
    teamSeason.getPlayerTeams().remove(playerTeam);
    playerTeamRepository.delete(playerTeam);
    
    // 4. Restore unsold player if applicable
    if (playerTeam.getWasUnsold() != null && playerTeam.getWasUnsold()) {
        UnsoldPlayer unsoldPlayer = new UnsoldPlayer();
        unsoldPlayer.setPlayer(player);
        unsoldPlayer.setSeason(season);
        unsoldPlayerRepository.save(unsoldPlayer);
    }
    
    // 5. Recalculate team statistics
    manageTeamSeasonStatus(affectedTeamSeasons);
}
```

### Controller Endpoint

```java
@DeleteMapping("/{playerTeamCode}")
public ResponseEntity<Void> revertPlayerTeam(@PathVariable String playerTeamCode) {
    playerTeamService.revertPlayerTeam(playerTeamCode);
    return ResponseEntity.noContent().build();
}
```

## Usage Examples

### Example 1: Revert a New Assignment
```bash
# Player "P001" was assigned to team "T001" in season "S2024"
# Generated code: P001T001S2024

DELETE /api/player-teams/P001T001S2024

# Result:
# - PlayerTeam record deleted
# - Team statistics recalculated
# - Player available for re-assignment
```

### Example 2: Revert an Unsold Player Assignment
```bash
# Unsold player "P002" was assigned to team "T002"
# Generated code: P002T002S2024

DELETE /api/player-teams/P002T002S2024

# Result:
# - PlayerTeam record deleted
# - UnsoldPlayer record restored
# - Player marked as unsold again
# - Team statistics recalculated
```

### Example 3: Revert an Updated Assignment
```bash
# Player amount was updated from 100 to 150
# Generated code: P003T003S2024

DELETE /api/player-teams/P003T003S2024

# Result:
# - PlayerTeam record deleted
# - Team statistics recalculated
# - Player available for re-assignment
```

## Key Features

### 1. **Transactional Safety**
- Entire revert operation is atomic
- If any step fails, entire transaction rolls back
- No partial reverts

### 2. **Automatic Statistics Recalculation**
- Uses existing `manageTeamSeasonStatus()` method
- Recalculates all team totals
- Updates player level summaries

### 3. **Unsold Player Restoration**
- Automatically restores UnsoldPlayer record if needed
- Allows player to be re-auctioned

### 4. **Error Handling**
- Throws `ResourceNotFoundException` if PlayerTeam doesn't exist
- Proper HTTP 404 response

## Limitations & Considerations

### What Revert Does NOT Do

1. **Does NOT restore previous values** - It completely removes the assignment
   - If you need to restore previous values, use UPDATE instead of DELETE

2. **Does NOT handle player transfers** - If player was transferred from Team A to Team B:
   - Reverting only removes from Team B
   - Does NOT restore to Team A
   - Use UPDATE to move back to Team A

3. **Does NOT track revert history** - No audit trail of reversions
   - Consider adding audit logging if needed

### When to Use Revert vs Update

| Scenario | Use Revert | Use Update |
|----------|-----------|-----------|
| Remove player from team completely | ✓ | ✗ |
| Change sold amount | ✗ | ✓ |
| Transfer player to different team | ✗ | ✓ |
| Undo unsold marking | ✓ | ✗ |
| Restore previous assignment | ✗ | ✓ |

## Alternative Approaches (Not Recommended)

### Approach 1: Soft Delete (Not Used)
- Add `isDeleted` flag instead of hard delete
- **Disadvantage**: Complicates queries, requires filtering everywhere

### Approach 2: Audit Trail with Rollback (Not Used)
- Store complete history of all changes
- Rollback to specific timestamp
- **Disadvantage**: Complex implementation, high storage overhead

### Approach 3: Event Sourcing (Not Used)
- Store all events and replay to restore state
- **Disadvantage**: Significant architectural change

## Recommended Approach: Current Implementation

The current `revertPlayerTeam` method is the best approach because:

1. **Simple & Clear**: Easy to understand and maintain
2. **Efficient**: Direct deletion without complex history tracking
3. **Consistent**: Uses existing statistics recalculation logic
4. **Transactional**: Atomic operations ensure data consistency
5. **Flexible**: Works for all three assignment scenarios

## Future Enhancements

If you need more advanced revert capabilities:

1. **Add Audit Logging**
   ```java
   auditLogService.logRevert(playerTeamCode, reason);
   ```

2. **Add Revert Reason**
   ```java
   public void revertPlayerTeam(String playerTeamCode, String reason)
   ```

3. **Add Revert History**
   - Store revert events in separate table
   - Track who reverted and when

4. **Add Conditional Revert**
   - Only allow revert within certain time window
   - Only allow revert by specific roles

## Testing

### Test Case 1: Revert New Assignment
```java
@Test
void testRevertNewPlayerTeamAssignment() {
    // Create assignment
    PlayerTeam pt = savePlayerTeam(request);
    
    // Revert
    playerTeamService.revertPlayerTeam(pt.getCode());
    
    // Verify
    assertNull(playerTeamRepository.findByCode(pt.getCode()));
    assertEquals(0, teamSeason.getPlayerTeams().size());
}
```

### Test Case 2: Revert Unsold Player
```java
@Test
void testRevertUnsoldPlayerRestoresRecord() {
    // Create assignment with wasUnsold=true
    PlayerTeam pt = savePlayerTeam(requestWithUnsold);
    
    // Verify unsold record was deleted
    assertNull(unsoldPlayerRepository.findBySeasonIdAndPlayerId(seasonId, playerId));
    
    // Revert
    playerTeamService.revertPlayerTeam(pt.getCode());
    
    // Verify unsold record restored
    assertNotNull(unsoldPlayerRepository.findBySeasonIdAndPlayerId(seasonId, playerId));
}
```

### Test Case 3: Revert Recalculates Statistics
```java
@Test
void testRevertRecalculatesTeamStatistics() {
    // Create assignment
    PlayerTeam pt = savePlayerTeam(request);
    
    // Verify statistics updated
    assertEquals(1, teamSeason.getTotalPlayer());
    assertEquals(expectedAmount, teamSeason.getTotalAmountSpent());
    
    // Revert
    playerTeamService.revertPlayerTeam(pt.getCode());
    
    // Verify statistics reset
    assertEquals(0, teamSeason.getTotalPlayer());
    assertEquals(BigDecimal.ZERO, teamSeason.getTotalAmountSpent());
}
```

## Summary

The `revertPlayerTeam` method provides a clean, efficient way to undo player team assignments. It:
- Deletes the PlayerTeam record
- Restores unsold player records if applicable
- Recalculates all team statistics
- Maintains data consistency through transactions

This is the recommended approach for reverting player team assignments in the SPL application.
