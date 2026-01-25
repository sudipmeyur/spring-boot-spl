package com.spl.spl.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.spl.spl.entity.TeamSeason;
import com.spl.spl.repository.TeamSeasonRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamSeasonService {

	private final TeamSeasonRepository teamSeasonRepository;

	public List<TeamSeason> getTeamSeasonsBySeason(Long seasonId) {
		return teamSeasonRepository.findBySeasonId(seasonId);
	}

	public TeamSeason getTeamSeason(Long id) {
		return teamSeasonRepository.findById(id).orElse(null);
	}
}