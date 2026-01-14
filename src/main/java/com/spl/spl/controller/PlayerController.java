package com.spl.spl.controller;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spl.spl.dto.PlayerResponse;
import com.spl.spl.dto.PlayerResponse.PlayerData;
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
	public PlayerResponse getAllPlayers() {
		List<Player> players = playerService.getAllPlayers();
		return new PlayerResponse(new PlayerData(players));
	}
}
