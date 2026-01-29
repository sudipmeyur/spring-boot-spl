package com.spl.spl.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "player_level", uniqueConstraints = {
	@UniqueConstraint(columnNames = "code")
})
@Data
public class PlayerLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonView(Views.Base.class)
	private Long id;
	
	@JsonView(Views.Base.class)
	@Column(unique = true)
	private String code;
	
	@JsonView(Views.PlayerLevel.class)
	private String name;
	
	@JsonView(Views.PlayerLevel.class)
	private BigDecimal baseAmount;
	
	@JsonView(Views.PlayerLevel.class)
	@Column(columnDefinition = "boolean default false")
	private Boolean isFree;
	
	@JsonView(Views.PlayerLevel.class)
	@Column(columnDefinition = "boolean default false")
	private Boolean isRandomTeamSelection;
}
