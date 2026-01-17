package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spl.spl.entity.TeamSeason;

public interface TeamSeasonRepository extends JpaRepository<TeamSeason, Long> {
	TeamSeason findByCode(String code);
	TeamSeason findByTeamCodeAndSeasonCode(String teamCode, String seasonCode);
}