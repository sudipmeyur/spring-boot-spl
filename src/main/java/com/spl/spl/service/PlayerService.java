package com.spl.spl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spl.spl.dto.PlayerInfoDto;
import com.spl.spl.dto.PlayerTeamRequest;
import com.spl.spl.entity.Player;
import com.spl.spl.entity.Season;
import com.spl.spl.entity.UnsoldPlayer;
import com.spl.spl.exception.SplBadRequestException;
import com.spl.spl.repository.PlayerRepository;
import com.spl.spl.repository.SeasonRepository;
import com.spl.spl.repository.UnsoldPlayerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

	private final PlayerRepository playerRepository;
	private final SeasonRepository seasonRepository;
	private final UnsoldPlayerRepository unsoldPlayerRepository;
	private final Random random = new Random();

	public List<Player> getAllPlayers() {
		return playerRepository.findAll();
	}

	public List<Player> getAvailablePlayersByLevelShuffled(Long seasonId, Long playerLevelId) {
		List<Player> players = new ArrayList<>(playerRepository.findPlayersNotInTeamByLevel(seasonId, playerLevelId));
		Collections.shuffle(players, random);
		return players;
	}
	
	@Transactional
	public UnsoldPlayer saveUnsoldPlayer(PlayerTeamRequest request) {
		validateRequiredFields(request);
		Player player = playerRepository.findByCode(request.getPlayerCode());
		Season season = seasonRepository.findByCode(request.getSeasonCode());
		
		UnsoldPlayer unsoldPlayer = new UnsoldPlayer();
		unsoldPlayer.setPlayer(player);
		unsoldPlayer.setSeason(season);
		
		UnsoldPlayer savedUnsoldPlayer = unsoldPlayerRepository.save(unsoldPlayer);
		return savedUnsoldPlayer;
	}

	private void validateRequiredFields(PlayerTeamRequest request) {
		if(StringUtils.isAnyBlank(request.getSeasonCode(),request.getPlayerCode())) {
			throw new SplBadRequestException("SeasonCode and PlayerCode are required fields");
		}
	}

	public List<Player> getUnsoldPlayersShuffled(Long seasonId) {
		List<Player> players = new ArrayList<>(playerRepository.findUnsoldPlayers(seasonId));
		Collections.shuffle(players, random);
		return players;
	}

	public List<PlayerInfoDto> getAllAuctionResultPlayers(Long seasonId) {
		List<PlayerInfoDto> playerInfos = new ArrayList<>(playerRepository.findAllPlayers(seasonId));
		return playerInfos;
	}
}
