package com.spl.spl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spl.spl.dto.PlayerInfoDto;
import com.spl.spl.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

	Player findByCode(String code);
	List<Player> findByIsActive(Boolean isActive);

	@Query("""
		SELECT p FROM Player p 
		LEFT JOIN PlayerTeam pt ON p.id = pt.player.id AND pt.teamSeason.season.id = :seasonId
		LEFT JOIN UnsoldPlayer usp ON p.id = usp.player.id AND usp.season.id = :seasonId
		WHERE p.playerLevel.id = :playerLevelId
		AND pt.player.id IS NULL 
		AND usp.player.id IS NULL
		AND p.isActive = true
		""")
	List<Player> findPlayersNotInTeamByLevel(@Param("seasonId") Long seasonId, @Param("playerLevelId") Long playerLevelId);

	@Query("""
		SELECT p FROM Player p 
		JOIN UnsoldPlayer usp ON p.id = usp.player.id AND usp.season.id = :seasonId
		WHERE p.isActive = true
		""")
	List<Player> findUnsoldPlayers(@Param("seasonId") Long seasonId);
	
	@Query("""
		SELECT new com.spl.spl.dto.PlayerInfoDto(p,
			pt,
			usp
		)
		FROM Player p 
		LEFT JOIN PlayerTeam pt ON p.id = pt.player.id AND pt.teamSeason.season.id = :seasonId
		LEFT JOIN UnsoldPlayer usp ON p.id = usp.player.id AND usp.season.id = :seasonId
		WHERE p.isActive = true
		""")
	List<PlayerInfoDto> findAllPlayers(@Param("seasonId") Long seasonId);

	
}
