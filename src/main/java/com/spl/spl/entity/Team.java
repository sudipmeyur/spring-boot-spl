package com.spl.spl.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "team", uniqueConstraints = {
	@UniqueConstraint(columnNames = "code")
})
@Data
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;
	
	@JsonView(Views.Base.class)
	@Column(unique = true)
	private String code;
	
	@JsonView(Views.Summary.class)
	private String name;
	
	@JsonView(Views.Summary.class)
	private String logoUrl;
}
