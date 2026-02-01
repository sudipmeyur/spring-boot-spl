package com.spl.spl.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "player", uniqueConstraints = {
	@UniqueConstraint(columnNames = "code")
})
@Data
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonView(Views.Summary.class)
	@Column(unique = true)
	private String code;
	
	@JsonView(Views.Summary.class)
	private String name;
	@JsonView(Views.Summary.class)
	private String imageUrl;
	
	@Column(columnDefinition = "boolean default true")
	private Boolean isActive = true;

	@JsonView(Views.Summary.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_level_id")
	private PlayerLevel playerLevel;
	
	@JsonView(Views.Summary.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_code")
	private PlayerCategory category;
}
