package com.spl.spl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import com.spl.spl.entity.Player;
import com.spl.spl.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

	private final PlayerRepository playerRepository;
	private final Random random = new Random();

	public List<Player> getAllPlayers() {
		return playerRepository.findAll();
	}

	public List<Player> getAvailablePlayersByLevelShuffled(String seasonCode, String playerLevelCode) {
		List<Player> players = new ArrayList<>(playerRepository.findPlayersNotInSeasonByLevel(seasonCode, playerLevelCode));
		Collections.shuffle(players, random);
		return players;
	}
}
