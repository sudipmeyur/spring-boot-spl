package com.spl.spl.dto;

import java.util.List;
import com.spl.spl.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
	private ResponseData data;

	@Data
	@AllArgsConstructor
	public static class ResponseData {
		private List<Player> players;
	}
}
