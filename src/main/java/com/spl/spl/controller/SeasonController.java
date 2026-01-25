package com.spl.spl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.dto.ItemResponse;
import com.spl.spl.dto.ItemResponse.ItemData;
import com.spl.spl.entity.Season;
import com.spl.spl.service.SeasonService;
import com.spl.spl.views.Views;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seasons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SeasonController {

	private final SeasonService seasonService;

	@JsonView(Views.SeasonView.class)
	@GetMapping("/current")
	public ResponseEntity<ItemResponse<Season>> getCurrentSeason() {
		Season currentSeason = seasonService.getCurrentSeason();
		return ResponseEntity.ok(new ItemResponse<>(new ItemData<>(currentSeason)));
	}

	
}