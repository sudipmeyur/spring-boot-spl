package com.spl.spl.dto;

import java.util.List;
import com.spl.spl.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerResponse {
	private PlayerData data;

	@Data
	@AllArgsConstructor
	public static class PlayerData {
		private List<Player> players;
	}
}
