package com.spl.spl.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spl.spl.dto.ItemsResponse;
import com.spl.spl.dto.ItemsResponse.ItemsData;
import com.spl.spl.dto.PlayerTeamRequest;
import com.spl.spl.entity.Player;
import com.spl.spl.entity.UnsoldPlayer;
import com.spl.spl.service.PlayerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerController {

	private final PlayerService playerService;

	@GetMapping
	public ResponseEntity<ItemsResponse<Player>> getAllPlayers() {
		List<Player> players = playerService.getAllPlayers();
		return ResponseEntity.ok(new ItemsResponse<>(new ItemsData<>(players)));
	}

	@GetMapping("/available")
	public ResponseEntity<ItemsResponse<Player>> getAvailablePlayers(
			@RequestParam String seasonId,
			@RequestParam String playerLevelId) {
		List<Player> players = playerService.getAvailablePlayersByLevelShuffled(Long.valueOf(seasonId), Long.valueOf(playerLevelId));
		return ResponseEntity.ok(new ItemsResponse<>(new ItemsData<>(players)));
	}
	
	@PostMapping("/unsold")
	public ResponseEntity<UnsoldPlayer> saveUnsoldPlayer(@RequestBody PlayerTeamRequest request) {
		UnsoldPlayer savedUnsoldPlayer = playerService.saveUnsoldPlayer(request);
		return ResponseEntity.ok(savedUnsoldPlayer);
	}
	
	@GetMapping("/unsold")
	public ResponseEntity<ItemsResponse<Player>> getUnsoldPlayers(
			@RequestParam String seasonId) {
		List<Player> players = playerService.getUnsoldPlayersShuffled(Long.valueOf(seasonId));
		return ResponseEntity.ok(new ItemsResponse<>(new ItemsData<>(players)));
	}
}
