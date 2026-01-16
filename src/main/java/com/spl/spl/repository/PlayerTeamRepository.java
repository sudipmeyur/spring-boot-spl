package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.entity.PlayerTeamId;

public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, PlayerTeamId> {
	PlayerTeam findByCode(String code);
}
