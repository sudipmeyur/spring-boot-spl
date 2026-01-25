package com.spl.spl.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemResponse<T> {
	@JsonView(Views.Base.class)
	private ItemData<T> data;

	@Data
	@AllArgsConstructor
	public static class ItemData<T> {
		@JsonView(Views.Base.class)
		private T item;
	}
}
