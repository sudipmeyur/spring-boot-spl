package com.spl.spl.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PlayerTeamRequest {
	private String playerCode;
	private String teamCode;
	private String seasonCode;
	private BigDecimal soldAmount;
}