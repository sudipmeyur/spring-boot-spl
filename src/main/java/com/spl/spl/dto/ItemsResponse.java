package com.spl.spl.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemsResponse<T> {
	@JsonView(Views.Base.class)
	private ItemsData<T> data;

	@Data
	@AllArgsConstructor
	public static class ItemsData<T> {
		@JsonView(Views.Base.class)
		private List<T> items;
	}
}
