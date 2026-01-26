package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spl.spl.entity.UnsoldPlayer;

public interface UnsoldPlayerRepository extends JpaRepository<UnsoldPlayer, Long> {
	
	UnsoldPlayer findBySeasonIdAndPlayerId(Long seasonId, Long playerId);
}
