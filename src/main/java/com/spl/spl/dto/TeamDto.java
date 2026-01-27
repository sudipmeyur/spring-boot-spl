package com.spl.spl.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.entity.Team;
import com.spl.spl.views.Views;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {
	
	@JsonView(Views.Base.class)
	private Team team;
	
	@JsonView(Views.Summary.class)
	private BigDecimal soldAmount;
	
	@JsonView(Views.Summary.class)
	private Boolean isManager;
	
	@JsonView(Views.Summary.class)
	private Boolean isRtmUsed;

}
