package com.spl.spl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spl.spl.dto.PlayerTeamRequest;
import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.service.PlayerTeamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/player-teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerTeamController {

	private final PlayerTeamService playerTeamService;

	@PostMapping
	public ResponseEntity<PlayerTeam> savePlayerTeam(@RequestBody PlayerTeamRequest request) {
		PlayerTeam savedPlayerTeam = playerTeamService.savePlayerTeam(request);
		return ResponseEntity.ok(savedPlayerTeam);
	}
	
	@DeleteMapping("/{playerTeamCode}")
	public ResponseEntity<Void> revertPlayerTeam(@PathVariable String playerTeamCode) {
		playerTeamService.revertPlayerTeam(playerTeamCode);
		return ResponseEntity.noContent().build();
	}
	
}