package com.spl.spl.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.spl.spl.entity.Player;
import com.spl.spl.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

	private final PlayerRepository playerRepository;

	public List<Player> getAllPlayers() {
		return playerRepository.findAll();
	}
}
