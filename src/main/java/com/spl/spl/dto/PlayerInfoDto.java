package com.spl.spl.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.entity.Player;
import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.views.Views;

import lombok.Data;

@Data
public class PlayerInfoDto {
	
	@JsonView(Views.Summary.class)
	private Player player;
	
	@JsonView(Views.Summary.class)
	private TeamDto teamInfo;
	
	@JsonView(Views.Summary.class)
	private Boolean isUnsold;
	
	public PlayerInfoDto(Player player, PlayerTeam playerTeam,
			Boolean isUnsold) {
		super();
		this.player = player;
		
		if(playerTeam!=null && playerTeam.getTeamSeason()!=null) {
			this.teamInfo = TeamDto.builder().team(playerTeam.getTeamSeason().getTeam())
					.isManager(playerTeam.getIsManager()).isRtmUsed(playerTeam.getIsRtmUsed()).build();
		}
		this.isUnsold= isUnsold;
		
	}
	
}
