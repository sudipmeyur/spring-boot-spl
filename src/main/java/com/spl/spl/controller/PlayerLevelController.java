package com.spl.spl.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.dto.ItemsResponse;
import com.spl.spl.dto.ItemsResponse.ItemsData;
import com.spl.spl.entity.PlayerLevel;
import com.spl.spl.service.PlayerLevelService;
import com.spl.spl.views.Views;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/player-levels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerLevelController {

	private final PlayerLevelService playerLevelService;

	@JsonView(Views.PlayerLevel.class)
	@GetMapping
	public ResponseEntity<ItemsResponse<PlayerLevel>> getAllPlayerLevels() {
		List<PlayerLevel> playerLevels = playerLevelService.getAllPlayerLevels();
		return ResponseEntity.ok(new ItemsResponse<>(new ItemsData<>(playerLevels)));
	}
}