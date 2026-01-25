package com.spl.spl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spl.spl.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

	Player findByCode(String code);

	@Query("SELECT p FROM Player p WHERE p.playerLevel.id = :playerLevelId AND p.id NOT IN (SELECT pt.player.id FROM PlayerTeam pt WHERE pt.teamSeason.season.id = :seasonId)")
	List<Player> findPlayersNotInTeamByLevel(@Param("seasonId") Long seasonId, @Param("playerLevelId") Long playerLevelId);
}
