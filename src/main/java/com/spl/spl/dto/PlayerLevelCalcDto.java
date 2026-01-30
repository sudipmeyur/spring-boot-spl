package com.spl.spl.dto;

import java.util.Map;

import com.spl.spl.entity.Season;
import com.spl.spl.entity.TeamSeason;
import com.spl.spl.entity.TeamSeasonPlayerLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerLevelCalcDto {
	private TeamSeason team;
	private Season season;
	/* store TeamSeasonPlayerLevel associated with teamSeason in a map 
	 * l1 = TeamSeasonPlayerLevel.playerLevel.code
	 * Ex. { l1 : <TeamSeasonPlayerLevel for l1>,...}
	 * */
	private Map<String, TeamSeasonPlayerLevel> playerLevels;

}
