package com.spl.spl.dto;

import java.util.Map;

import com.spl.spl.entity.TeamSeasonPlayerLevel;

import lombok.Data;

@Data
public class PlayerLevelCalcDto {
	 private Map<String,TeamSeasonPlayerLevel> playerLevels;

}
