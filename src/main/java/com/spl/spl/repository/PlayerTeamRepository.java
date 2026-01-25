package com.spl.spl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.entity.PlayerTeamId;

public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, PlayerTeamId> {
	PlayerTeam findByCode(String code);
	List<PlayerTeam> findByTeamSeasonId(Long teamSeasonId);
}
