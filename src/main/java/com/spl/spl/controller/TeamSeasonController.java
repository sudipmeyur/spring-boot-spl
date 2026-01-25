package com.spl.spl.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.dto.ItemResponse;
import com.spl.spl.dto.ItemResponse.ItemData;
import com.spl.spl.dto.ItemsResponse;
import com.spl.spl.dto.ItemsResponse.ItemsData;
import com.spl.spl.entity.TeamSeason;
import com.spl.spl.service.TeamSeasonService;
import com.spl.spl.views.Views;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/team-seasons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeamSeasonController {

	private final TeamSeasonService teamSeasonService;

	@JsonView(Views.TeamSeasonsView.class)
	@GetMapping
	public ResponseEntity<ItemsResponse<TeamSeason>> getTeamSeasonsBySeasonCode(@RequestParam String seasonId) {
		List<TeamSeason> teamSeasons = teamSeasonService.getTeamSeasonsBySeason(Long.valueOf(seasonId));
		return ResponseEntity.ok(new ItemsResponse<>(new ItemsData<>(teamSeasons)));
	}
	
	@JsonView(Views.TeamSeasonView.class)
	@GetMapping("/{id}")
	public ResponseEntity<ItemResponse<TeamSeason>> getTeamSeasonById(@PathVariable String id) {
		TeamSeason teamSeason = teamSeasonService.getTeamSeason(Long.valueOf(id));
		return ResponseEntity.ok(new ItemResponse<>(new ItemData<>(teamSeason)));
	}
}