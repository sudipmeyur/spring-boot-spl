package com.spl.spl.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spl.spl.dto.PlayerTeamRequest;
import com.spl.spl.entity.Player;
import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.entity.Season;
import com.spl.spl.entity.Team;
import com.spl.spl.repository.PlayerRepository;
import com.spl.spl.repository.PlayerTeamRepository;
import com.spl.spl.repository.SeasonRepository;
import com.spl.spl.repository.TeamRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerTeamService {

	private final PlayerTeamRepository playerTeamRepository;
	private final PlayerRepository playerRepository;
	private final TeamRepository teamRepository;
	private final SeasonRepository seasonRepository;

	@Transactional
	public PlayerTeam savePlayerTeam(PlayerTeamRequest request) {
		Player player = playerRepository.findByCode(request.getPlayerCode());
		Team team = teamRepository.findByCode(request.getTeamCode());
		Season season = seasonRepository.findByCode(request.getSeasonCode());

		PlayerTeam playerTeam = new PlayerTeam();
		playerTeam.setPlayer(player);
		playerTeam.setTeam(team);
		playerTeam.setSeason(season);
		playerTeam.setSoldAmount(request.getSoldAmount());

		return playerTeamRepository.save(playerTeam);
	}
}