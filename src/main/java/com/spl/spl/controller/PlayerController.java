package com.spl.spl.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.spl.spl.dto.Response;
import com.spl.spl.dto.Response.ResponseData;
import com.spl.spl.entity.Player;
import com.spl.spl.service.PlayerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerController {

	private final PlayerService playerService;

	@GetMapping
	public ResponseEntity<Response> getAllPlayers() {
		List<Player> players = playerService.getAllPlayers();
		return ResponseEntity.ok(new Response(new ResponseData(players)));
	}

	@GetMapping("/available")
	public ResponseEntity<Response> getAvailablePlayers(
			@RequestParam String seasonCode,
			@RequestParam String playerLevelCode) {
		List<Player> players = playerService.getAvailablePlayersByLevelShuffled(seasonCode, playerLevelCode);
		return ResponseEntity.ok(new Response(new ResponseData(players)));
	}
}
