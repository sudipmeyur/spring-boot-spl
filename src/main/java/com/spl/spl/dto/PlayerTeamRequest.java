package com.spl.spl.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PlayerTeamRequest {
	private String code;
	private String playerCode;
	private String teamSeasonCode;
	private BigDecimal soldAmount;
	private Boolean isFree;
	private Boolean isRtmUsed;
	private Boolean isUnsold;
	private Boolean isManager;
	private String seasonCode;
	
}