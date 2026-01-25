package com.spl.spl.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.spl.spl.entity.PlayerLevel;
import com.spl.spl.repository.PlayerLevelRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerLevelService {

	private final PlayerLevelRepository playerLevelRepository;

	public List<PlayerLevel> getAllPlayerLevels() {
		return playerLevelRepository.findAll();
	}
}