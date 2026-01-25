package com.spl.spl.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.spl.spl.entity.TeamSeasonPlayerLevel;

public interface TeamSeasonPlayerLevelRepository extends JpaRepository<TeamSeasonPlayerLevel, Long> {
	List<TeamSeasonPlayerLevel> findByTeamSeasonId(Long teamSeasonId);
	TeamSeasonPlayerLevel findByTeamSeasonIdAndPlayerLevelId(Long teamSeasonId, Long playerLevelId);
}